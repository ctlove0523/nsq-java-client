package io.github.ctlove0523.nsq.packets;

import io.github.ctlove0523.nsq.FrameType;

public class NsqFrameFactory {

    public static NsqFrame create(FrameType frameType) {
        switch (frameType) {
            case FrameTypeResponse:
                return new NsqResponseFrame();
            case FrameTypeError:
                return new NsqErrorFrame();
            case FrameTypeMessage:
                return new NsqMessageFrame();
            default:
                throw new IllegalArgumentException("frame type invalid");
        }
    }
}
