package io.github.ctlove0523.nsq;

public class MessageHandleResult {
    private boolean result;
    private String messageId;

    public MessageHandleResult(boolean result, String messageId) {
        this.result = result;
        this.messageId = messageId;
    }

    public boolean isResult() {
        return result;
    }

    public String getMessageId() {
        return messageId;
    }
}
