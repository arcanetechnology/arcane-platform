FROM eclipse-temurin:19.0.2_7-jre
EXPOSE 8080:8080
RUN mkdir /app
COPY ./build/install/oauth2-provider-emulator/ /app/
WORKDIR /app/bin
CMD ["./oauth2-provider-emulator"]