#!/bin/bash
set -e

# Linux 发布脚本 (x86_64)

# 最小 JRE，JDK 升级后要修改文件名版本号
jre_filename="jre-21.0.8+9-x64_linux.tar.gz"
# 最终产物
dist_filename="sonovel-linux_x64.tar.gz"
dist_dirname="SoNovel-Linux_x64"

# 项目根路径
project_path=$(
  cd "$(dirname "$0")" || exit
  cd ..
  pwd
)
cd "$project_path" || exit

# Maven 打包
mvn clean package -Plinux-x86_64 '-Dmaven.test.skip=true' '-DjrePath=runtime'

# 创建产物目录
mkdir -p dist
mkdir -p "target/$dist_dirname"

cp "bundle/$jre_filename" "target/$dist_dirname"
cp -r bundle/rules "target/$dist_dirname"
cp bundle/config.ini bundle/readme.txt bundle/run-linux.sh "target/$dist_dirname"
cp "bundle/支持 & 赞助.png" "target/$dist_dirname"

cd target
mv app-jar-with-dependencies.jar app.jar
cp app.jar "$dist_dirname"

# 解压 JRE
cd "$dist_dirname"
tar zxf "$jre_filename" && rm "$jre_filename"
cd ..

# 打包压缩
tar czf "$dist_filename" "$dist_dirname"
mv "$dist_filename" "$project_path/dist"

echo "✅ Linux x64 构建完成！产物: $dist_filename"