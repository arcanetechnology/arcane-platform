#!/usr/bin/env bash

echo "Waiting for firestore-emulator to launch on 5173..."
while ! nc -z firestore-emulator 5173; do
  sleep 0.1 # wait for 1/10 of the second before check again
done

echo "Waiting for arcane-platform-app to launch on 8080..."
while ! nc -z arcane-platform-app 8080; do
  sleep 0.1 # wait for 1/10 of the second before check again
done

echo "Waiting for backend: $BACKEND_HOST to launch on 8080..."
while ! nc -z "$BACKEND_HOST" 8080; do
  sleep 0.1 # wait for 1/10 of the second before check again
done

./acceptance-tests --select-package=no.arcane.platform.tests