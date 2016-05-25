package org.logstash.beats;

import java.util.logging.Logger;

public class Runner {
    private static final int DEFAULT_PORT = 5044;
    public static Logger logger = Logger.getLogger(Runner.class.getName());


    static public void main(String[] args) throws Exception {
        logger.info("Starting Beats Bulk");
        new Server(DEFAULT_PORT).listen();
    }
}