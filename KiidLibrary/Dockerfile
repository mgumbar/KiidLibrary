FROM java:8
VOLUME /tmp
ADD target/library-0.0.1-SNAPSHOT.jar library.jar
RUN apt-get update && apt-get -y install \
    tesseract
RUN bash -c 'touch /library.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /library.jar"]