package org.logstash.beats;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.apache.log4j.Logger;
import org.apache.tomcat.jni.SSL;
import org.logstash.netty.SslSimpleBuilder;

import javax.net.ssl.SSLException;


public class Server {
    static final Logger logger = Logger.getLogger(Server.class.getName());

    private int port;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workGroup;
    private IMessageListener messageListener = new MessageListener();
    private SslSimpleBuilder sslBuilder;

    public Server(int p) {
        port = p;
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();
    }

    public void enableSSL(SslSimpleBuilder builder) {
        sslBuilder = builder;
    }

    public Server listen() throws InterruptedException {
        try {
            logger.info("Starting server listing port: " + this.port);

            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler())
                    .childHandler(new BeatsInitializer(this));

            Channel channel = server.bind(port).sync().channel();

            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

        return this;
    }

    public void setMessageListener(IMessageListener listener) {
        messageListener = listener;
    }

    public boolean isSslEnable() {
        if(this.sslBuilder != null) {
            return true;
        } else {
            return false;
        }
    }

    private class BeatsInitializer extends ChannelInitializer<SocketChannel> {
        private final String LOGGER_HANDLER = "logger";
        private final String SSL_HANDLER = "ssl-handler";
        private final String KEEP_ALIVE_HANDLER = "keep-alive-handler";
        private final String BEATS_PARSER = "beats-parser";
        private final String BEATS_HANDLER = "beats-handler";
        private final int DEFAULT_IDLESTATEHANDLER_THREAD = 4;
        private final EventExecutorGroup idleExecutorGroup = new DefaultEventExecutorGroup(DEFAULT_IDLESTATEHANDLER_THREAD);
        private final BeatsHandler beatsHandler;
        private final LoggingHandler loggingHandler = new LoggingHandler();
        private final Server server;

        public BeatsInitializer(Server s) {
            server = s;
            beatsHandler = new BeatsHandler(server.messageListener);
        }

        public void initChannel(SocketChannel socket) throws SSLException {
            ChannelPipeline pipeline = socket.pipeline();

            pipeline.addLast(LOGGER_HANDLER, loggingHandler);

            if(server.isSslEnable()) {
                SslHandler sslHandler = sslBuilder.build(socket.alloc());
                pipeline.addLast(SSL_HANDLER, sslHandler);
            }

            // We have set a specific executor for the idle check, because the `beatsHandler` can be
            // blocked on the queue, this the idleStateHandler manage the `KeepAlive` signal.
            pipeline.addLast(idleExecutorGroup, KEEP_ALIVE_HANDLER, new IdleStateHandler(60*15, 5, 0));
            pipeline.addLast(BEATS_PARSER, new BeatsParser());
            pipeline.addLast(BEATS_HANDLER, this.beatsHandler);
        }
    }
}
