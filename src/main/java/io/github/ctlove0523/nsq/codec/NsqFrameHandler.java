package io.github.ctlove0523.nsq.codec;

import io.github.ctlove0523.nsq.packets.NsqFrame;
import io.github.ctlove0523.nsq.NettyNsqConnection;
import io.github.ctlove0523.nsq.NsqConnection;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NsqFrameHandler extends SimpleChannelInboundHandler<NsqFrame> {
    private static final Logger log = LoggerFactory.getLogger(NsqFrameHandler.class);

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        NsqConnection nsqClient = ctx.channel().attr(NettyNsqConnection.NSQ_CONNECTION_ATTRIBUTE_KEY).get();
        if (nsqClient != null) {
            log.info("Client disconnected! {}", nsqClient);
        } else {
            log.error("No client set for : {}", ctx.channel());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.error("Nsq Frame Handler exception caught", cause);

        ctx.channel().close();
        NsqConnection client = ctx.channel().attr(NettyNsqConnection.NSQ_CONNECTION_ATTRIBUTE_KEY).get();
        if (client != null) {
//            client.close();
        } else {
            log.warn("No connection set for : {}", ctx.channel());
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NsqFrame msg) throws Exception {
        final NsqConnection client = ctx.channel().attr(NettyNsqConnection.NSQ_CONNECTION_ATTRIBUTE_KEY).get();
        if (client != null) {
            ctx.channel().eventLoop().execute(() -> client.processNsqFrame(msg));
        } else {
            log.warn("No nsq client set for : " + ctx.channel());
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        final NsqConnection connection = ctx.channel().attr(NettyNsqConnection.NSQ_CONNECTION_ATTRIBUTE_KEY).get();
//        if (Objects.nonNull(connection)) {
//
//            connection.reconnect();
//        }
    }
}
