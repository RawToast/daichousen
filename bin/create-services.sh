#!/bin/bash

kubectl create -f  ../kube/backend-service.yaml
kubectl create -f  ../kube/web-service.yaml

kubectl create -f  ../kube/backend-controller.yaml
kubectl create -f  ../kube/web-controller.yaml
