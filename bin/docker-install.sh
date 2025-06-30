#!/bin/bash
# ====================================================
# 于 Ubuntu 24 测试通过
# 执行前请确保下载链接的可访问性！建议开启 🪜 或使用 GitHub、Docker 镜像加速
# ====================================================

set -e  # 出错即退出
set -o pipefail  # 管道中的任何命令失败都会导致脚本退出

# 获取最新版本号
LATEST_VERSION=$(curl -s https://api.github.com/repos/freeok/so-novel/releases/latest | grep '"tag_name":' | cut -d '"' -f4)
echo "🔖 最新版本：$LATEST_VERSION"

APP_NAME="sonovel"
TAR_NAME="${APP_NAME}-linux_x64.tar.gz"
DIR_NAME="SoNovel-Linux_x64"
IMAGE_NAME="sonovel:${LATEST_VERSION}"

# 函数：下载文件
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
mv Dockerfile "${DIR_NAME}"
cd "${DIR_NAME}"

echo "📁 准备宿主机挂载目录..."
sudo mkdir -p /sonovel/downloads

# 如果宿主机 config.ini 不存在，就复制它；否则保留用户已有配置
if [ ! -f /sonovel/config.ini ]; then
  sudo cp config.ini /sonovel/config.ini
else
  echo "⚠️ /sonovel/config.ini 已存在，跳过复制。"
fi

echo "🐳 构建 Docker 镜像: ${IMAGE_NAME} ..."
# 构建 Docker 镜像
if ! docker build -t "${IMAGE_NAME}" .; then
  echo "❌ Docker 镜像构建失败！"
  exit 1
fi

echo "🚀 手动执行以下命令启动容器."
echo "docker run -it --rm \
-v /sonovel/config.ini:/sonovel/config.ini \
-v /sonovel/downloads:/sonovel/downloads \
${IMAGE_NAME} bash"