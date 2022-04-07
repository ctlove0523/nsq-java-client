package io.github.ctlove0523.nsq;

import io.github.ctlove0523.nsq.cmd.NsqCommand;
import io.github.ctlove0523.nsq.packets.NsqFrame;
import io.github.ctlove0523.nsq.packets.NsqResponseFrame;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class SyncCommandExecutor {
    private static final Logger log = LoggerFactory.getLogger(SyncCommandExecutor.class);
    private BlockingQueue<NsqCommand> requests = new LinkedBlockingDeque<>(1);
    private BlockingQueue<NsqResponseFrame> responses = new LinkedBlockingDeque<>(1);
    private Channel channel;

    public SyncCommandExecutor(Channel channel) {
        this.channel = channel;
    }

    public NsqResponseFrame executeCommand(NsqCommand command) {
        try {
            if (!requests.offer(command, 15, TimeUnit.SECONDS)) {
                return null;
            }

            responses.clear();
            final ChannelFuture fut = channel.writeAndFlush(command);

            if (!fut.await(15, TimeUnit.SECONDS)) {
                return null;
            }

            NsqResponseFrame response = responses.poll(15, TimeUnit.SECONDS);
            if (response == null) {
                return null;
            }

            requests.poll(); //clear the request object
            return response;
        } catch (final InterruptedException e) {
            LogManager.getLogger(this).warn("Thread was interruped!", e);
        }
        return null;
    }

    public void addResponse(NsqResponseFrame response) {
        if (!requests.isEmpty()) {
            try {
                responses.offer(response, 20, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                LogManager.getLogger(this).error("Thread was interruped, probably shuthing down", e);
            }
        }
    }
}
