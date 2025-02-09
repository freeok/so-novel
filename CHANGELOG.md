## v1.7.8 (2025-02-09)

### ✨ 新特性

- 新增书源 8：[大熊猫文学](https://github.com/freeok/so-novel/issues/86)
- 新增书源 9：[369小说网](https://github.com/freeok/so-novel/issues/90)
- 新增书源 10：[天天看小说](https://github.com/freeok/so-novel/issues/90)
- 新增封面获取途径：纵横中文网
- 书源规则支持 JS
- rule.json 新增 limitPage 和 needProxy 属性

### 🐛 Bug 修复

- 修复 BookSourceTest.java
- 修复非全本下载时，生成的目录文件内容错误
- 修复正文 html 标签含有属性时引发的错误
- 修复搜索结果为空时报错

### ♻️ 重构优化

- 更新起点榜单
- 优化 rule.json
- 重构 CoverUpdater#matchBook
- 重构 CrawlUtils.java 解耦
- 重构 Source#jsoup
- 重构 FileUtils.java
- 重构 BookSourceQualityTest.java
- 重构 rule.json 书籍详情与封面规则
- 重构 BookSourceTest.java
- 解耦 BookParser.java
- 清理 rule.js
- 目录重命名为 TOC

> [!NOTE]
>
> 最新书源信息见：https://github.com/freeok/so-novel/issues/50