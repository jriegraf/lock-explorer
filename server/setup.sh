#!/usr/bin/env bash

apt update
apt install -y nginx pwgen

# Login to Github Container Registry
# echo $TOKEN | docker login https://docker.pkg.github.com -u jriegraf --password-stdin

# check if docker is available
if ! command -v docker &> /dev/null
then
    echo "--- Installing docker ---"
    curl -fsSL https://get.docker.com -o get-docker.sh
    sh get-docker.sh
fi

# check if docker-compose is available
if ! command -v docker-compose &> /dev/null
then
    echo "--- Installing docker-compose ---"
    curl -L "https://github.com/docker/compose/releases/download/1.27.4/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
fi

curl -L -o docker-compose.yml https://raw.githubusercontent.com/jriegraf/lock-explorer/main/server/docker-compose.yml
curl -L -o create_user.sql https://raw.githubusercontent.com/jriegraf/lock-explorer/main/server/create_user.sql
curl -L -o .env https://raw.githubusercontent.com/jriegraf/lock-explorer/main/server/.env

# Update all images
# docker images | grep -v REPOSITORY | awk '{print $1}' | xargs -L1 docker pull

ORACLE_PASSWORD="$(pwgen 14 1)"
DATABASE_PASSWORD="$(pwgen 14 1)"

sed -i "s/<<ORACLE_PASSWORD>>/$ORACLE_PASSWORD/g" .env
sed -i "s/<<DATABASE_PASSWORD>>/$DATABASE_PASSWORD/g" .env
sed -i "s/<<DATABASE_PASSWORD>>/$DATABASE_PASSWORD/g" create_user.sql

mkdir -p database-scripts
mv create_user.sql database-scripts/



docker-compose up
