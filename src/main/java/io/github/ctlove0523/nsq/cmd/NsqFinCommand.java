package io.github.ctlove0523.nsq.cmd;

import java.util.Collections;
import java.util.List;

public class NsqFinCommand implements NsqCommand {
    private String messageId;

    public NsqFinCommand(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public NsqCommandName commandName() {
        return NsqCommandName.FIN;
    }

    @Override
    public List<String> commandParams() {
        return Collections.singletonList(messageId);
    }
}
