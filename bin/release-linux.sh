#!/bin/bash
set -e

# ==========================
# Linux 发布脚本 (x64, arm64)
# 用法：
#   ./release-linux.sh [arch]
# 示例：
#   ./release-linux.sh x64
#   ./release-linux.sh arm64
# 默认：x64
# ==========================

arch="${1:-x64}"

# JRE 文件名
jre_filename="jre-21.0.8+9-${arch}_linux.tar.gz"
# 输出文件名和目录名根据架构区分
dist_filename="sonovel-linux_${arch}.tar.gz"
dist_dirname="sonovel-linux_${arch}"

# 检查 JRE 文件是否存在
if [[ ! -f "bundle/$jre_filename" ]]; then
  echo "❌ 未找到 bundle/$jre_filename"
  exit 1
fi

# 项目根路径
project_path=$(
  cd "$(dirname "$0")" || exit
  cd ..
  pwd
)
cd "$project_path" || exit

# Maven 打包
echo "🏗️ 开始 Maven 构建 ($arch)..."
mvn clean package -P"linux-${arch}" -Dmaven.test.skip=true -DjrePath=runtime

# 创建产物目录
mkdir -p dist
mkdir -p "target/$dist_dirname"

# 复制文件
cp "bundle/$jre_filename" "target/$dist_dirname"
cp -r bundle/rules "target/$dist_dirname"
cp bundle/config.ini bundle/readme.txt bundle/run-linux.sh "target/$dist_dirname"
cp "bundle/支持 & 赞助.png" "target/$dist_dirname"

# 移动 jar 包
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

echo "✅ Linux ${arch} 构建完成！产物: $dist_filename"