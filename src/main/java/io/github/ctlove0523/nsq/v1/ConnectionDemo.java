package io.github.ctlove0523.nsq.v1;

import io.github.ctlove0523.nsq.ClientMetadata;
import io.github.ctlove0523.nsq.NsqClient;
import io.github.ctlove0523.nsq.cmd.NsqCommand;
import io.github.ctlove0523.nsq.cmd.NsqReadyCommand;
import io.github.ctlove0523.nsq.cmd.NsqSubCommand;
import io.github.ctlove0523.nsq.packets.NsqErrorFrame;
import io.github.ctlove0523.nsq.packets.NsqMessageFrame;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executors;

public class ConnectionDemo {
    public static void main(String[] args) {
        ClientMetadata metadata = ClientMetadata.builder()
                .clientId("publisher")
                .heartbeatInterval(2000)
                .build();
        SocketAddress remote = new InetSocketAddress("localhost", 4150);
        NsqConnection connection = new NettyNsqConnection(remote, metadata, new MessageHandler() {
            @Override
            public void handleError(NsqErrorFrame error) {
                System.out.println("error");
            }

            @Override
            public void handleMessage(NsqMessageFrame message) {
                System.out.println(new String(message.getMessageBody()));
            }
        }, Executors.newFixedThreadPool(3));

        connection.connect();
        NsqCommand command = new NsqSubCommand("test-topic", "test-channel");
        connection.executeCommand(command);

        NsqCommand rdy = new NsqReadyCommand(1);
        connection.executeCommand(rdy);

    }
}
