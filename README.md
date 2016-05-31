# Work in progress, NOT READY FOR USE

This is WIP to rewrite the lumberjack protocol in Java+Netty, this protocol is used by beats and logstash-forwarder.

### Goals
- Support V1 (LSF)
- Support V2 (libbeat)
- Use non-blocking IO with Netty
- Don't use threads for each connection.
- Remove the need to have a circuit breaker to better communicate back pressure downstream.
- pipelining
- Make it a lot faster, initial test shows and improvements from 22k EPS to 100k EPS.

