package io.github.ctlove0523.nsq.v1;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class ProducerInfo {
    private List<String> channels = new ArrayList<>();
    private List<PeerInfo> producers = new ArrayList<>();
}
