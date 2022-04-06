package io.github.ctlove0523.nsq.cmd;

public class NsqCloseCommand implements NsqCommand {
    @Override
    public CommandName commandName() {
        return CommandName.CLS;
    }
}
