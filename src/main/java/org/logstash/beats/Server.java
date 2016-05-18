package org.logstash.beats;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.log4j.Logger;

public class Server {
    private int port;

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workGroup;

    private MessageListener messageListener = new MessageListener();

    static Logger logger = Logger.getLogger(Server.class.getName());

    public Server(int port) {
        this.port = port;
        this.bossGroup = new NioEventLoopGroup();
        this.workGroup = new NioEventLoopGroup();
    }

    public Server listen() throws InterruptedException {
        try {
            logger.info("Starting server listing port: " + this.port);

            ServerBootstrap server = new ServerBootstrap();
            server.group(this.bossGroup, this.workGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler())
                    .childHandler(new BeatsInitializer(this.messageListener));

            Channel channel = server.bind(this.port).sync().channel();

            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

        return this;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    private class BeatsInitializer extends ChannelInitializer<SocketChannel> {
        private final BeatsHandler beatsHandler;
        private final LoggingHandler loggingHandler = new LoggingHandler();

        public BeatsInitializer(MessageListener messageListener) {
            this.beatsHandler = new BeatsHandler(messageListener);
        }

        public void initChannel(SocketChannel socket) {
            ChannelPipeline pipeline = socket.pipeline();

            pipeline.addLast("logger", this.loggingHandler);
            pipeline.addLast("keep-alive-handler", new IdleStateHandler(60*15, 5, 0));
            pipeline.addLast("beats-parser", new BeatsParser());
            pipeline.addLast("beats-handler", this.beatsHandler);
        }
    }
}
