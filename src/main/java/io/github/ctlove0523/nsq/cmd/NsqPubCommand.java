package io.github.ctlove0523.nsq.cmd;

import java.util.Collections;
import java.util.List;

public class NsqPubCommand implements NsqCommand {
    private String topicName;
    public byte[] message;

    public NsqPubCommand(String topicName, byte[] message) {
        this.topicName = topicName;
        this.message = message;
    }

    @Override
    public CommandName commandName() {
        return CommandName.PUB;
    }

    @Override
    public List<String> commandParams() {
        return Collections.singletonList(topicName);
    }

    @Override
    public List<byte[]> commandBody() {
        return Collections.singletonList(message);
    }
}
