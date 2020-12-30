#!/usr/bin/env bash

sudo apt update
sudo apt install nginx

curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh