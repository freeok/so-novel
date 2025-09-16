# So Novel

<div align="center">
  <img src="assets/logo.png" alt="logo" style="width: 128px;">
</div>

## 概述

**So Novel** 是一款通用的网页内容处理与导出工具，它致力于帮助用户高效地从网页中提取结构化信息，并将其灵活导出为
EPUB、TXT、PDF 等多种标准电子文档格式。适用于学习采集、格式转换、电子书制作等场景。

## TUI 预览 (Text-based User Interface)

![preview-tui.png](assets/preview-tui.png)

## WebUI 预览 (网页版)

![preview-webui.png](assets/preview-webui.png)

## CLI 预览 (Command Line Interface)

![preview-cli.png](assets/preview-cli.png)

## 使用

### 📦 普通安装

1. 下载最新版 https://github.com/freeok/so-novel/releases
2. 根据 [readme.txt](bundle%2Freadme.txt) 使用

### 🍨 Scoop

```bash
scoop bucket add freeok https://github.com/freeok/scoop-bucket
scoop install freeok/so-novel
```

### 🍺 Homebrew

```bash
brew tap ownia/homebrew-ownia
brew install so-novel
```

### 🐧 Linux

```bash
bash <(curl -sSL https://raw.githubusercontent.com/freeok/so-novel/main/bin/linux-install.sh)
```

### 🐳 Docker

**方式 1：脚本一键安装**

```bash
curl -sSL https://raw.githubusercontent.com/freeok/so-novel/main/bin/docker-install.sh | bash
```

**方式 2：Docker Compose**

```yaml
services:
  sonovel:
    image: ghcr.io/freeok/sonovel:latest
    container_name: sonovel
    ports:
      - "7765:7765"
    environment:
      JAVA_OPTS: "-Dmode=web"
    volumes:
      - sonovel_data:/sonovel
    restart: unless-stopped

volumes:
  sonovel_data:
```

**方式 3：直接运行容器**

```bash
# 如需挂载，请提前准备好 config.ini 文件、rules 目录
docker run -d \
  --name sonovel \
  -v /sonovel/config.ini:/sonovel/config.ini \
  -v /sonovel/rules:/sonovel/rules \
  -v /sonovel/downloads:/sonovel/downloads \
  -p 7765:7765 \
  -e JAVA_OPTS='-Dmode=web' \
  ghcr.io/freeok/sonovel:latest
```

**方式 4：从源码构建镜像**

```bash
# 确保已安装 git、maven

# 构建项目
git clone https://github.com/freeok/so-novel.git && cd so-novel
sh bin/release-linux.sh

# 构建 docker 镜像
cp target/app.jar . && cp -r target/SoNovel-Linux_x64/{config.ini,rules} .
docker build -t sonovel .
```

> [!TIP]
>
> 推荐使用以下阅读器
>
> 桌面端：[Readest](https://readest.com/)、[Koodo Reader](https://www.koodoreader.com/zh)、[Calibre](https://calibre-ebook.com/)、[Neat Reader (网页版)](https://www.neat-reader.cn/webapp)
>
> 移动端：[Readest](https://readest.com/)、[Apple Books](https://www.apple.com/apple-books/)、[Moon+ Reader (静读天下)](https://moondownload.com/chinese.html)、[Kindle](https://apps.apple.com/us/app/amazon-kindle/id302584613)
>
> 如需其它电子书格式，请使用 [Calibre](https://calibre-ebook.com/zh_CN) 或 [Convertio](https://convertio.co/zh/) 自行转换！

## JVM Options

| 参数            | 说明                     | 默认值          |
|---------------|------------------------|--------------|
| -Djre         | JRE / JDK 路径           | ./runtime    |
| -Dconfig.file | 配置文件路径                 | ./config.ini |
| -Dmode        | 启动模式，可选值：tui\|cli\|web | tui          |

## 常见问题

https://github.com/freeok/so-novel/issues?q=label%3A%22usage%20question%22

## 讨论

https://github.com/freeok/so-novel/discussions?discussions_q=

## 支持 & 赞助

如果觉得有所帮助，欢迎扫码赞助☕、点击项目主页顶部的⭐Star 按钮支持！

🚀这将是我们持续更新的动力源泉！同时，你也能第一时间获取到最新的更新动态。💡❤️

| 支付宝赞助                                                           | 微信赞助                                                           |
|-----------------------------------------------------------------|----------------------------------------------------------------|
| <img src="assets/donation-alipay.png" alt="支付宝收款码" width="197"> | <img src="assets/donation-wechat.jpg" alt="微信赞赏码" width="197"> |

## 免责声明

在使用本工具前，请务必仔细阅读我们的[法律免责声明](bundle/DISCLAIMER.md)。使用本工具即表示您已阅读、理解并同意遵守所有条款。

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=freeok/so-novel&type=Date)](https://star-history.com/#freeok/so-novel&Date)