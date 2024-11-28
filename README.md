# so-novel

![logo.ico](assets/logo.ico)

## 前言

适合用户：既想免费看新书，又想获得最佳的阅读体验

适用场景：国内网上 98% 的 txt 、epub 等格式的小说都是完本。如果想看新书，要么去正版平台付费，要么笔趣阁等一类网站，要么用”阅读“等一类
APP (仅限安卓)。其实这两个方案足够多数人用了，但总会有众口难调的情况：嫌弃 UI
难看的、吐槽功能的、受限于平台的。这时阅读器的优势就体现出来了：DIY。这个工具最大的意义就是能把连载的新书下载为 epub
等电子书格式，这样就能导入自己喜爱的阅读器

对于完本小说，本工具依然可以搜索下载。若出现错别字、排版等问题，建议自行搜索下载对应精校版

## 介绍

交互式小说下载器，Windows、macOS、Linux 解压即用

可根据书名、作者搜索并下载小说

支持导出格式：epub、txt、html（支持翻页）

结合以下电子书阅读器使用更佳

- 电脑：[koodo-reader](https://www.koodoreader.com/zh)
- 手机：[Apple Books](https://www.apple.com/apple-books/)、[Moon+ Reader](https://moondownload.com/chinese.html)、~~微信读书~~（2024.4 更新后非付费会员每月最多导入三本书，先培养用户习惯，后开始收割）

> [!WARNING]
>
> iOS 16 Apple Books 目录不能定位到当前章节，如果看到很多章了，就需要从上一直向下划，十分不便
>
> iOS 17 修复了此 BUG

## 效果

保留部分下载日志

![sample.jpg](assets%2Fsample.jpg)

## 使用

### 普通安装

1. 下载最新版 https://github.com/freeok/so-novel/releases
2. 根据 [readme.txt](input%2Freadme.txt) 使用

### Scoop 安装 🍨

```bash
# 若搜不到 so-novel，请先执行下面这一行命令
scoop bucket add spc https://ghp.ci/https://github.com/lzwme/scoop-proxy-cn
scoop install so-novel
```

> [!TIP]
>
> 部分书源会屏蔽国外 IP，建议关闭代理后使用
>
> 由于书源 URL 经常变动，旧版基本无法使用，请使用最新版
>
> 若最新版也无法使用，请 New issue，将尽快修复
>
> 欢迎在 issue 推荐高质量书源（最好能搜到[起点人气榜单](https://www.qidian.com/rank/)的书、错别字少、同步更新快）

## 常见问题

macOS & Linux 运行 .sh 失败，尝试以下命令

```bash
dos2unix macos-run.sh
或
dos2unix linux-run.sh
```

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=freeok/so-novel&type=Date)](https://star-history.com/#freeok/so-novel&Date)
