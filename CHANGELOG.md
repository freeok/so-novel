## v1.10.1 (2026-04-05)

### ✨ Features

- WebUI 功能增强 #311

### 🐛 Bug Fixes

- 修复 WebUI 跟随服务器配置的下载格式错误
- 修复设置 [cookie] qidian 后，封面仍下载失败 #315

### ♻️ Refactor

- 优化 HtmlTocHandler.java, CrawlerPostHandler.java
- 更新 CoverUpdaterTest.java
- 更新 maven-compiler-plugin configuration
- 重构并调用 FileUtils#toAbsolutePath()
- 移除 VersionServlet.java