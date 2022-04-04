package io.github.ctlove0523.nsq.packets;

import java.nio.charset.StandardCharsets;

public class NsqErrorFrame extends NsqFrame {

    public String getErrorMessage() {
        return new String(getData(), StandardCharsets.UTF_8);
    }
}
