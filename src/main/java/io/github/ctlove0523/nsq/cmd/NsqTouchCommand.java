package io.github.ctlove0523.nsq.cmd;

import java.util.Collections;
import java.util.List;

public class NsqTouchCommand implements NsqCommand {
    private String messageId;

    public NsqTouchCommand(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public NsqCommandName commandName() {
        return NsqCommandName.TOUCH;
    }

    @Override
    public List<String> commandParams() {
        return Collections.singletonList(messageId);
    }
}
