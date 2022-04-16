package io.github.ctlove0523.nsq.lookup;

public interface NsqLookupClient {

    String getVersion();

    boolean ping();

    ProducerInfo getProducer(String topic);

    Topics getTopics();

    Channels getChannels(String topic);

    Nodes listNodes();

    boolean addTopic(String topic);

    boolean deleteTopic(String topic);

    boolean addChannel(String topic, String channel);

    boolean deleteChannel(String topic, String channel);
}
