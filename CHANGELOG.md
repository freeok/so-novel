## v1.11.0 (2026-07-01)

### ✨ Features

- WebUI 使用 alpinejs、tailwindcss 重构
- WebUI 增加搜索历史功能
- WebUI 搜索历史支持删除单项，增加删除确认框
- WebUI 增加搜索建议功能
- WebUI 增加书源列表功能 #317
- WebUI 增加已下书籍删除功能
- WebUI 增加下载进度条功能
- 新增书籍详情页解析属性 latestChapterUrl
- 添加 Windows 一键安装脚本及 README 说明

### 🐛 Bug Fixes

- 修复 WebUI 下载下拉菜单默认格式与显式格式去重
- 修复 BookDownloadServlet 和 BookDeleteServlet 路径穿越漏洞
- 修复 PdfMergeHandler 中 FileOutputStream 未安全关闭导致的资源泄漏
- 修复 CoverUpdater.fetchCover 中 InputStream 可能的资源泄漏
- 修复 FileUtils.sortFilesByName 潜在的空指针和数字解析异常
- 修复 Javet 5.0.8 下 ThreadLocal 频繁创建 V8Runtime 导致 V8 sandbox 虚拟地址空间耗尽 #336

### ♻️ Refactor

- 前端代码复用
- 抽取 applyCrawlSettings 方法简化重复 null 检查
- 将 search-filter 判断逻辑内聚到 SearchResultsHandler 中
- 预计算相似度消除重复 StrUtil.similar 调用，提取 weight 方法简化打分逻辑
- StrUtil.isEmpty() 改为更严格的 isBlank()
- 将全本小说网搜索特殊处理内联到规则文件，移除 SearchParserQuanben5.java
- 重构 LangType.java
- LangUtil 重命名为 LangUtils
- 删除冗余规则属性 pagination
- 降低搜索过滤阈值

### ⚡ Performance

- 优化 VirtualThreadLimiter.close() 忙等待模式（Phaser 替代自旋）
- 优化 AppConfigLoader.loadConfig() 重复读取配置文件

### 💄 UI

- 操作按钮统一替换为 svg 图标
- 历史记录和刷新按钮改为图标
- 设置按钮改为开关
- 将源站链接合并到源站列，删除前往按钮

### 🚸 Other Improvements

- 在 Windows cmd 中启用 ANSI 支持，防止彩色文字乱码 #334
- 清理 main.json 失效书源
- 更新单元测试