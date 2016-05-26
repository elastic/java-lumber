package org.logstash.beats;

public class Payload {
        private int protocol;
        private long windowSize;

        public Payload() {        }

        public int getProtocol() {
            return protocol;
        }

        public void setProtocol(int protocol) {
            this.protocol = protocol;
        }

        public long getWindowSize() {
            return windowSize;
        }

        public void setWindowSize(long windowSize) {
        this.windowSize = windowSize;
    }
}