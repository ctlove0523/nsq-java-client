package io.github.ctlove0523.nsq.cmd;

import java.util.Collections;
import java.util.List;

public class NsqAuthCommand implements NsqCommand {
    private byte[] auth;

    @Override
    public NsqCommandName commandName() {
        return NsqCommandName.AUTH;
    }

    @Override
    public List<byte[]> commandBody() {
        return Collections.singletonList(auth);
    }
}
