#!/bin/bash

docker pull yourdockerhubuser/finance-system:latest
docker stop finance-system || true
docker rm finance-system || true
docker run -d -p 80:8080 --name finance-system yourdockerhubuser/finance-system:latest
