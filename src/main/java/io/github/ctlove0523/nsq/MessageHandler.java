package io.github.ctlove0523.nsq;

import io.github.ctlove0523.nsq.packets.NsqMessageFrame;

import java.util.concurrent.CompletableFuture;

public interface MessageHandler {
    MessageHandleResult handle(NsqMessageFrame message);
}
