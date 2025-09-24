## v1.9.3 (2025-09-24)

### ✨ 新特性

- 规则文件 `language` 属性默认从系统获取
- 支持 native 打包方式
- 复活书源「速读谷」 #262

### 🐛 修复

- 修复 `search-limit` 未指定时不为全部
- 修复裁剪 JRE 引发的问题 #266

### ♻️ 重构

- 优化 Linux、macOS 启动脚本
- 使用 JDK 21 Virtual Threads
- 使用 JDK 21 `Collection.getFirst()`
- 重构 `OkHttpClientFactory.java`
- 重构 `Main.java`
- 重构 `BookSourceQualityTest.java`

### 📝 文档

- 添加启动参数说明 (JVM Options)
- 添加从源码构建 Docker 镜像 #257
- 更新 `BOOK_SOURCES.md`
- 移除 `qidian_rank`

### 🛠️ 其他

- JRE 升级到 21
- 添加 `fetch-github-releases.sh`
- 添加 `VirtualThreadTest.java`
- 整理 `resources` 文件