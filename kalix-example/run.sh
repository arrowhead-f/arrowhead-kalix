#!/usr/bin/env bash

../gradlew distZip

yes 'A' | unzip "./build/distributions/*.zip" -d ./build/distributions-unzip

CP="$(find . -wholename "./build/distributions-unzip/*/lib/*" | tr '\n' ':')"

java -cp "${CP}" se.arkalix.example.EchoSystem \
  ./crypto/system-keystore.p12 ./crypto/system-truststore.p12 &

sleep 1s

java -cp "${CP}" se.arkalix.example.EchoClient \
  ./crypto/client-keystore.p12 ./crypto/client-truststore.p12 &

wait %1 %2

echo "Done!"