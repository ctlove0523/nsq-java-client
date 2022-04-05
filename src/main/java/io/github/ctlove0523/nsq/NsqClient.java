package io.github.ctlove0523.nsq;

import io.github.ctlove0523.nsq.cmd.NsqCommand;
import io.github.ctlove0523.nsq.cmd.NsqIdentifyCommand;
import io.github.ctlove0523.nsq.cmd.NsqNopCommand;
import io.github.ctlove0523.nsq.cmd.NsqReadyCommand;
import io.github.ctlove0523.nsq.cmd.NsqSubCommand;
import io.github.ctlove0523.nsq.codec.NsqCommandEncoder;
import io.github.ctlove0523.nsq.codec.NsqDecoder;
import io.github.ctlove0523.nsq.codec.NsqFrameHandler;
import io.github.ctlove0523.nsq.packets.NsqErrorFrame;
import io.github.ctlove0523.nsq.packets.NsqFrame;
import io.github.ctlove0523.nsq.packets.NsqResponseFrame;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class NsqClient {
    private static final Logger log = LoggerFactory.getLogger(NsqClient.class);
    private static final byte[] PROTOCOL_VERSION = "  V2".getBytes();
    public static final AttributeKey<NsqClient> NSQ_CLIENT_ATTRIBUTE_KEY = AttributeKey.valueOf("nsq.client");
    private String host;
    private int port;
    private ClientMetadata metadata;

    private Channel channel;
    private CommandReqRespContainer commandReqRespContainer;

    public NsqClient(String host, int port, ClientMetadata metadata) {
        this.host = host;
        this.port = port;
        this.metadata = metadata;
    }

    public void connect() throws Exception {
        log.info("begin to connect");
        System.out.println("begin to connect server");
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                LengthFieldBasedFrameDecoder dec = new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, Integer.BYTES);
                dec.setSingleDecode(true);

                pipeline.addLast("LengthFieldBasedFrameDecoder", dec);
                pipeline.addLast("NsqDecoder", new NsqDecoder()); // in
                pipeline.addLast("NsqEncoder", new NsqCommandEncoder()); // out
                pipeline.addLast("NSQHandler", new NsqFrameHandler()); // in
            }
        });

        ChannelFuture connectFuture = bootstrap.connect(host, port);

        this.channel = connectFuture.syncUninterruptibly().channel();
        if (!connectFuture.isSuccess()) {
            throw new Exception("connect failed");
        }

        this.channel.attr(NSQ_CLIENT_ATTRIBUTE_KEY).set(this);

        this.commandReqRespContainer = new CommandReqRespContainer(this.channel);
        // connect to nsq success,begin to send proto version
        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes(PROTOCOL_VERSION);
        channel.write(buf);
        channel.flush();

        // Update client metadata on the server and negotiate features
        NsqCommand identifyCommand = new NsqIdentifyCommand(metadata.toJson().getBytes(StandardCharsets.UTF_8));
        CompletableFuture<NsqFrame> identifyFuture = sendCommand(identifyCommand);
        NsqFrame response = identifyFuture.get();
        if (response != null) {
            log.info("identify success:{}", ((NsqResponseFrame) response).getMessage());
        }
        System.out.println("connect to server success");
    }

    public CompletableFuture<NsqFrame> subscribe(String topicName, String channelName) throws Exception {
        NsqCommand subCommand = new NsqSubCommand(topicName, channelName);
        return sendCommand(subCommand);
    }

    public void ready() {
        NsqCommand readyCommand = new NsqReadyCommand(10);
        sendCommand(readyCommand);
    }

    private void heartBeat() {
        NsqCommand heartbeatCommand = new NsqNopCommand();
        sendCommand(heartbeatCommand);
    }

    private CompletableFuture<NsqFrame> sendCommand(NsqCommand command) {
        return commandReqRespContainer.executeCommand(command);
    }

    public void close() {
        this.channel.close();
    }

    public void processNsqFrame(NsqFrame nsqFrame) {
        // 服务端发送的响应，包含心跳
        if (nsqFrame instanceof NsqResponseFrame) {
            NsqResponseFrame responseFrame = (NsqResponseFrame) nsqFrame;
            String message = responseFrame.getMessage();
            if (message.equals("_heartbeat_")) {
                log.debug("server heartbeat");
                heartBeat();
            } else {
                commandReqRespContainer.addResponse(responseFrame);
            }
        }

        if (nsqFrame instanceof NsqErrorFrame) {
            log.warn("error response {}",((NsqErrorFrame)nsqFrame).getErrorMessage());
            commandReqRespContainer.addResponse(nsqFrame);
        }
    }
}
