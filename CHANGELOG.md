## v1.7.6 (2025-01-29)

### ✨ 新特性

- 支持繁体中文(台湾)  、简体中文互转
- 新增书源 6：[全本小说网](https://quanben5.com/) (https://github.com/freeok/so-novel/issues/82)

### 🐛 Bug 修复

- 修复下载指定章节起始章错误
- 修复章节解析逻辑错误
- 修复语言不为 zh_CN 时无法从起点获取最新封面
- 修复在 String#replaceFirst 中标题包含括号时的 PatternSyntaxException

### ♻️ 重构优化

- txt、html 格式增加下载封面日志
- 书源 1 ~ 4 规则增加 language 属性
- 更新 rule-5.json
- 规范 language 值命名
- 重构 ChineseConverter.java
- 重构 SearchResultParser#parse
- String#trim 替换为 String#strip (JDK 11)