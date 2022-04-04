package io.github.ctlove0523.nsq.cmd;

import java.util.Collections;
import java.util.List;

public class NsqReadyCommand implements NsqCommand {
    private int count;

    public NsqReadyCommand(int count) {
        this.count = count;
    }

    @Override
    public NsqCommandName commandName() {
        return NsqCommandName.RDY;
    }

    @Override
    public List<String> commandParams() {
        return Collections.singletonList(Integer.toString(count));
    }
}
