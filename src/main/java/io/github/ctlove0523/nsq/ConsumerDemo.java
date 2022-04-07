package io.github.ctlove0523.nsq;

import io.github.ctlove0523.nsq.packets.NsqResponseFrame;

public class ConsumerDemo {
    public static void main(String[] args) throws Exception {
        ClientMetadata metadata = ClientMetadata.builder()
                .clientId("consumer")
                .outputBufferSize(10240)
                .build();
        NsqClient client = new NsqClient("localhost", 4150, metadata);
        client.connect();

        NsqResponseFrame responseFrame = client.subscribe("test-consumer", "random");
        System.out.println(responseFrame.getMessage());
        client.ready(100);
    }
}
