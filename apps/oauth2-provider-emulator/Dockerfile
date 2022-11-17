FROM eclipse-temurin:18.0.2.1_1-jre
EXPOSE 8080:8080
RUN mkdir /app
COPY ./build/install/oauth2-provider-emulator/ /app/
WORKDIR /app/bin
CMD ["./oauth2-provider-emulator"]