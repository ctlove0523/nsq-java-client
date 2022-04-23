package io.github.ctlove0523.nsq.v1;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PeerInfo {
    private String remote_address;
    private String hostname;
    private String broadcast_address;
    private int tcp_port;
    private int http_port;
    private String version;
}
