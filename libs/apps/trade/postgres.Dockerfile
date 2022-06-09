FROM postgres:alpine

COPY src/main/resources/schema.ddl /docker-entrypoint-initdb.d/init.sql