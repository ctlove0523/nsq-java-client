package io.github.ctlove0523.nsq;

import io.github.ctlove0523.nsq.cmd.NsqCommand;
import io.github.ctlove0523.nsq.cmd.NsqFinCommand;
import io.github.ctlove0523.nsq.cmd.NsqIdentifyCommand;
import io.github.ctlove0523.nsq.cmd.NsqMultiplePubCommand;
import io.github.ctlove0523.nsq.cmd.NsqNopCommand;
import io.github.ctlove0523.nsq.cmd.NsqPubCommand;
import io.github.ctlove0523.nsq.cmd.NsqReadyCommand;
import io.github.ctlove0523.nsq.cmd.NsqSubCommand;
import io.github.ctlove0523.nsq.codec.NsqCommandEncoder;
import io.github.ctlove0523.nsq.codec.NsqDecoder;
import io.github.ctlove0523.nsq.codec.NsqFrameHandler;
import io.github.ctlove0523.nsq.packets.NsqErrorFrame;
import io.github.ctlove0523.nsq.packets.NsqFrame;
import io.github.ctlove0523.nsq.packets.NsqMessageFrame;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class NsqClient {
    private static final Logger log = LoggerFactory.getLogger(NsqClient.class);
    private static final byte[] PROTOCOL_VERSION = "  V2".getBytes();
    public static final AttributeKey<NsqClient> NSQ_CLIENT_ATTRIBUTE_KEY = AttributeKey.valueOf("nsq.client");
    private String host;
    private int port;
    private Channel channel;
    private SyncCommandExecutor syncCommandExecutor;
    private ClientMetadata metadata;
    private MessageHandler messageHandler = new MessageHandler() {
        @Override
        public MessageHandleResult handle(NsqMessageFrame message) {
            System.out.println(new String(message.getMessageBody()));
            return new MessageHandleResult(true, new String(message.getMessageId()));
        }
    };
    private ExecutorService messageExecutor = Executors.newFixedThreadPool(10);

    public NsqClient(String host, int port, ClientMetadata metadata) {
        this.host = host;
        this.port = port;
        this.metadata = metadata;
    }

    public void connect() throws Exception {
        log.info("begin to connect nsq");
        EventLoopGroup workerGroup = new NioEventLoopGroup(5);
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

        this.syncCommandExecutor = new SyncCommandExecutor(this.channel);
        // connect to nsq success,begin to send proto version
        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes(PROTOCOL_VERSION);
        channel.write(buf);
        channel.flush();

        // Update client metadata on the server and negotiate features
        NsqResponseFrame identifyRes = identify(metadata);
        if (identifyRes !=null && identifyRes.getMessage().equals("OK")) {
            log.info("identify success");
        } else {
            log.warn("identify failed");
        }
        log.info("connect to server success");
    }

    public NsqResponseFrame identify(ClientMetadata metadata) {
        NsqCommand command = new NsqIdentifyCommand(metadata.toJson().getBytes(StandardCharsets.UTF_8));
        return syncCommandExecutor.executeCommand(command);
    }

    public NsqResponseFrame subscribe(String topicName, String channelName) {
        NsqCommand subCommand = new NsqSubCommand(topicName, channelName);
        return syncCommandExecutor.executeCommand(subCommand);
    }

    public NsqResponseFrame publish(String topic, byte[] message) {
        NsqCommand command = new NsqPubCommand(topic, message);
        return syncCommandExecutor.executeCommand(command);
    }

    public NsqResponseFrame multiplePublish(String topic, List<byte[]> messages) {
        NsqCommand command = new NsqMultiplePubCommand(topic, messages);
        return syncCommandExecutor.executeCommand(command);
    }

    // when success this command has no response
    public void ready(int size) {
        NsqCommand readyCommand = new NsqReadyCommand(size);
        sendCommand(readyCommand);
    }

    // when success this command has no response
    public void finishMessage(String messageId) {
        NsqCommand command = new NsqFinCommand(messageId);
        sendCommand(command);
    }

    public void noop() {
        NsqCommand command = new NsqNopCommand();
        sendCommand(command);
    }

    private void heartBeat() {
        noop();
    }

    private void sendCommand(NsqCommand command) {
        channel.writeAndFlush(command);
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
                log.info("nsq response {}", new String(nsqFrame.getData()));
                syncCommandExecutor.addResponse(responseFrame);
            }

        }

        if (nsqFrame instanceof NsqErrorFrame) {
            log.warn("error response {}", ((NsqErrorFrame) nsqFrame).getErrorMessage());
            ready(10);
        }
        if (nsqFrame instanceof NsqMessageFrame) {
            NsqMessageFrame messageFrame = (NsqMessageFrame) nsqFrame;
            CompletableFuture.supplyAsync(new Supplier<MessageHandleResult>() {
                @Override
                public MessageHandleResult get() {
                    return messageHandler.handle(messageFrame);
                }
            }, messageExecutor).whenComplete(new BiConsumer<MessageHandleResult, Throwable>() {
                @Override
                public void accept(MessageHandleResult messageHandleResult, Throwable throwable) {
                    if (throwable == null && messageHandleResult.isResult()) {
                        finishMessage(messageHandleResult.getMessageId());
                        ready(1);
                    }
                }
            });
        }
    }
}
