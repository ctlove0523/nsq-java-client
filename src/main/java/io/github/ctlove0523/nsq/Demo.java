package io.github.ctlove0523.nsq;

public class Demo {
    public static void main(String[] args) throws Exception {
        ClientMetadata metadata = ClientMetadata.builder()
                .clientId("nsq-java-client")
                .hostname("localhost")
                .build();
        NsqClient client = new NsqClient("localhost", 4150, metadata);
        client.connect();
        client.subscribe("first-topic","random-channel");
        client.ready();
    }
}
