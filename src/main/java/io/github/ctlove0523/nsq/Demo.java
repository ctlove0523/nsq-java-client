package io.github.ctlove0523.nsq;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class Demo {
    public static void main(String[] args) throws Exception {
        ClientMetadata metadata = ClientMetadata.builder()
                .clientId("publisher")
                .build();
        NsqClient client = new NsqClient("localhost", 4150, metadata);
        client.connect();
        for (int i = 0; i < 100; i++) {
            client.publish("test-consumer", (i + "-heihie").getBytes(StandardCharsets.UTF_8));
        }
    }
}
