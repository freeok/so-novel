## v1.9.7 (2025-12-23)

### ✨ Features

- 改善独立搜索体验
- 解析器支持自动检测文本编码
- 自动获取分辨率最高的封面

### ⚡ Performance

- 提高封面获取性能

### 🐛 Bug Fixes

- 修复全本小说网无法使用

### ♻️ Refactor

- 重构 `SourceUtils.java`
- 优化正文开头重复章节标题的过滤正则 #57
- 增强 CLI 代码健壮性
- 规范 D1 Worker URL 的 Base64 编码

### 🔧 Configuration

- 将「大熊猫文学」规则迁移至 `proxy-rules.json`
- 悠久小说网新增封面规则
- 更新 `flowlimit-rules.json`

### 📝 Documentation

- 新增 `SPONSORS.md`
- 更新 `readme.txt`
- 更新 `feedback.yml`