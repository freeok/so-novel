#!/bin/bash
set -e

# Linux 发布脚本 (x86_64)
# JDK 升级后需要修改下面 2 个版本号

# JRE 文件
jre_filename="jre-17.0.12+7_linux.tar.gz"
jre_dirname="jdk-17.0.12+7-jre"

# 最终产物
dist_filename="sonovel-linux.tar.gz"
dist_dirname="SoNovel-linux"

# 项目根路径
project_path=$(
  cd "$(dirname "$0")" || exit
  cd ..
  pwd
)
cd "$project_path" || exit

# Maven 打包
mvn clean package -Plinux-x86_64 -Dmaven.test.skip=true -DjrePath=runtime

# 创建产物目录
mkdir -p dist
mkdir -p "target/$dist_dirname"

# 复制配置、说明、字体、JRE
cp config.ini bundle/readme.txt bundle/run-linux.sh "bundle/$jre_filename" "target/$dist_dirname"
cp -r bundle/fonts "target/$dist_dirname"

cd target
mv app-jar-with-dependencies.jar app.jar
cp app.jar "$dist_dirname"

# 解压 JRE 并改名
cd "$dist_dirname"
tar zxf "$jre_filename" && rm "$jre_filename"
mv "$jre_dirname" runtime
cd ..

# 打包压缩
tar czf "$dist_filename" "$dist_dirname"
mv "$dist_filename" $project_path/dist

echo Linux done!