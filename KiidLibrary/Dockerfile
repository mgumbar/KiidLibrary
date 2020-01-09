FROM java:8
VOLUME /tmp
ADD target/library-0.0.1-SNAPSHOT.jar library.jar
RUN apt-get -q -y install libleptonica-dev
RUN apt-get -q -y install libtesseract3 libtesseract-dev

RUN apt-get -q -y install tesseract-ocr
RUN apt-get -q -y install tesseract-ocr-eng tesseract-ocr-fra tesseract-ocr-nld

RUN bash -c 'touch /library.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /library.jar"]