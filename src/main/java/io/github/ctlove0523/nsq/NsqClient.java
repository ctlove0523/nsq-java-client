package io.github.ctlove0523.nsq;

import io.github.ctlove0523.nsq.cmd.NsqCommand;
import io.github.ctlove0523.nsq.cmd.NsqIdentifyCommand;
import io.github.ctlove0523.nsq.codec.NsqCommandEncoder;
import io.github.ctlove0523.nsq.codec.NsqDecoder;
import io.github.ctlove0523.nsq.codec.NsqFrameHandler;
import io.github.ctlove0523.nsq.packets.NsqFrame;
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

import java.nio.charset.StandardCharsets;

public class NsqClient {
    private static final byte[] PROTOCOL_VERSION = "  V2".getBytes();
    public static final AttributeKey<NsqClient> NSQ_CLIENT_ATTRIBUTE_KEY = AttributeKey.valueOf("nsq.client");
    private String host;
    private int port;
    private ClientMetadata metadata;

    private Channel channel;

    public void connect() throws Exception {
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

        // connect to nsq success,begin to send proto version
        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes(PROTOCOL_VERSION);
        channel.write(buf);
        channel.flush();

        // Update client metadata on the server and negotiate features
        NsqCommand identifyCommand = new NsqIdentifyCommand(metadata.toJson().getBytes(StandardCharsets.UTF_8));
        ChannelFuture identifyFuture = sendCommand(identifyCommand);
        identifyFuture.sync();
    }

    private ChannelFuture sendCommand(NsqCommand command) {
        return this.channel.writeAndFlush(command);
    }

    public void close() {
        this.channel.close();
    }

    public void processNsqFrame(NsqFrame nsqFrame) {

    }
}
