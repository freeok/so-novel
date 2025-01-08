# so-novel

<div align="center">
  <img src="assets/logo-1.ico" alt="logo" style="width: 128px; border-radius: 25px">
</div>

## 前言

适合用户：既想免费看正版付费新书，又追求更佳阅读体验的网络文学爱好者。

适用场景：国内网上 98% 的 TXT 、EPUB 等格式的小说都是完本。如果想看新书，要么去起点等一类的正版平台付费阅读，要么去笔趣阁等一类网站，要么用“阅读”（仅限安卓）等一类
APP。其实这些方案足够很多人用了，但总会有众口难调的情况：嫌弃 UI
难看的、吐槽功能的、受限于平台的。这时阅读器的优势便显现出来了——DIY。这个工具最大的意义就是能把连载的新书免费下载为 EPUB
等电子书格式，从而导入自己喜爱的阅读器。

对于完本小说，本工具同样可以搜索下载。若出现错别字、排版等问题，建议自行搜索下载对应精校版。

## 介绍

交互式小说下载器，Windows、macOS、Linux 解压即用

可根据书名、作者搜索并下载小说

支持导出格式：epub、txt、html（支持翻页）

结合以下电子书阅读器使用更佳

- 电脑：[koodo-reader](https://www.koodoreader.com/zh)
- 手机：[Apple Books](https://www.apple.com/apple-books/)、[Moon+ Reader](https://moondownload.com/chinese.html)、<del>
  微信读书</del>（2024.4 更新后非付费会员每月最多导 3 本书）

> [!WARNING]
>
> iOS 16 Apple Books 目录不能定位到当前章节，如果看到很多章了，就需要从上一直向下划，十分不便
>
> iOS 17 修复了此 Bug

## 效果

保留部分下载日志

![sample.jpg](assets%2Fsample.jpg)

## 使用

### 普通安装

1. 下载最新版 https://github.com/freeok/so-novel/releases
2. 根据 [readme.txt](input%2Freadme.txt) 使用

### 🍨 Scoop 安装

```bash
# 若搜不到 so-novel，请先执行下面这一行命令
scoop bucket add spc https://ghp.ci/https://github.com/lzwme/scoop-proxy-cn
scoop install so-novel
```

### 🍺 Homebrew 安装

```bash
brew tap ownia/homebrew-ownia
brew install so-novel
```

> [!TIP]
>
> 许多书源会屏蔽国外 IP，需关闭代理后使用
>
> 由于书源 URL 会变动，旧版可能无法使用，请使用最新版
>
> 若最新版的书源无法使用，请 New issue，将尽快修复
>
> 欢迎在 issue 推荐高质量书源（无需点击“确认您是真人”、能搜到[起点人气榜单](https://www.qidian.com/rank/)的书、错别字少、排版不错乱）

## 常见问题

报错：SocketTimeoutException

- 浏览器是否可以打开书源网站
- 关闭代理
- 更换网络
- 多次尝试，或稍后再试

报错：UnknownHostException (https://github.com/freeok/so-novel/issues/25)

- 检查 DNS 解析 (nslookup 命令)
- 更换 DNS

WPS 打不开 epub，提示：不支持打开该类型文件或文件已损坏 (https://github.com/freeok/so-novel/issues/54)

- 使用专业的电子书阅读器打开 EPUB 文件
- 不用 epub 格式，config.ini 中修改

macOS & Linux 运行 .sh 失败，尝试以下命令

 ```bash
 dos2unix macos-run.sh
 或
 dos2unix linux-run.sh
 ```

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=freeok/so-novel&type=Date)](https://star-history.com/#freeok/so-novel&Date)