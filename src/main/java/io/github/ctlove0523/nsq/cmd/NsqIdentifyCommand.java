package io.github.ctlove0523.nsq.cmd;

import java.util.Collections;
import java.util.List;

public class NsqIdentifyCommand implements NsqCommand {
    private byte[] commandBody;

    public NsqIdentifyCommand(byte[] commandBody) {
        this.commandBody = commandBody;
    }

    @Override
    public NsqCommandName commandName() {
        return NsqCommandName.IDENTIFY;
    }

    @Override
    public List<byte[]> commandBody() {
        return Collections.singletonList(this.commandBody);
    }
}
