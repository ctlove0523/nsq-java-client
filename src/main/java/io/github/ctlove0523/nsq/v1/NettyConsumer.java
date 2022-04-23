package io.github.ctlove0523.nsq.v1;

import io.github.ctlove0523.nsq.ClientMetadata;
import io.github.ctlove0523.nsq.cmd.NsqCommand;
import io.github.ctlove0523.nsq.cmd.NsqReadyCommand;
import io.github.ctlove0523.nsq.cmd.NsqSubCommand;
import io.github.ctlove0523.nsq.packets.NsqFrame;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NettyConsumer implements Consumer {
    private String topic;
    private String channel;
    private Set<SocketAddress> nsqAddresses;
    private TopicLookup topicLookup;
    private BackoffPolicy backoffPolicy;
    private int pollingInterval;
    private ClientMetadata clientMetadata;
    private MessageHandler messageHandler;
    private ExecutorService executorService;

    private Map<SocketAddress, NsqConnection> connections = new HashMap<>();


    public NettyConsumer(ConsumerBuilder builder) {
        this.topic = builder.getTopic();
        this.channel = builder.getChannel();
        this.nsqAddresses = builder.getNsqAddresses();
        this.topicLookup = builder.getTopicLookup();
        this.backoffPolicy = builder.getBackoffPolicy();
        this.pollingInterval = builder.getPollingInterval();
        this.clientMetadata = builder.getClientMetadata();
        this.messageHandler = builder.getMessageHandler();

        if (builder.getExecutorService() == null) {
            this.executorService = Executors.newFixedThreadPool(1);
        } else {
            this.executorService = builder.getExecutorService();
        }
    }

    @Override
    public void start() {
        if (topicLookup != null) {
            Set<SocketAddress> oldAddresses = connections.keySet();
            Set<SocketAddress> addresses = topicLookup.addresses(topic);
            Set<SocketAddress> addedAddresses = addedSocketAddresses(oldAddresses, addresses);
            for (SocketAddress address : addedAddresses) {
                NsqConnection connection = new NettyNsqConnection(address, clientMetadata, messageHandler, executorService, backoffPolicy);
                connection.connect();
                connections.put(address, connection);
            }

            Set<SocketAddress> lostAddresses = lostSocketAddresses(oldAddresses, addresses);
            for (SocketAddress address : lostAddresses) {
                System.out.println(address + " lost consumer");
            }

        } else {
            for (SocketAddress address : nsqAddresses) {
                NsqConnection connection = new NettyNsqConnection(address, clientMetadata, messageHandler, executorService, backoffPolicy);
                connection.connect();
                connections.put(address, connection);
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                scheduler.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        for (NsqConnection conn : connections.values()) {
                            if (!conn.connected()) {
                                conn.reconnect();
                                subscribe(conn);
                            }
                        }
                    }
                }, 5L, 5L, TimeUnit.SECONDS);
            }
        }

        subscribe();

    }

    private void subscribe(NsqConnection connection) {
        if (!connection.connected()) {
            connection.disconnect();
        }

        NsqCommand subCommand = new NsqSubCommand(topic, channel);
        NsqFrame subResponse = connection.executeCommand(subCommand);
        if (Objects.nonNull(subResponse) && new String(subResponse.getData()).equals("OK")) {
            System.out.println("sub to " + connection.remoteAddress() + " success");

            NsqCommand rdyCommand = new NsqReadyCommand(1);
            connection.executeCommandNoResponse(rdyCommand);
        }
    }

    private void subscribe() {
        // 订阅
        NsqCommand subCommand = new NsqSubCommand(topic, channel);
        NsqCommand rdyCommand = new NsqReadyCommand(1);
        for (Iterator<Map.Entry<SocketAddress, NsqConnection>> iterator = connections.entrySet().iterator(); iterator.hasNext(); ) {
            NsqConnection connection = iterator.next().getValue();
            if (!connection.connected()) {
                connection.disconnect();
                iterator.remove();
                continue;
            }

            NsqFrame subResponse = connection.executeCommand(subCommand);
            if (Objects.isNull(subResponse) || !new String(subResponse.getData()).equals("OK")) {
                System.out.println("sub failed");
                iterator.remove();
                continue;
            }

            System.out.println("sub to " + connection.remoteAddress() + " success");
            connection.executeCommandNoResponse(rdyCommand);
        }
    }

    @Override
    public void stop() {

    }

    private Set<SocketAddress> addedSocketAddresses(Set<SocketAddress> oldAddresses, Set<SocketAddress> newAddresses) {
        Set<SocketAddress> addedAddresses = new HashSet<>();
        for (SocketAddress address : newAddresses) {
            if (!oldAddresses.contains(address)) {
                addedAddresses.add(address);
            }
        }

        return addedAddresses;
    }

    private Set<SocketAddress> lostSocketAddresses(Set<SocketAddress> oldAddresses, Set<SocketAddress> newAddresses) {
        Set<SocketAddress> lostAddresses = new HashSet<>();
        for (SocketAddress address : oldAddresses) {
            if (!newAddresses.contains(address)) {
                lostAddresses.add(address);
            }
        }

        return lostAddresses;
    }
}
