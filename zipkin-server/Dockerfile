FROM java:8
VOLUME /tmp
ADD zipkin-server-2.9.4-exec.jar zipkin-server-2.9.4-exec.jar
RUN bash -c 'touch /zipkin-server-2.9.4-exec.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /zipkin-server-2.9.4-exec.jar"]
