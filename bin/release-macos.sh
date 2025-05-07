#!/bin/bash
set -e

# macOS 发布脚本 (arm64, x64)
# JDK 升级后需要修改下面 3 个版本号

# JRE 文件名
jre_filename_arm64="jre-17.0.11+9-arm64_mac.tar.gz"
jre_filename_x64="jre-17.0.11+9-x64_mac.tar.gz"
# JRE 解压后的目录名
jre_dirname="jdk-17.0.11+9-jre"

# 最终产物名
dist_filename_arm64="sonovel-macos_arm64.tar.gz"
dist_dirname_arm64="SoNovel-macOS_arm64"
dist_filename_x64="sonovel-macos_x64.tar.gz"
dist_dirname_x64="SoNovel-macOS_x64"

# 自动定位项目根目录
project_path=$(cd "$(dirname "$0")" || exit; cd ..; pwd)
cd "$project_path" || exit

# 读取架构参数
arch="$1"
if [[ "$arch" == "x64" ]]; then
  profile="macos-x86_64"
  jre_filename="$jre_filename_x64"
  dist_filename="$dist_filename_x64"
  dist_dirname="$dist_dirname_x64"
else
  arch="arm64" # 默认
  profile="macos-arm64"
  jre_filename="$jre_filename_arm64"
  dist_filename="$dist_filename_arm64"
  dist_dirname="$dist_dirname_arm64"
fi

echo "👉 打包 macOS [$arch]..."

# Maven 打包
mvn clean package -Dmaven.test.skip=true -P$profile -DjrePath=runtime

# 准备目录
mkdir -p dist
mkdir -p "target/$dist_dirname"

# 复制配置文件、说明、脚本、字体
cp config.ini bundle/readme.txt bundle/run-macos.sh "bundle/$jre_filename" "target/$dist_dirname"
cp -r bundle/fonts "target/$dist_dirname"

# 复制 jar
cd target
mv app-jar-with-dependencies.jar app.jar || true  # 忽略已改名的情况
cp app.jar "$dist_dirname"

# 解压 JRE
cd "$dist_dirname"
tar zxf "$jre_filename" && rm "$jre_filename"
mv "$jre_dirname" runtime
cd ..

# 打包 tar.gz
tar czf "$dist_filename" "$dist_dirname"
mv "$dist_filename" "$project_path/dist"

echo "✅ macOS [$arch] 打包完成！输出: dist/$dist_filename"