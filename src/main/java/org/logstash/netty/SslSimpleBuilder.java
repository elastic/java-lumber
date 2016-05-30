package org.logstash.netty;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.io.File;
import java.nio.file.FileSystem;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ph on 2016-05-27.
 */
public class SslSimpleBuilder {
    public static Logger logger = Logger.getLogger(SslSimpleBuilder.class.getName());

    private final File sslKeyFile;
    private final File sslCertificateFile;
    private final boolean insecure = false;


    // TODO default to TLSv1.2
    // TODO DEFAULT TO THE MODERN MOZILLA CIPHERS LIST.
    // TODO LSF MIGHT NEED TO CHANGES STUFF IN THEIR CONFIG.
    // TODO Mutual auth.

    // TODO disable local trust store by default.
    // This cipher selection comes from https://wiki.mozilla.org/Security/Server_Side_TLS
    // TODO cypher_suites
    // I think I would be in favor of using the defaults?
    // LOOK at the IANA column on the mozilla page.
    private final String[] DEFAULT_CIPHERS = new String[] {
            "ECDHE-RSA-AES128-GCM-SHA256",
            "ECDHE-ECDSA-AES128-GCM-SHA256",
            "ECDHE-RSA-AES256-GCM-SHA384",
            "ECDHE-ECDSA-AES256-GCM-SHA384",
            "DHE-RSA-AES128-GCM-SHA256",
            "DHE-DSS-AES128-GCM-SHA256",
            "kEDH+AESGCM",
            "ECDHE-RSA-AES128-SHA256",
            "ECDHE-ECDSA-AES128-SHA256",
            "ECDHE-RSA-AES128-SHA",
            "ECDHE-ECDSA-AES128-SHA",
            "ECDHE-RSA-AES256-SHA384",
            "ECDHE-ECDSA-AES256-SHA384",
            "ECDHE-RSA-AES256-SHA",
            "ECDHE-ECDSA-AES256-SHA",
            "DHE-RSA-AES128-SHA256",
            "DHE-RSA-AES128-SHA",
            "DHE-DSS-AES128-SHA256",
            "DHE-RSA-AES256-SHA256",
            "DHE-DSS-AES256-SHA",
            "DHE-RSA-AES256-SHA",
            "AES128-GCM-SHA256",
            "AES256-GCM-SHA384",
            "AES128-SHA256",
            "AES256-SHA256",
            "AES128-SHA",
            "AES256-SHA",
            "AES",
            "CAMELLIA",
            "DES-CBC3-SHA",
            "!aNULL",
            "!eNULL",
            "!EXPORT",
            "!DES",
            "!RC4",
            "!MD5",
            "!PSK",
            "!aECDH",
            "!EDH-DSS-DES-CBC3-SHA",
            "!EDH-RSA-DES-CBC3-SHA",
            "!KRB5-DES-CBC3-SHA"
    };
    private String[] protocols = new String[] { "TLSv1", "TLSv1.1", "TLSv1.2" };

    // TODO TLS, lets try to make sure we use similar or the same name for the option if possible.
    // https://www.elastic.co/guide/en/beats/filebeat/current/configuration-output-tls.html#_max_version
    // https://www.elastic.co/guide/en/beats/filebeat/current/configuration-output-tls.html#_min_version


    // TODO insecure.


    // TODO, check if we can convert x509 to PKCS8 on the fly.
    // because netty sslcontext builder only support pkcs8 for the private key ..
    // openssl pkcs8 -topk8 -nocrypt -in pkcs1_key_file -out pkcs8_key.pem
    // http://netty.io/wiki/sslcontextbuilder-and-private-key.html
    public SslSimpleBuilder(String sslCertificateFilePath, String sslKeyFilePath) {
        this.sslCertificateFile = this.createFile(sslCertificateFilePath);
        this.sslKeyFile = this.createFile(sslKeyFilePath);
    }

    public SslSimpleBuilder protocols(String[] protocols) {
        this.protocols = protocols;
        return this;
    }

    public SslContext build() throws SSLException {
        SslContextBuilder sslBuilder = SslContextBuilder.forServer(this.sslCertificateFile, this.sslKeyFile);
        sslBuilder.ciphers(Arrays.asList(DEFAULT_CIPHERS));
        SslContext context = sslBuilder.build();

        logger.debug(context.cipherSuites());

        return context;
    }

    private File createFile(String filepath) {
        return new File(filepath);
    }

}