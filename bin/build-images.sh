#!/bin/bash

eval $(minikube docker-env)

docker build ../chousen -t daichousen:server

docker build ../frontend -t daichousen:frontend