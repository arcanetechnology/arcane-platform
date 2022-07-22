FROM eclipse-temurin:18.0.1_10-jre
EXPOSE 8080:8080
RUN mkdir /app
COPY ./build/install/oauth2-provider-emulator/ /app/
WORKDIR /app/bin
CMD ["./oauth2-provider-emulator"]