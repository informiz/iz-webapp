#!/usr/bin/env bash


# TODO: can get cert and key directly from secret-manager with $(gcloud secrets versions access latest --secret=...)?
openssl req -x509 -new -nodes -key devCAEncrypted.key -sha256 -days 3650 -out devCA.pem

openssl rsa -in devCAEncrypted.key -out devCA.key

kubectl create secret tls demo-channel-tls --cert devCA.pem --key devCA.key

kubectl create secret generic demo-channel-backend-cert --from-file=keystore.p12 --from-literal=password=demochan

kubectl create secret generic demo-channel-service-account-creds --from-file=key.json=/path/to/channel-service-key.json

kubectl create secret generic demo-channel-db-creds --from-literal=username=test --from-literal=password=test

kubectl create secret generic demo-channel-oauth-creds --from-literal=client=<CLIENT_ID> --from-literal=secret=<CLINET_SERVICE>



sed -i "s/<PROFILE>/test/g" deploy-test/iz-deployment.yaml
sed -i "s/<CHANNEL_NAME>/demo-channel/g" deploy-test/*.yaml


kubectl apply -f iz-cert.yaml -n iz-beta-namespace

kubectl apply -f iz-deployment.yaml -n iz-beta-namespace

kubectl apply -f iz-webapp-service.yaml -n iz-beta-namespace

kubectl apply -f iz-webapp-ingress.yaml -n iz-beta-namespace



