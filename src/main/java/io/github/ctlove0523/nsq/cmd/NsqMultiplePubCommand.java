package io.github.ctlove0523.nsq.cmd;

import java.util.Collections;
import java.util.List;

public class NsqMultiplePubCommand implements NsqCommand {
    private String topicName;
    public List<byte[]> messages;

    public NsqMultiplePubCommand(String topicName, List<byte[]> messages) {
        this.topicName = topicName;
        this.messages = messages;
    }

    @Override
    public NsqCommandName commandName() {
        return NsqCommandName.MultiplePub;
    }

    @Override
    public List<String> commandParams() {
        return Collections.singletonList(topicName);
    }

    @Override
    public List<byte[]> commandBody() {
        return messages;
    }
}
