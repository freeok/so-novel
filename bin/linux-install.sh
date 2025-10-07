#!/bin/bash
set -e

# ==========================
# SoNovel 通用 Linux 安装脚本
# ==========================

# 获取最新版本号
LATEST=$(curl -s https://api.github.com/repos/freeok/so-novel/releases/latest | grep '"tag_name":' | cut -d '"' -f4)
echo "🔖 最新版本：$LATEST"

# 自动识别架构
ARCH=$(uname -m)
case "$ARCH" in
  x86_64)
    ARCH_TAG="x64"
    ;;
  aarch64)
    ARCH_TAG="arm64"
    ;;
  *)
    echo "❌ 不支持的架构: $ARCH"
    exit 1
    ;;
esac

URL="https://github.com/freeok/so-novel/releases/download/${LATEST}/sonovel-linux_${ARCH_TAG}.tar.gz"
TMP_FILE="/tmp/sonovel.tar.gz"
INSTALL_DIR="$HOME/SoNovel"

echo "📥 正在下载 SoNovel (${ARCH_TAG}) ..."
curl -L "$URL" -o "$TMP_FILE"

# 清理旧安装
rm -rf "$INSTALL_DIR"
mkdir -p "$INSTALL_DIR"

# 解压时去掉第一层目录（SoNovel-Linux_x64 / SoNovel-Linux_arm64）
tar -xzf "$TMP_FILE" -C "$INSTALL_DIR" --strip-components=1

echo "✅ 安装完成！"
echo "📁 安装目录: $INSTALL_DIR"

# 启动程序
cd "$INSTALL_DIR" && bash run-linux.sh