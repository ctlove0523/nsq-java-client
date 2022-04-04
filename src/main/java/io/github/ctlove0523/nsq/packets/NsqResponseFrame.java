package io.github.ctlove0523.nsq.packets;

import java.nio.charset.StandardCharsets;

public class NsqResponseFrame extends NsqFrame {

    public String getMessage() {
        return new String(getData(), StandardCharsets.UTF_8);
    }
}
