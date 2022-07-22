FROM eclipse-temurin:18.0.1_10-jre
EXPOSE 8080:8080
RUN mkdir /app
COPY ./build/install/arcane-platform-app/ /app/
WORKDIR /app/bin
CMD ["./arcane-platform-app"]