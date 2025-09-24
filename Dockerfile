FROM eclipse-temurin:21-jre-jammy

WORKDIR /sonovel

COPY app.jar /sonovel/
COPY config.ini /sonovel/
COPY rules /sonovel/rules

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS \
  -XX:+UseZGC \
  -XX:+ZGenerational \
  -Dfile.encoding=UTF-8 \
  -Duser.timezone=GMT+08 \
  -jar app.jar"]