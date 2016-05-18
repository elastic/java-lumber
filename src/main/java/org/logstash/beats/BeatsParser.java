package org.logstash.beats;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.log4j.Logger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;


public class BeatsParser extends ByteToMessageDecoder {
    private static final int CHUNK_SIZE = 1024;

    private final static Logger logger = Logger.getLogger(Server.class.getName());
    private final ObjectMapper mapper = new ObjectMapper();
    private final Inflater decompresser = new Inflater();

    private enum States {
        READ_HEADER,
        READ_FRAME_TYPE,
        READ_WINDOW_SIZE,
        READ_JSON_HEADER,
        READ_COMPRESSED_FRAME_HEADER,
        READ_COMPRESSED_FRAME,
        READ_JSON,
    }

    private States currentState = States.READ_HEADER;
    private long requiredBytes = 0;
    private Payload payload = new Payload();
    private int sequence = 0;

    public BeatsParser() {
        super();
        this.reset();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // Gather a bit of information to be used as part of the identity stream.
       ///this.remoteAddress = ctx.channel().remoteAddress().toString();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(!hasEnoughBytes(in)) {
            return;
        }

        switch (currentState) {
            case READ_HEADER: {
                logger.debug("Running: READ_HEADER");

                byte currentVersion = in.readByte();

                if(Protocol.isVersion2(currentVersion)) {
                    logger.debug("Frame version 2 detected");
                    payload.setProtocol(Protocol.VERSION_2);
                } else {
                    logger.debug("Frame version 1 detected");
                    payload.setProtocol(Protocol.VERSION_1);
                }

                transition(States.READ_FRAME_TYPE, 1);
                break;
            }
            case READ_FRAME_TYPE: {
                logger.debug("Running: READ_FRAME_TYPE");
                byte frameType = in.readByte();

                switch(frameType) {
                    case Protocol.CODE_WINDOW_SIZE: {
                        transition(States.READ_WINDOW_SIZE, 4);
                        break;
                    }
                    case Protocol.CODE_JSON_FRAME: {
                        // Reading Sequence + size of the payload
                        transition(States.READ_JSON_HEADER, 8);
                        break;
                    }
                    case Protocol.CODE_COMPRESSED_FRAME: {
                        transition(States.READ_COMPRESSED_FRAME_HEADER, 4);
                        break;
                    }
                }
                break;
            }
            case READ_WINDOW_SIZE: {
                logger.debug("Running: READ_WINDOW_SIZE");

                long windowSize = in.readUnsignedInt();
                logger.debug("Window size:" + windowSize);
                payload.setWindowSize(windowSize);

                resetSequence();
                transitionToReadHeader();
                break;
            }
            case READ_JSON_HEADER: {
                logger.debug("Running: READ_JSON_HEADER");

                this.sequence = (int) in.readUnsignedInt();
                int jsonPayloadSize = (int) in.readUnsignedInt();

                transition(States.READ_JSON, jsonPayloadSize);
                break;
            }
            case READ_COMPRESSED_FRAME_HEADER: {
                logger.debug("Running: READ_COMPRESSED_FRAME_HEADER");

                transition(States.READ_COMPRESSED_FRAME, in.readUnsignedInt());
                break;
            }

            case READ_COMPRESSED_FRAME: {
                logger.debug("Running: READ_COMPRESSED_FRAME");


                byte[] bytes = new byte[(int) this.requiredBytes];
                in.readBytes(bytes);

                InputStream inflater = new InflaterInputStream(new ByteArrayInputStream(bytes));
                ByteArrayOutputStream decompressed = new ByteArrayOutputStream();

                byte[] chunk = new byte[CHUNK_SIZE];
                int length = 0;

                while ((length = inflater.read(chunk)) > 0) {
                    decompressed.write(chunk, 0, length);
                }

                inflater.close();
                decompressed.close();

                transitionToReadHeader();
                ByteBuf newInput = Unpooled.wrappedBuffer(decompressed.toByteArray());
                while(newInput.readableBytes() > 0) {
                    decode(ctx, newInput, out);
                }

                break;
            }
            case READ_JSON: {
                logger.debug("Running: READ_JSON");
                ByteBufInputStream input = new ByteBufInputStream(in.readBytes((int) this.requiredBytes));
                String line = input.readLine();
                logger.debug("Contents: " + line);

                Message message = new Message(payload, sequence, (Map) mapper.readValue(line, Object.class));

                out.add(message);


                transitionToReadHeader();
                break;
            }
        }
    }

    private boolean hasEnoughBytes(ByteBuf in) {
        if(in.readableBytes() >= this.requiredBytes) {
            return true;
        }

        return false;
    }

    public void transitionToReadHeader() {
        transition(States.READ_HEADER, 1);
    }

    public void transition(States next, long need) {
        logger.debug("Transition, from: " + this.currentState + " to: " + next + " required bytes: " + need);
        this.currentState = next;
        this.requiredBytes = need;
    }

    public void reset() {
        this.payload = new Payload();
        this.requiredBytes = 0;
        transitionToReadHeader();
    }

    public void resetSequence() {
        this.sequence = 0;
    }
}
