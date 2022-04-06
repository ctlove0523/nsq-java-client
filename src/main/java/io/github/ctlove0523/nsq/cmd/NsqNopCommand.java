package io.github.ctlove0523.nsq.cmd;

public class NsqNopCommand implements NsqCommand {
    @Override
    public CommandName commandName() {
        return CommandName.NOP;
    }
}
