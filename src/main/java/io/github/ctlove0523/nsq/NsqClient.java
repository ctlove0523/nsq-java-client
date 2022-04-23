package io.github.ctlove0523.nsq;

import io.github.ctlove0523.nsq.packets.NsqResponseFrame;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.ExecutorService;

public interface NsqClient extends Closeable {
    void registerMessageHandler(MessageHandler messageHandler, ExecutorService executor);

    void subscribe(String topicName, String channelName);

    NsqResponseFrame publish(String topic, byte[] message);

    NsqResponseFrame multiplePublish(String topic, List<byte[]> messages);
}
