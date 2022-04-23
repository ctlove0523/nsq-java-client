package io.github.ctlove0523.nsq.v1;

import io.github.ctlove0523.nsq.ClientMetadata;
import io.github.ctlove0523.nsq.SyncCommandExecutor;
import io.github.ctlove0523.nsq.cmd.NsqCloseCommand;
import io.github.ctlove0523.nsq.cmd.NsqCommand;
import io.github.ctlove0523.nsq.cmd.NsqFinCommand;
import io.github.ctlove0523.nsq.cmd.NsqIdentifyCommand;
import io.github.ctlove0523.nsq.cmd.NsqNopCommand;
import io.github.ctlove0523.nsq.cmd.NsqReadyCommand;
import io.github.ctlove0523.nsq.codec.NsqCommandEncoder;
import io.github.ctlove0523.nsq.codec.NsqDecoder;
import io.github.ctlove0523.nsq.codec.NsqFrameHandler;
import io.github.ctlove0523.nsq.packets.NsqErrorFrame;
import io.github.ctlove0523.nsq.packets.NsqFrame;
import io.github.ctlove0523.nsq.packets.NsqMessageFrame;
import io.github.ctlove0523.nsq.packets.NsqResponseFrame;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NettyNsqConnection implements NsqConnection {
    private static final Logger log = LoggerFactory.getLogger(NettyNsqConnection.class);
    public static final AttributeKey<NsqConnection> NSQ_CONNECTION_ATTRIBUTE_KEY = AttributeKey.valueOf("nsq.client");
    private static final byte[] PROTOCOL_VERSION = "  V2".getBytes();

    private Bootstrap bootstrap;
    private Channel channel;
    private SyncCommandExecutor commandExecutor;
    private SocketAddress remoteAddress;
    private ClientMetadata clientMetadata;
    private MessageHandler messageHandler;
    private ExecutorService messageExecutor;
    private BackoffPolicy backoffPolicy;

    public NettyNsqConnection(SocketAddress remoteAddress, ClientMetadata clientMetadata,
                              MessageHandler messageHandler, ExecutorService messageExecutor,
                              BackoffPolicy backoffPolicy) {
        Objects.requireNonNull(remoteAddress, "remoteAddress");
        Objects.requireNonNull(clientMetadata, "clientMetadata");
        Objects.requireNonNull(messageHandler, "messageHandler");

        this.remoteAddress = remoteAddress;
        this.clientMetadata = clientMetadata;
        this.messageHandler = messageHandler;

        if (Objects.nonNull(messageExecutor)) {
            this.messageExecutor = messageExecutor;
        } else {
            this.messageExecutor = Executors.newSingleThreadExecutor();
        }

        if (Objects.nonNull(backoffPolicy)) {
            this.backoffPolicy = backoffPolicy;
        } else {
            this.backoffPolicy = new DefaultBackoffPolicy();
        }

        EventLoopGroup workerGroup = new NioEventLoopGroup(5);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                LengthFieldBasedFrameDecoder dec = new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, Integer.BYTES);
                dec.setSingleDecode(true);

                pipeline.addLast("LengthFieldBasedFrameDecoder", dec);
                pipeline.addLast("NsqDecoder", new NsqDecoder()); // in
                pipeline.addLast("NsqEncoder", new NsqCommandEncoder()); // out
                pipeline.addLast("NSQHandler", new NsqFrameHandler()); // in
            }
        });
        this.bootstrap = bootstrap;
    }

    @Override
    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public SocketAddress remoteAddress() {
        return remoteAddress;
    }

    @Override
    public boolean connect() {
        log.info("begin to connect nsq server,remote address = {}", remoteAddress);

        ChannelFuture connectFuture = bootstrap.connect(remoteAddress);

        connectFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    connect();
                }
            }
        });

        this.channel = connectFuture.syncUninterruptibly().channel();
        if (!connectFuture.isSuccess()) {
            log.warn("connect to nsq server failed");
            return false;
        }

        this.channel.attr(NSQ_CONNECTION_ATTRIBUTE_KEY).set(this);
        this.commandExecutor = new SyncCommandExecutor(this.channel);

        // connect to nsq success,begin to send proto version
        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes(PROTOCOL_VERSION);
        channel.write(buf);
        channel.flush();

        // Update client metadata on the server and negotiate features
        NsqCommand identityCommand = new NsqIdentifyCommand(clientMetadata.toJson().getBytes(StandardCharsets.UTF_8));
        NsqFrame identifyResponse = executeCommand(identityCommand);
        if (identifyResponse != null && new String(identifyResponse.getData()).equals("OK")) {
            log.info("Update client metadata on the server and negotiate features success");
        } else {
            log.warn("Update client metadata on the server and negotiate features failed");
            return false;
        }

        log.info("connect nsq server: {},success", remoteAddress);
        return true;
    }

    @Override
    public boolean connected() {
        return channel.isActive();
    }

    @Override
    public boolean reconnect() {
        System.out.println("begin to reconnect to nsqd");
        for (int i = 0; i < backoffPolicy.getMaxRetries(); i++) {
            if (connected()) {
                return true;
            }

            int retryInterval = backoffPolicy.getRetryInterval(i);
            try {
                TimeUnit.SECONDS.sleep(retryInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            boolean reconnectRes = connect();
            if (reconnectRes) {
                System.out.println("reconnect to nsq success");
                break;
            }
        }
        return false;
    }

    @Override
    public boolean disconnect() {
        NsqCommand closeCommand = new NsqCloseCommand();
        NsqResponseFrame closeResponse = (NsqResponseFrame) executeCommand(closeCommand);
        if (closeResponse != null && closeResponse.getMessage().equals("CLOSE_WAIT")) {
            log.info("Cleanly close connection success.");
            return channel.disconnect().isSuccess();
        } else {
            channel.close();
            return false;
        }
    }

    @Override
    public NsqFrame executeCommand(NsqCommand command) {
        return commandExecutor.executeCommand(command);
    }

    @Override
    public void executeCommandNoResponse(NsqCommand command) {
        if (channel == null) {
            log.warn("when execute command please connect to nsq first");
            return;
        }

        this.channel.writeAndFlush(command);
    }

    private void heartBeat() {
        NsqCommand heartbeatCommand = new NsqNopCommand();
        executeCommandNoResponse(heartbeatCommand);
    }

    private void ready(int count) {
        NsqCommand readyCommand = new NsqReadyCommand(count);
        executeCommandNoResponse(readyCommand);
    }

    public void finishMessage(String messageId) {
        NsqCommand command = new NsqFinCommand(messageId);
        executeCommandNoResponse(command);
    }

    public void processNsqFrame(NsqFrame nsqFrame) {
        // response from server contains heartbeat
        if (nsqFrame instanceof NsqResponseFrame) {
            NsqResponseFrame responseFrame = (NsqResponseFrame) nsqFrame;
            String message = responseFrame.getMessage();
            if (message.equals("_heartbeat_")) {
                log.debug("response server heartbeat");
                heartBeat();
            } else {
                log.info("nsq response {}", new String(nsqFrame.getData()));
                commandExecutor.addResponse(responseFrame);
            }

        }

        if (nsqFrame instanceof NsqErrorFrame) {
            log.warn("error response {}", ((NsqErrorFrame) nsqFrame).getErrorMessage());
            if (Objects.nonNull(messageHandler)) {
                messageHandler.handleError((NsqErrorFrame) nsqFrame);
            }

            commandExecutor.addResponse(nsqFrame);
        }

        if (nsqFrame instanceof NsqMessageFrame) {
            NsqMessageFrame messageFrame = (NsqMessageFrame) nsqFrame;
            messageExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (Objects.nonNull(messageHandler)) {
                        messageHandler.handleMessage(messageFrame);
                        finishMessage(new String(messageFrame.getMessageId()));
                        ready(1);
                    }
                }
            });
        }
    }
}
