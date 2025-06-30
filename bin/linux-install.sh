#!/bin/bash

set -e

# 获取最新版本号
LATEST=$(curl -s https://api.github.com/repos/freeok/so-novel/releases/latest | grep '"tag_name":' | cut -d '"' -f4)
echo "🔖 最新版本：$LATEST"

URL="https://github.com/freeok/so-novel/releases/download/${LATEST}/sonovel-linux_x64.tar.gz"
TMP_FILE="/tmp/sonovel.tar.gz"
INSTALL_DIR="$HOME/SoNovel"

# 下载
echo "📥 正在下载 SoNovel..."
curl -L "$URL" -o "$TMP_FILE"

# 清理旧安装
rm -rf "$INSTALL_DIR"
mkdir -p "$INSTALL_DIR"

# 解压时去掉第一层目录（SoNovel-Linux_x64）
tar -xzf "$TMP_FILE" -C "$INSTALL_DIR" --strip-components=1

echo "✅ 安装完成！"
echo "📁 安装目录: $INSTALL_DIR"

# 运行
cd "$INSTALL_DIR" && bash run-linux.sh