package io.github.ctlove0523.nsq.cmd;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NsqDeferPubCommand implements NsqCommand {
    private String topicName;
    private String deferTime;
    private byte[] message;

    public NsqDeferPubCommand(String topicName, String deferTime, byte[] message) {
        this.topicName = topicName;
        this.deferTime = deferTime;
        this.message = message;
    }

    @Override
    public CommandName commandName() {
        return CommandName.DeferPub;
    }

    @Override
    public List<String> commandParams() {
        return Arrays.asList(topicName, deferTime);
    }

    @Override
    public List<byte[]> commandBody() {
        return Collections.singletonList(message);
    }
}
