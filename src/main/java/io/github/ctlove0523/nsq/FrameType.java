package io.github.ctlove0523.nsq;

import java.util.HashMap;
import java.util.Map;

public enum FrameType {
    FrameTypeResponse(0),
    FrameTypeError(1),
    FrameTypeMessage(2);
    private static final Map<Integer, FrameType> frameTypes = new HashMap<>();

    static {
        for (FrameType frameType : FrameType.values()) {
            frameTypes.put(frameType.frameTypeCode, frameType);
        }
    }

    private final int frameTypeCode;

    FrameType(int frameTypeCode) {
        this.frameTypeCode = frameTypeCode;
    }

    public static FrameType fromFrameTypeCode(int frameTypeCode) {
        if (frameTypeCode < 0 || frameTypeCode > 2) {
            throw new IllegalArgumentException("frame type code must between [0,2]");
        }
        return frameTypes.get(frameTypeCode);
    }
}
