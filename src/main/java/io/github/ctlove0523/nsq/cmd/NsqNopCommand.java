package io.github.ctlove0523.nsq.cmd;

public class NsqNopCommand implements NsqCommand {
    @Override
    public NsqCommandName commandName() {
        return NsqCommandName.NOP;
    }
}
