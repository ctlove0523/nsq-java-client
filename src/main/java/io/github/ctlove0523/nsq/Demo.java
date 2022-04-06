package io.github.ctlove0523.nsq;

import io.github.ctlove0523.nsq.packets.NsqFrame;

import java.nio.charset.StandardCharsets;

public class Demo {
    public static void main(String[] args) throws Exception {
        ClientMetadata metadata = ClientMetadata.builder()
                .clientId("java client")
                .build();
        NsqClient client = new NsqClient("localhost", 4150, metadata);
        client.connect();
        NsqFrame frame = client.publish("first-topic", "heihie".getBytes(StandardCharsets.UTF_8))
                .get();
        System.out.println(new String(frame.getData()));
    }
}
