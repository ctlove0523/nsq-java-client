package io.github.ctlove0523.nsq.cmd;

import java.util.Arrays;
import java.util.List;

public class NsqSubCommand implements NsqCommand {
    private String topicName;
    private String channelName;

    public NsqSubCommand(String topicName, String channelName) {
        this.topicName = topicName;
        this.channelName = channelName;
    }

    @Override
    public CommandName commandName() {
        return CommandName.SUB;
    }

    @Override
    public List<String> commandParams() {
        return Arrays.asList(topicName, channelName);
    }
}
