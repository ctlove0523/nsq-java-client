package io.github.ctlove0523.nsq.v1;


import io.github.ctlove0523.nsq.ClientMetadata;
import io.github.ctlove0523.nsq.packets.NsqErrorFrame;
import io.github.ctlove0523.nsq.packets.NsqMessageFrame;

import java.net.InetSocketAddress;

public class ConsumerDemo {
    public static void main(String[] args) {
        ClientMetadata metadata = ClientMetadata.builder()
                .heartbeatInterval(10*1000)
                .hostname("localhost")
                .build();

        Consumer consumer = new ConsumerBuilder()
                .topic("sdk")
                .channel("sdk-channel")
                .addNsqAddresses(InetSocketAddress.createUnresolved("localhost", 4150))
                .addNsqAddresses(InetSocketAddress.createUnresolved("localhost",6150))
                .clientMetadata(metadata)
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
