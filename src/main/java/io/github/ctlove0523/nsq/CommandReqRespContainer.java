package io.github.ctlove0523.nsq;

import io.github.ctlove0523.nsq.cmd.NsqCommand;
import io.github.ctlove0523.nsq.packets.NsqFrame;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CommandReqRespContainer {
    private BlockingQueue<NsqCommand> requests = new ArrayBlockingQueue<>(1);
    private BlockingQueue<NsqFrame> responses = new ArrayBlockingQueue<>(1);
    private Channel channel;
    private CompletableFuture<NsqFrame> result;

    public CommandReqRespContainer(Channel channel) {
        this.channel = channel;
    }

    public CompletableFuture<NsqFrame> executeCommand(NsqCommand command) {
        try {
            requests.offer(command, 10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            result.completeExceptionally(new TimeoutException());
            return result;
        }
        result = new CompletableFuture<>();
        responses.clear();

        ChannelFuture f = channel.writeAndFlush(command);
        try {
            boolean cmdSendResult = f.await(5, TimeUnit.SECONDS);
            if (!cmdSendResult) {
                result.completeExceptionally(new TimeoutException("cmd time out"));
                return result;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            result.completeExceptionally(e);
            return result;
        }

        NsqFrame cmdResponse = null;
        try {
            cmdResponse = responses.poll(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            result.completeExceptionally(e);
            return result;
        }

        if (cmdResponse == null) {
            result.completeExceptionally(new Exception("no response"));
        } else {
            result.complete(cmdResponse);
        }

        return result;
    }

    public void addResponse(NsqFrame response) {
        if (!requests.isEmpty()) {
            System.out.println("begin to add response");
            responses.offer(response);
        }
    }
}
