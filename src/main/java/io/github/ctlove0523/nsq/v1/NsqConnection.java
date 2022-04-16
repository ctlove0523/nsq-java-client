package io.github.ctlove0523.nsq.v1;

import io.github.ctlove0523.nsq.cmd.NsqCommand;
import io.github.ctlove0523.nsq.packets.NsqFrame;
import io.netty.channel.Channel;

import java.net.SocketAddress;

public interface NsqConnection {

    Channel getChannel();

    SocketAddress remoteAddress();

    boolean connect();

    boolean reconnect();

    boolean disconnect();

    NsqFrame executeCommand(NsqCommand command);

    void executeCommandNoResponse(NsqCommand command);

    void processNsqFrame(NsqFrame nsqFrame);
}
