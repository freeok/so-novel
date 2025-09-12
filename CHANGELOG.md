## v1.9.2 (2025-09-12)

### ✨ 新特性

- WebUI 增加源站链接 #243
- 新增七猫封面获取
- 自动填充 `sourceId`
- 可选是否从 qidian 获取最新封面

### 🐛 修复

- 修复 CLI 模式无法触发

### ♻️ 重构

- 移除 fonts 目录，从系统字体获取，优化体积
- 更新章节重试策略
- 重构 `CoverUpdater.java`

### 🛠️ 其他

- 升级 JRE 并优化体积（Windows, Linux）
- 支持 Docker Compose 部署 #252
- 补充 Docker 版安装说明 #253
- 取消包装 jar，降低误报概率
- 重命名 exe 文件