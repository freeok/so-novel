## v1.10.0 (2026-03-08)

### ✨ Features

- 新增书源「燃文小说网」
- 新增书源「101看书」#238
- 新增书源「笔趣阁365」#256
- 新增书源「东滩小说」#295
- 新增书源集 `cf-direct.json`
- 支持添加 Cloudflare 防护的书源 #250
- 简化书源详情页规则，默认从 meta 获取

### 🐛 Bug Fixes

- 修复 69 书吧无法获取正文 #250
- 修复 Web 启动崩溃
- 修复 WebUI 部署后未显示章节下载进度

### ♻️ Refactor

- 更新解析器
- 更新随机 UA
- OkHttp 请求携带 Referer 头
- 重构 SSE 代码
- 重构 `VirtualThreadLimiter.java`
- 重构 `CheckUpdateAction.java`
- 优化 `CrawlUtils#hasCf()`
- 移除 `SearchResultsHandler#sort()`
- 移除 `Book.java`
- 移除 `Rule#Book#wordCount`
- 重命名规则文件

### 📝 Documentation

- update readme.txt
- update recommend-source.yml
- update SPONSORS.md
- README.md: 增加 WPS、掌阅等软件无法打开下载的 EPUB 解决方案
- README.md: 将「JVM Options」改为「自定义 JVM 系统属性」
- README.md: 移除 JVM Options `-Dfile.encoding`
- README.md: 更正 `start-custom-jre.cmd` 错误

### 🚸 Other Improvements

- WebUI 列显示书源名
- 优化 `run-macos.sh` #308
- 更新书源规则模板
- 更新 JS 逆向测试
- 适配 act