## v1.9.1 (2025-09-02)

### ✨ 新特性

- 增加输入详情页 URL 下载方式的可选项
- 新增 JVM 参数 mode 以设置启动模式

### 🐛 修复

- 修复 Javet libatomic Linux 的链接问题 #207

### ♻️ 重构

- 重构 `Crawler.java`、`SingleSearchAction.java`
- 下载进度推送改用 SSE 实现

### 🛠️ 其他

- Docker 默认以 Web 模式启动 #239
- 优化 Docker 部署
- 禁用书源「69书吧」#250
- 升级依赖