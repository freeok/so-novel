FROM openjdk:17-jdk-slim

WORKDIR /sonovel

COPY app.jar config.ini /sonovel/

ENTRYPOINT [ \
"java", \
"-Dfile.encoding=UTF-8", \
"-Duser.timezone=GMT+08", \
"-jar", \
"app.jar" \
]