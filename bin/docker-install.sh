#!/bin/bash
# ====================================================
# SoNovel 通用 Docker 安装脚本
# 执行前请确保下载链接的可访问性！建议开启 🪜 或使用 GitHub、Docker 镜像加速
# ====================================================

set -e
set -o pipefail

# 获取最新版本号
LATEST_VERSION=$(curl -s https://api.github.com/repos/freeok/so-novel/releases/latest | grep '"tag_name":' | cut -d '"' -f4)
echo "🔖 最新版本：$LATEST_VERSION"

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

APP_NAME="sonovel"
TAR_NAME="${APP_NAME}-linux_${ARCH_TAG}.tar.gz"
DIR_NAME="sonovel-linux_${ARCH_TAG}"
IMAGE_NAME="sonovel:${LATEST_VERSION#v}"

# 下载函数
download_file() {
  local url=$1
  local output=$2
  echo "📥 下载文件: ${url} ..."
  if ! wget -q --show-progress -O "$output" "$url"; then
    echo "❌ 下载失败: ${url}"
    exit 1
  fi
}

# 下载 release 文件
download_file "https://github.com/freeok/so-novel/releases/download/${LATEST_VERSION}/${TAR_NAME}" "$TAR_NAME"
download_file "https://raw.githubusercontent.com/freeok/so-novel/main/Dockerfile" "Dockerfile"

echo "📦 解压文件..."
tar -zxf "${TAR_NAME}"

echo "📂 准备构建目录..."
cd "${DIR_NAME}"

echo "📁 准备宿主机挂载目录..."
sudo mkdir -p /sonovel/downloads
sudo cp -r ./rules /sonovel/

# 如果宿主机 config.ini 不存在，就复制它
if [ ! -f /sonovel/config.ini ]; then
  sudo cp config.ini /sonovel/config.ini
else
  echo "⚠️ /sonovel/config.ini 已存在，跳过复制。"
fi

echo "🐳 构建 Docker 镜像: ${IMAGE_NAME} ..."
if ! docker build -f ../Dockerfile -t "${IMAGE_NAME}" .; then
  echo "❌ Docker 镜像构建失败！"
  exit 1
fi

echo "✅ Docker 镜像构建完成 (${ARCH_TAG})"

echo "🚀 Web 模式请手动执行以下命令启动容器:"
echo "docker run -d \
--name sonovel-web \
-v /sonovel/config.ini:/sonovel/config.ini \
-v /sonovel/downloads:/sonovel/downloads \
-v /sonovel/rules:/sonovel/rules \
-p 7765:7765 \
-e JAVA_OPTS='-Dmode=web' \
${IMAGE_NAME}"

echo ""
echo "🚀 TUI 模式请手动执行以下命令启动容器:"
echo "docker run -it --rm \
-v /sonovel/config.ini:/sonovel/config.ini \
-v /sonovel/downloads:/sonovel/downloads \
-v /sonovel/rules:/sonovel/rules \
-e JAVA_OPTS='-Dmode=tui' \
${IMAGE_NAME}"