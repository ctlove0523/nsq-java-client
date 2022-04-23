package io.github.ctlove0523.nsq;

import java.net.SocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class ConsumerBuilder {
    private String topic;
    private String channel;
    private Set<SocketAddress> nsqAddresses;
    private TopicLookup topicLookup;
    private BackoffPolicy backoffPolicy;
    private int pollingInterval;
    private ClientMetadata clientMetadata;
    private MessageHandler messageHandler;
    private ExecutorService executorService;

    public ConsumerBuilder topic(String topic) {
        this.topic = topic;
        return this;
    }

    public ConsumerBuilder channel(String channel) {
        this.channel = channel;
        return this;
    }

    public ConsumerBuilder addNsqAddresses(SocketAddress... address) {
        if (nsqAddresses == null) {
            nsqAddresses = new HashSet<>();
        }

        nsqAddresses.addAll(Arrays.asList(address));
        return this;
    }

    public ConsumerBuilder topicLookup(TopicLookup topicLookup) {
        this.topicLookup = topicLookup;
        return this;
    }

    public ConsumerBuilder backoffPolicy(BackoffPolicy backoffPolicy) {
        this.backoffPolicy = backoffPolicy;
        return this;
    }

    public ConsumerBuilder pollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval;
        return this;
    }

    public ConsumerBuilder clientMetadata(ClientMetadata clientMetadata) {
        this.clientMetadata = clientMetadata;
        return this;
    }

    public ConsumerBuilder messageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
        return this;
    }

    public ConsumerBuilder executorService(ExecutorService  executorService) {
        this.executorService = executorService;
        return this;
    }

    public Consumer build() {
        return new NettyConsumer(this);
    }

    public String getTopic() {
        return topic;
    }

    public String getChannel() {
        return channel;
    }

    public Set<SocketAddress> getNsqAddresses() {
        return nsqAddresses;
    }

    public TopicLookup getTopicLookup() {
        return topicLookup;
    }

    public BackoffPolicy getBackoffPolicy() {
        return backoffPolicy;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public ClientMetadata getClientMetadata() {
        return clientMetadata;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
