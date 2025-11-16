#!/bin/bash

cd api-gateway/
docker build -t api-gateway:latest .

cd ..
cd auth-service/
docker build -t auth-service:latest .

cd ..
cd catalog-service/
docker build -t catalog-service:latest .

cd ..
cd eureka-service/
docker build -t eureka-service:latest .

cd ..
cd favorite-service/
docker build -t favorite-service:latest .

cd ..
cd history-service/
docker build -t history-service:latest .

cd ..
cd playing-service/
docker build -t playing-service:latest .

cd ..
cd recommend-service/
docker build -t recommend-service:latest .

cd ..
cd rec-sys/
docker build -t rec-sys:latest .

cd ..
cd user-service/
docker build -t user-service:latest .

#cd ..
#cd infrastructure/
#chmod +x localstack-deploy.sh
#./localstack-deploy.sh
