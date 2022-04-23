package io.github.ctlove0523.nsq;

import java.net.SocketAddress;
import java.util.Set;

public interface TopicLookup {
    Set<SocketAddress> addresses(String topic);
}
