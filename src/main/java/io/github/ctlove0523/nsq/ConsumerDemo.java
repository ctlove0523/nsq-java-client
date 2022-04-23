package io.github.ctlove0523.nsq;


import io.github.ctlove0523.nsq.packets.NsqErrorFrame;
import io.github.ctlove0523.nsq.packets.NsqMessageFrame;

import java.net.InetSocketAddress;
import java.util.Collections;

public class ConsumerDemo {
    public static void main(String[] args) {
        ClientMetadata metadata = new ClientMetadata.Builder()
                .heartbeatInterval(40*1000)
                .hostname("localhost")
                .build();

        TopicLookup topicLookup = new DefaultTopicLookup(Collections.singletonList("http://localhost:4161"));

        Consumer consumer = new ConsumerBuilder()
                .topic("sdk")
                .channel("sdk-channel")
                .addNsqAddresses(InetSocketAddress.createUnresolved("localhost", 4150))
                .addNsqAddresses(InetSocketAddress.createUnresolved("localhost",6150))
                .clientMetadata(metadata)
                .topicLookup(topicLookup)
                .pollingInterval(10)
                .messageHandler(new MessageHandler() {
                    @Override
                    public void handleError(NsqErrorFrame error) {

                    }

                    @Override
                    public void handleMessage(NsqMessageFrame message) {
                        System.out.println(new String(message.getMessageBody()));
                    }
                })
                .build();

        consumer.start();
    }
}
