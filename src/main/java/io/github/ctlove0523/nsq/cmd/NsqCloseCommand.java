package io.github.ctlove0523.nsq.cmd;

public class NsqCloseCommand implements NsqCommand {
    @Override
    public NsqCommandName commandName() {
        return NsqCommandName.CLS;
    }
}
