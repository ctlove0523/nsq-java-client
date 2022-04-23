package io.github.ctlove0523.nsq.v1;

public interface BackoffPolicy {
    int getRetryInterval(int times);

    int getMaxRetries();
}
