#!/usr/bin/env bash

# TODO: can get cert and key directly from secret-manager with $(gcloud secrets versions access latest --secret=...)?
kubectl create secret tls ingress-cert-key \
  --cert cert-file --key key-file

kubectl apply -f iz-deployment.yaml

kubectl apply -f iz-webapp-service.yaml

kubectl apply -f iz-webapp-ingress.yaml

