#!/bin/bash

project_path=$(
  cd "$(dirname "$0")" || exit
  cd ..
  pwd
)
cd "$project_path" || exit

mvn clean package "-Dmaven.test.skip=true"
mkdir target/jpackage
mv target/app-jar-with-dependencies.jar target/jpackage

echo "jpackage version: $(jpackage --version)"
# 在 linux 环境会转换为 .deb 文件
# type=app-image 记得去掉--about-url
jpackage \
--name "SoNovel" \
--input "target/jpackage" \
--dest dist \
--icon assets/logo-1.ico \
--app-version 1.0.0 \
--copyright "Copyright (C) 2025 SoNovel. All rights reserved." \
--description "开源搜书神器" \
--vendor "FreeOK" \
--main-jar "app-jar-with-dependencies.jar"

cp config.ini input/readme.txt dist/SoNovel

echo "开始压缩打包"
tar czvf dist/SoNovel_linux.tar.gz dist/SoNovel