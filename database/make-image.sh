#!/bin/bash

git clone --depth 1 git@github.com:oracle/docker-images.git
cd ~/oracledb/docker-images/OracleDatabase/SingleInstance/dockerfiles
sudo ./buildDockerImage.sh -v 18.4.0 -x
