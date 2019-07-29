#!/bin/bash

cd "$(dirname "$0")"

sh ./check_java_version.sh

[[ $? -ne 0 ]] && exit 1

mvn clean package deploy
