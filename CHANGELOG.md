## v1.7.10 (2025-03-01)

### ✨ 新特性

- 新增书源 13：得奇小说网 https://github.com/freeok/so-novel/issues/98
- 新增书源 14：新笔趣阁
- 新增书源 15：略更网 https://github.com/freeok/so-novel/issues/100

### 🐛 Bug 修复

- 修复 https://github.com/freeok/so-novel/issues/101
- 修复短目录范围下载失败
- 修复部分书源封面下载失败
- 修复分页正则

### ♻️ 重构优化

- 优化书源规则
- 优化 JS 规则
- 优化搜索结果列显示
- 下载格式不分大小写
- 重构 `JsoupUtils#select`
- 重构 `SearchResultParser`
- 重构 `TocParser#extractPaginationUrls`

### 🚀 部署

- 为项目添加 `Dockerfile`