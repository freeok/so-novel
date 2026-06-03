## v1.10.2 (2026-06-03)

### ✨ Features

- 增强书源规则功能，filterTag 支持 CSS Selector 语法
- 新增配置项：是否启用下载进度条 #254

### 🐛 Bug Fixes

- 修复 WebUI 偶发性下载非指定文件的问题 #325
- 修复书海阁分页章节第一页内容丢失 #331
- 修复「69书吧」域名切换导致目录章节数为 0 的问题

### ♻️ Refactor

- 限制并发数并调整 SSE 推送频率
- 更新书源规则
- 更新 BookSourceTest

### 🚚 Chore

- 更新 Bootstrap CDN

### 📝 Documentation

- 更新 SPONSORS.md
- 更新 BOOK_SOURCES.md、readme.txt
- 更新 preview-webui.png