package org.logstash.beats;

import org.apache.log4j.Logger;
import org.logstash.netty.SslSimpleBuilder;


public class Runner {
    private static final int DEFAULT_PORT = 5044;
    public static Logger logger = Logger.getLogger(Runner.class.getName());


    static public void main(String[] args) throws Exception {
        logger.info("Starting Beats Bulk");

        Server server = new Server(DEFAULT_PORT);

        //if(args[1] != "ssl") {
            logger.debug("Using SSL");

            String sslCertificate = "/Users/ph/es/certificates/certificate.crt";
            String sslKey = "/Users/ph/es/certificates/certificate.pkcs8.key";

            logger.debug("SSLCertificate: " + sslCertificate);
            logger.debug("SSLKey: " + sslKey);

            server.enableSSL(new SslSimpleBuilder(sslCertificate, sslKey).build());
        //}

        server.listen();
    }
}