#!/bin/bash

cd "$(dirname "$0")" || exit
source "lib_certs.sh"
cd ..

create_root_keystore \
  "crypto/root.p12" "arrowhead.eu"

create_cloud_keystore \
  "crypto/root.p12" "arrowhead.eu" \
  "crypto/kalix-example.p12" "kalix-example.ltu.arrowhead.eu"

# Echo system

create_system_keystore \
  "crypto/root.p12" "arrowhead.eu" \
  "crypto/kalix-example.p12" "kalix-example.ltu.arrowhead.eu" \
  "crypto/system-keystore.p12" "echo-system.kalix-example.ltu.arrowhead.eu" \
  "dns:localhost,ip:127.0.0.1"

create_truststore \
  "crypto/system-truststore.p12" \
  "crypto/root.crt" "arrowhead.eu"

# Echo client

create_system_keystore \
  "crypto/root.p12" "arrowhead.eu" \
  "crypto/kalix-example.p12" "kalix-example.ltu.arrowhead.eu" \
  "crypto/client-keystore.p12" "echo-client.kalix-example.ltu.arrowhead.eu" \
  "dns:localhost,ip:127.0.0.1"

create_truststore \
  "crypto/client-truststore.p12" \
  "crypto/root.crt" "arrowhead.eu"
