## v1.8.4 (2025-06-30)

### ✨ 新特性

- 开放自定义书源功能
- 搜索支持多书连续下载 [https://github.com/freeok/so-novel/issues/192](https://github.com/freeok/so-novel/issues/192)
- 新增书源: 黄易天地 [https://github.com/freeok/so-novel/issues/187](https://github.com/freeok/so-novel/issues/187)
- 新增配置项: `active-rules`, `show_download_log`
- 新增规则字段: `crawl`
- 新增 Linux 一键安装脚本

### 🐛 修复

- 修复失效书源：全本小说网
- 修复封面下载失败时触发 `ansi` 渲染参数异常 [https://github.com/freeok/so-novel/issues/198](https://github.com/freeok/so-novel/issues/198)
- 修复合并产物文件名包含系统非法字符时大小为零且名称被截断 [https://github.com/freeok/so-novel/issues/190](https://github.com/freeok/so-novel/issues/190)
- 修复 `docker-install.sh` 在 WSL Ubuntu 安装失败

### ♻️ 重构

- 拆分书源规则
- 优化目录爬取规则

### 🛠️ 其他

- 升级依赖
- 更新 CI 脚本
- 添加 `D1Workers`
- 添加 `DISCLAIMER.md`
- 优化 `feedback.yml`
- 优化 `docker-install.sh`
- 移除 `jline`