package io.github.ctlove0523.nsq.lookup;

public interface NsqLookupClient {

    String getVersion();

    boolean ping();
}
