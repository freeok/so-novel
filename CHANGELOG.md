## v1.12.0 (2026-10-26)

### ✨ Features

- 新增 8 个书源并更新书源文档

### 🐛 Bug Fixes

- 修复 Javet 初始化时可能出现的 `SIGSEGV` 错误，通过在主线程强制初始化 V8，无需设置环境变量 `JAVET_DISABLE_PKU=1`
- 兼容服务端拒绝 `Referer` 尾部斜杠的问题

### ♻️ Refactor

- 引入 DSL 执行引擎，重构 HTML 提取逻辑
- 将 `BookParser.getContentType()` 迁移到 `HtmlExtractor.java`
- 优化 `SearchParser.java`
- 优化 `BookParser.java`
- 重构 `TocParser.java`
- 重构 `ChapterParser.java`
- 改进项目结构
- 增强 `SearchResultsHandler.java` 代码健壮性

### ⚡ Performance

- 优化分页目录解析速度

### 🔧 Maintenance

- 整理 `main.json`、`rate-limit.json` 书源配置
- 清理 `main.json` 失效书源并更新域名
- 更新书源规则域名
- 更新 Homebrew 安装步骤
- 新增终端输入编码不匹配测试
- 为 Star History 添加令牌
- 将 CHANGELOG_ALL.md 打包进各平台发行包