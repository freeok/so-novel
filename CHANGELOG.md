## v1.8.5 (2025-07-21)

### ✨ 新特性

- 支持 CLI #104
- 新增章节下载进度条功能
- 支持 `active-rules` 绝对路径 #203

### 🐛 修复

- 修复分页章节内容包含多余的 `<p>` #195
- 修复 HTML 文件名前导零导致无法翻页

### ♻️ 重构

- 优化 Docker 安装脚本 #206
- 优化 HTML 目录文件内容格式
- 改进指定搜索, 批量下载体验
- 解耦 Main.java

### 🛠️ 其他

- 更新 GHP 链接
- 更新 `proxy-rules.json`
- 更新 `BookSourceTest.java`
- 更新 `README.md`
- 优化 `CHANGELOG_ALL.md`
- 移除无效的系统属性