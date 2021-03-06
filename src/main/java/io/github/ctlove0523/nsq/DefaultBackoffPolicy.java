package io.github.ctlove0523.nsq;

public class DefaultBackoffPolicy implements BackoffPolicy {
    @Override
    public int getRetryInterval(int times) {
        return 10;
    }

    @Override
    public int getMaxRetries() {
        return 2;
    }
}
