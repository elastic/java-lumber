package org.logstash.beats;

import io.netty.handler.ssl.SslContext;
import io.netty.util.ResourceLeakDetector;
import org.apache.log4j.Logger;
import org.logstash.netty.PrivateKeyConverter;
import org.logstash.netty.SslSimpleBuilder;


public class Runner {
    private static final int DEFAULT_PORT = 5044;
    public static Logger logger = Logger.getLogger(Runner.class.getName());


    static public void main(String[] args) throws Exception {
        logger.info("Starting Beats Bulk");

        // TODO remove this
        // ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);

        Server server = new Server(DEFAULT_PORT);

        if(true) {
            logger.debug("Using SSL");

            String sslCertificate = "/Users/ph/es/certificates/certificate.crt";
            String sslKey = "/Users/ph/es/certificates/certificate.pkcs8.key";

            logger.debug("SSLCertificate: " + sslCertificate);
            logger.debug("SSLKey: " + sslKey);

            SslContext context = new SslSimpleBuilder(sslCertificate, sslKey)
                    .protocols(new String[] { "TLSv1.2" })
                    .setCertificateAuthorities(new String[] { sslCertificate })
                    .setVerifyMode("VERIFY_PEER")
                    .build();

            server.enableSSL(context);

        }

        server.listen();
    }
}