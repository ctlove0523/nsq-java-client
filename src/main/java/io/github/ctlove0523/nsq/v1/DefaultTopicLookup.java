package io.github.ctlove0523.nsq.v1;

import io.github.ctlove0523.nsq.lookup.JdkNsqLookupClient;
import io.github.ctlove0523.nsq.lookup.NsqLookupClient;
import io.github.ctlove0523.nsq.lookup.PeerInfo;
import io.github.ctlove0523.nsq.lookup.ProducerInfo;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DefaultTopicLookup implements TopicLookup {
    private List<String> nsqLookupAddresses;
    private List<NsqLookupClient> nsqLookupClients;

    public DefaultTopicLookup(List<String> nsqLookupAddresses) {
        this.nsqLookupAddresses = nsqLookupAddresses;
        this.nsqLookupClients = new ArrayList<>(nsqLookupAddresses.size());
        for (String address : nsqLookupAddresses) {
            nsqLookupClients.add(new JdkNsqLookupClient(address));
        }

    }

    @Override
    public Set<SocketAddress> addresses(String topic) {
        return null;
    }

    private Set<SocketAddress> getProducers(String topic) {
        Set<SocketAddress> socketAddresses = new HashSet<>();
        for (NsqLookupClient client : nsqLookupClients) {
            ProducerInfo producer = client.getProducer(topic);
            if (Objects.isNull(producer) || producer.getProducers().isEmpty()) {
                continue;
            }

            for (PeerInfo peer : producer.getProducers()) {
                String host = peer.getBroadcast_address();
                int port = peer.getTcp_port();
                SocketAddress socketAddress = InetSocketAddress.createUnresolved(host, port);
                socketAddresses.add(socketAddress);
            }
        }

        return socketAddresses;
    }
}
