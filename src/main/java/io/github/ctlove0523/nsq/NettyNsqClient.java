package io.github.ctlove0523.nsq;

import io.github.ctlove0523.nsq.cmd.NsqCommand;
import io.github.ctlove0523.nsq.cmd.NsqReadyCommand;
import io.github.ctlove0523.nsq.cmd.NsqSubCommand;
import io.github.ctlove0523.nsq.packets.NsqResponseFrame;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class NettyNsqClient implements NsqClient {
    private List<SocketAddress> nsqdAddresses = new ArrayList<>();
    private MessageHandler messageHandler;
    private ExecutorService executor;
    private ClientMetadata clientMetadata;

    public NettyNsqClient() {
        this.nsqdAddresses.add(new InetSocketAddress("localhost", 4150));
    }

    @Override
    public void registerMessageHandler(MessageHandler messageHandler, ExecutorService executor) {
        this.messageHandler = messageHandler;
        this.executor = executor;
    }

    @Override
    public void subscribe(String topicName, String channelName) {
        for (SocketAddress address : nsqdAddresses) {
            BackoffPolicy policy = new BackoffPolicy() {
                @Override
                public int getRetryInterval(int times) {
                    return 10 * times;
                }

                @Override
                public int getMaxRetries() {
                    return 3;
                }
            };
            NsqConnection connection = new NettyNsqConnection(address, clientMetadata, messageHandler, executor, policy);
            boolean connected = connection.connect();
            if (!connected) {
                System.out.println("connect to server failed");
                continue;
            }

            NsqCommand subCommand = new NsqSubCommand(topicName, channelName);
            NsqResponseFrame subResponse = (NsqResponseFrame) connection.executeCommand(subCommand);
            if (subResponse != null && subResponse.getMessage().equals("OK")) {
                System.out.println("sub success");
            }

            NsqCommand rdyCommand = new NsqReadyCommand(1);
            connection.executeCommandNoResponse(rdyCommand);

        }

    }

    @Override
    public NsqResponseFrame publish(String topic, byte[] message) {
        return null;
    }

    @Override
    public NsqResponseFrame multiplePublish(String topic, List<byte[]> messages) {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
