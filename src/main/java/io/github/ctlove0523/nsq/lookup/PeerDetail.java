package io.github.ctlove0523.nsq.lookup;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class PeerDetail {
    private String remote_address;
    private String hostname;
    private String broadcast_address;
    private int tcp_port;
    private int http_port;
    private String version;
    private List<String> tombstones;
    private List<String> topics;
}
