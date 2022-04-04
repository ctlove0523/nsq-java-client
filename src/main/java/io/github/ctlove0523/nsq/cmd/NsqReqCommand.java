package io.github.ctlove0523.nsq.cmd;

import java.util.Arrays;
import java.util.List;

public class NsqReqCommand implements NsqCommand {
    private String messageId;
    private String timeout;

    public NsqReqCommand(String messageId, String timeout) {
        this.messageId = messageId;
        this.timeout = timeout;
    }

    @Override
    public NsqCommandName commandName() {
        return NsqCommandName.REQ;
    }

    @Override
    public List<String> commandParams() {
        return Arrays.asList(messageId, timeout);
    }
}
