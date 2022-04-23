package io.github.ctlove0523.nsq.v1;

import java.net.SocketAddress;
import java.util.Set;

public interface TopicLookup {
    Set<SocketAddress> addresses(String topic);
}
