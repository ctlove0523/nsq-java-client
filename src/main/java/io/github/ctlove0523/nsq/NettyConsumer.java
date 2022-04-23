package io.github.ctlove0523.nsq;

import io.github.ctlove0523.nsq.v1.NsqConnection;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

public class NettyConsumer {
    private String topicName;
    private String channelName;

    private Map<SocketAddress, NsqConnection> connections = new HashMap<>();

}
