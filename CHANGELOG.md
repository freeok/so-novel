## v1.7.7 (2025-02-03)

### ✨ 新特性

- 新增书源 7: [69书吧(官方)](https://69shuba.cx/) (https://github.com/freeok/so-novel/issues/40)
- 支持详情页 URL 下载 (https://github.com/freeok/so-novel/issues/78)
- rule.json 新增 timeout 属性

### ♻️ 重构优化

- 优化源站搜索结果 (根据每个作者对应的书籍数量降序)
- 起点榜单移动到 qidian_rank 目录
- 更新 rule-6.json
- 清理 rule.json 冗余属性
- 优化 BookSourceQualityTest.java
- 优化 ChapterParser.java
- 优化 Source#jsoupConn
- 优化 ChapterConverter#convert
- 重构 ChapterParser#parse
- 重构 NewSourceTest.java
- 重构 rule.json 排序属性

> [!NOTE]
>
> 温馨提示：
>
> 69书吧（官方）章节限流，使用前必须修改默认配置，[参见此处配置](https://github.com/freeok/so-novel/releases/tag/v1.7.3)
>
> 且搜索有 CF，故暂不支持搜索，只能通过输入书籍详情页网址下载