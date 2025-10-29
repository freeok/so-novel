## v1.9.6 (2025-10-30)

### ✨ 新特性

- 新增配置项：是否过滤低相似度搜索结果 #283
- 适配 ARM64 发行版的自动更新
- WebUI 显示版本号
- TUI 增加赞助选项

### 🐛 修复

- 修复 Linux 版无法启用 Web 模式 #281

### ♻️ 重构

- 解决某些阅读器无法识别 txt 中的章节名 #282
- 重构 ChapterFilter、ChapterConverter
- 配置项 threads 重命名为 concurrency

### 🛠️ 其他

- 优化构建脚本，从 Adoptium 下载 JRE
- 纠正 readme.txt 错别字 #278
- 升级依赖