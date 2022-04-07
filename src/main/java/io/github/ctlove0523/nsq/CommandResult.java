package io.github.ctlove0523.nsq;

import io.github.ctlove0523.nsq.cmd.NsqCommand;

public class CommandResult {
    private NsqCommand command;
    private boolean result;
    private String errorMessage;
    private Exception cause;

    public static CommandResult success(NsqCommand command) {
        CommandResult result = new CommandResult();
        result.setCommand(command);
        result.setResult(true);
        return result;
    }

    public static CommandResult exception(NsqCommand command, Exception e) {
        CommandResult result = new CommandResult();
        result.setCommand(command);
        result.setResult(false);
        result.setCause(e);
        return result;
    }

    public static CommandResult failed(NsqCommand command, String errorMessage) {
        CommandResult result = new CommandResult();
        result.setCommand(command);
        result.setResult(false);
        result.setErrorMessage(errorMessage);
        return result;
    }


    public NsqCommand getCommand() {
        return command;
    }

    public void setCommand(NsqCommand command) {
        this.command = command;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Exception getCause() {
        return cause;
    }

    public void setCause(Exception cause) {
        this.cause = cause;
    }
}
