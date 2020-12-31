#!/usr/bin/env bash

apt update
apt install nginx

# check if docker is available
if ! command -v docker &> /dev/null
then
    curl -fsSL https://get.docker.com -o get-docker.sh
    sh get-docker.sh
fi

# check if docker-compose is available
if ! command -v docker-compose &> /dev/null
then
    curl -L "https://github.com/docker/compose/releases/download/1.27.4/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
fi

curl -L -o docker-compose.yml https://raw.githubusercontent.com/jriegraf/lock-explorer/main/server/docker-compose.yml
curl -L -o create_user.sql https://raw.githubusercontent.com/jriegraf/lock-explorer/main/server/create_user.sql
curl -L -o .env https://raw.githubusercontent.com/jriegraf/lock-explorer/main/server/create_user.sql



# Update all images
docker images | grep -v REPOSITORY | awk '{print $1}' | xargs -L1 docker pull


# docker-compose up --no-recreate
