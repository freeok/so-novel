## v1.8.0-beta.1 (2025-04-06)

### ✨ 新特性

- 新增聚合搜索功能（显著提升用户体验） https://github.com/freeok/so-novel/issues/106
- 新增书源 16：96读书 https://github.com/freeok/so-novel/issues/102
- 新增书源 17：速读谷 https://github.com/freeok/so-novel/issues/115
- 书源 5 替换为：新天禧小说 https://github.com/freeok/so-novel/issues/112
- 书源 9 替换为笔趣阁 https://github.com/freeok/so-novel/issues/129
- 新增配置项 search-limit
- 书籍详情抓取更多信息

### 🐛 修复

- 修复失效书源
- 修复正文内容包含 \<br\>
- 修复搜索结果列错位
- 修复部分书源目录章节链接错误
- 修复部分书源分页目录仅获取首页

### ♻️ 重构

- 重构 `SourceUtils.java`
- 重构 `BookSourceQualityTest.java`
- 重构 `SearchResultParser` 并重命名为 `SearchParser`
- 重命名 `SearchResultsHandler#handle`
- 新增属性 `SearchResult#sourceId`
- 新增重载 `SearchResultParser#parse`

### 🎨 UI

- 修改 ANSI 样式
- 修改功能选项文本

### 📝 文档

- 精简 `BOOK_SOURCES.md`
- 修复 `ISSUE_TEMPLATE` 无效链接
- 更新 `qidian_rank`