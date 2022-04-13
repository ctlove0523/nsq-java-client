package io.github.ctlove0523.nsq.v1;

import io.github.ctlove0523.nsq.packets.NsqErrorFrame;
import io.github.ctlove0523.nsq.packets.NsqMessageFrame;

public interface MessageHandler {
    void handleError(NsqErrorFrame error);

    void handleMessage(NsqMessageFrame message);
}
