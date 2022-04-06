package io.github.ctlove0523.nsq.cmd;

import java.util.ArrayList;
import java.util.List;

public interface NsqCommand {
    CommandName commandName();

    default List<String> commandParams() {
        return new ArrayList<>();
    }

    default List<byte[]> commandBody() {
        return new ArrayList<>();
    }
}
