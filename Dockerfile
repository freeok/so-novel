FROM eclipse-temurin:17-jre-jammy

WORKDIR /sonovel

COPY app.jar /sonovel/
COPY config.ini /sonovel/
COPY rules /sonovel/rules
COPY fonts /sonovel/fonts

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dfile.encoding=UTF-8 -Duser.timezone=GMT+08 -jar app.jar"]