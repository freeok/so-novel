## v1.8.3 (2025-06-10)

### ✨ 新特性

- 新增书源: 书林文学 https://github.com/freeok/so-novel/issues/166
- 新增书源: 小说虎
- 新增章节缓存目录保留配置项 https://github.com/freeok/so-novel/issues/172
- 临时章节文件名添加前导零 https://github.com/freeok/so-novel/issues/165

### 🐛 修复

- 修复模糊文本的聚合搜索结果为空（相似度过低被忽略）
- 修复分页章节标签解析错误
- 修复非全本下载后，文件名下划线前的序号错误
- 修复自定义下载路径时，封面下载出错 https://github.com/freeok/so-novel/discussions/177
- 修复书源 20 封面下载失败
- 修复 epub 空封面页

### ♻️ 重构

- 替换章节文件名中的非法字符 https://github.com/freeok/so-novel/pull/179
- 重构 `BookSourceQualityTest.java`
- 更新 timeout 默认值

### 🛠️ 其他

- 移除书源: 新笔趣阁 https://github.com/freeok/so-novel/issues/148
- 移除书源: 96读书（章节页 CF）
- 优化书源 20 过滤规则
- 更新发布脚本
- 更新起点榜单
- 更新推荐书源模板