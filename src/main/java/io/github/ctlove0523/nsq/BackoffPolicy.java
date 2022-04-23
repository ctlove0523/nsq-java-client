package io.github.ctlove0523.nsq;

public interface BackoffPolicy {
    int getRetryInterval(int times);

    int getMaxRetries();
}
