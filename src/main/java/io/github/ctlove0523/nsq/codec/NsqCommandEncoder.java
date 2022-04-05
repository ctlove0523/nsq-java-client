package io.github.ctlove0523.nsq.codec;

import io.github.ctlove0523.nsq.cmd.NsqCommand;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class NsqCommandEncoder extends MessageToMessageEncoder<NsqCommand> {

    @Override
    protected void encode(ChannelHandlerContext ctx, NsqCommand cmd, List<Object> out) throws Exception {
        List<String> commandLineParts = new ArrayList<>();
        commandLineParts.add(cmd.commandName().cmdName());
        commandLineParts.addAll(cmd.commandParams());
        commandLineParts.add("\n");
        String commandLine = String.join(" ", commandLineParts);

        ByteBuf buf = Unpooled.buffer();
        buf.writeBytes(commandLine.getBytes(StandardCharsets.UTF_8));

        // Publish multiple messages to a topic (atomically)
        if (cmd.commandBody().size() > 1) {
            // write total body size and message size
            int bodySize = 4; //4 for total messages int.
            for (byte[] messageBody : cmd.commandBody()) {
                bodySize += 4; //message size
                bodySize += messageBody.length;
            }
            buf.writeInt(bodySize);
            buf.writeInt(cmd.commandBody().size());
        }

        for (byte[] data : cmd.commandBody()) {
            buf.writeInt(data.length);
            buf.writeBytes(data);
        }

        out.add(buf);
    }
}
