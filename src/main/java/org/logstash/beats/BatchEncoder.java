package org.logstash.beats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.IOException;


public class BatchEncoder extends MessageToByteEncoder<Batch> {
    private final static ObjectMapper mapper = new ObjectMapper().registerModule(new AfterburnerModule());

    @Override
    protected void encode(ChannelHandlerContext ctx, Batch batch, ByteBuf out) throws Exception {
        out.writeByte(batch.getProtocol());
        out.writeByte('W');
        out.writeInt(batch.size());
        out.writeBytes(getPayload(ctx, batch));
    }

    protected ByteBuf getPayload(ChannelHandlerContext ctx, Batch batch) throws IOException {
        ByteBuf payload = ctx.alloc().buffer();

        // Aggregates the payload that we could decide to compress or not.
        for(Message message : batch.getMessages()) {
            payload.writeByte(batch.getProtocol());
            payload.writeByte('J');
            payload.writeInt(message.getSequence());

            byte[] json = mapper.writeValueAsBytes(message.getData());
            payload.writeInt(json.length);
            payload.writeBytes(json);
        }

        return payload;
    }
}
