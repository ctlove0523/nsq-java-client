package io.github.ctlove0523.nsq.lookup;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class Nodes {
    private List<PeerDetail> producers;
}
