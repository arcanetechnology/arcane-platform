FROM eclipse-temurin:18.0.2.1_1-jre
RUN apt-get update \
 && apt-get install -y --no-install-recommends \
    netcat \
 && rm -rf /var/lib/apt/lists/*
EXPOSE 8080:8080
RUN mkdir /app
COPY ./build/install/acceptance-tests/ /acceptance-tests/
COPY ./scripts/* /acceptance-tests/bin/
WORKDIR /acceptance-tests/bin
CMD ["./wait.sh"]