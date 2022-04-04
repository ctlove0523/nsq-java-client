package io.github.ctlove0523.nsq.codec;

import io.github.ctlove0523.nsq.FrameType;
import io.github.ctlove0523.nsq.packets.NsqFrame;
import io.github.ctlove0523.nsq.packets.NsqFrameFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class NsqDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int size = msg.readInt();
        int frameTypeCode = msg.readInt();
        FrameType frameType = FrameType.fromFrameTypeCode(frameTypeCode);

        NsqFrame nsqFrame = NsqFrameFactory.create(frameType);
        nsqFrame.setSize(size);
        nsqFrame.setFrameType(frameTypeCode);

        // 排除frame 类型
        ByteBuf byteBuf = msg.readBytes(size - 4);
        if (byteBuf.hasArray()) {
            nsqFrame.setData(byteBuf.array());
        } else {
            byte[] array = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(array);
            nsqFrame.setData(array);
        }
        out.add(nsqFrame);

        byteBuf.release();

    }
}
