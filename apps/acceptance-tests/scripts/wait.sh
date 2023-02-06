#!/usr/bin/env bash

echo "Waiting for firestore-emulator to launch on 5173..."
while ! nc -z firestore-emulator 5173; do
  sleep 0.1 # wait for 1/10 of the second before check again
done

echo "Waiting for k33-backend to launch on 8080..."
while ! nc -z k33-backend 8080; do
  sleep 0.1 # wait for 1/10 of the second before check again
done

echo "Waiting for backend: $BACKEND_HOST to launch on 8080..."
while ! nc -z "$BACKEND_HOST" 8080; do
  sleep 0.1 # wait for 1/10 of the second before check again
done

./acceptance-tests --select-package=com.k33.platform.tests