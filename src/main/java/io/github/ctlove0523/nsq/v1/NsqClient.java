package io.github.ctlove0523.nsq.v1;

import io.github.ctlove0523.nsq.packets.NsqResponseFrame;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.ExecutorService;

public interface NsqClient extends Closeable {
    void registerMessageHandler(MessageHandler messageHandler, ExecutorService executor);

    NsqResponseFrame subscribe(String topicName, String channelName);

    NsqResponseFrame publish(String topic, byte[] message);

    NsqResponseFrame multiplePublish(String topic, List<byte[]> messages);
}
