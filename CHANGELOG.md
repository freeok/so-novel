## v1.9.0 (2025-08-22)

### ✨ 新特性

- 支持 web 功能 #226
- 章节下载出错时中断下载 #220
- 新增书源「老幺小说网」
- 支持配置 github 代理加速地址，用于获取更新

### 🐛 修复

- 修复下载时遇到 `"this.jna" is null` #233
- 修复「全本小说网」聚合搜索失效
- 修复过滤正则误删正文
- 修复顶点小说正文未解密
- 修复某些书源获取的章节链接不正确
- 修复 `docker-install.sh` 启动容器调用错误

### ♻️ 重构

- 优化 web 包代码
- 重构 `config.ini`、`ConfigUtils`
- 重构 `Crawler.java`、`ChapterParser.java`、`BookSourceTest.java`、`JsoupUtils#clearAllAttributes`、
  `ChapterParser#fetchSinglePageContent`、`Parser#httpClient`
- 改进代码结构

### 🛠️ 其他

- 更新、清理书源
- 增加规则模板注释
- 改进 webui 并适配移动端
- 改进 webui 下载体验
- 更新 `feedback.yml`
- 更新 `BOOK_SOURCES.md`
- 更新 `README.md`