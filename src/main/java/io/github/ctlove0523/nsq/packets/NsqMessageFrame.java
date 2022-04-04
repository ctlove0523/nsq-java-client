package io.github.ctlove0523.nsq.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class NsqMessageFrame extends NsqFrame {
    private long timestamp;
    private int attempts;
    private byte[] messageId = new byte[16];
    private byte[] messageBody;

    @Override
    public void setData(byte[] data) {
        super.setData(data);

        ByteBuf buf = Unpooled.wrappedBuffer(data);
        this.timestamp = buf.readLong();
        this.attempts = buf.readShort();
        buf.readBytes(messageId);
        ByteBuf bodyBuf = buf.readBytes(buf.readableBytes());
        if (bodyBuf.hasArray()) {
            this.messageBody = bodyBuf.array();
        } else {
            byte[] bodyArray = new byte[bodyBuf.readableBytes()];
            bodyBuf.readBytes(bodyArray);
            this.messageBody = bodyArray;
        }

        bodyBuf.release();
        buf.release();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getAttempts() {
        return attempts;
    }

    public byte[] getMessageId() {
        return messageId;
    }

    public byte[] getMessageBody() {
        return messageBody;
    }
}
