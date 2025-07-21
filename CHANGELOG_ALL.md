## v1.8.4 (2025-06-30)

### ✨ 新特性

- 开放自定义书源功能
- 搜索支持多书连续下载 #192
- 新增书源: 黄易天地 #187
- 新增配置项: `active-rules`, `show_download_log`
- 新增规则字段: `crawl`
- 新增 Linux 一键安装脚本

### 🐛 修复

- 修复失效书源：全本小说网
- 修复封面下载失败时触发 `ansi`
- 渲染参数异常 #198
- 修复合并产物文件名包含系统非法字符时大小为零且名称被截断 #190
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

---

## v1.8.3 (2025-06-10)

### ✨ 新特性

- 新增书源: 书林文学 #166
- 新增书源: 小说虎
- 新增章节缓存目录保留配置项 #172
- 临时章节文件名添加前导零 #165

### 🐛 修复

- 修复模糊文本的聚合搜索结果为空（相似度过低被忽略）
- 修复分页章节标签解析错误
- 修复非全本下载后，文件名下划线前的序号错误
- 修复自定义下载路径时，封面下载出错 #177
- 修复书源 20 封面下载失败
- 修复 epub 空封面页

### ♻️ 重构

- 替换章节文件名中的非法字符 #179
- 重构 `BookSourceQualityTest.java`
- 更新 timeout 默认值

### 🛠️ 其他

- 移除书源: 新笔趣阁 #148
- 移除书源: 96读书（章节页 CF）
- 优化书源 20 过滤规则
- 更新发布脚本
- 更新起点榜单
- 更新推荐书源模板

---

## v1.8.2 (2025-05-07)

### 更新内容

:ambulance: 修复 macOS 和 Linux 运行出错
:bug: fix watchConfig NoResourceException #170

> [!Note]
> 若 Linux 版运行报错：libjavet-v8-linux-x86_64.v.4.1.3.so: libatomic.so.1: cannot open shared object file: No such file
> or directory
>
> 请尝试执行以下命令解决
> ```bash
> sudo apt update
> sudo apt install libatomic1 -y
> ```

---

## v1.8.1 (2025-05-06)

### 更新内容

- :boom: 重构 `HttpURLConnection` 为 `OkHttp`
- :boom: `JS Runtime` 由 `Nashorn` 升级为 `Javet` (V8 嵌入)
- :sparkles: 新增书源：阅读库 #155
- :sparkles: 新增书源：顶点小说（JS 逆向、反爬破解）
- :sparkles: 自动设置 `language` 配置项
- :sparkles: 过滤正文不可见字符
- :sparkles: 封装环境判断工具类 `EnvUtils`
- :children_crossing: 提高批量下载健壮性
- :speech_balloon: 修改聚合搜索日志文字
- :zap: 优化 `OkHttp` 配置
- :ambulance: 更新书源 18 域名
- :bug: 修复书源 10 目录链接错误
- :bug: 修复书源 11 正文过滤正则错误
- :bug: 修复书源 20 目录乱序、正文随机乱码
- :bug: 修复 `nashorn` 线程不安全导致 `JS` 执行结果错误
- :bug: 修复 `txt`、`html` 格式下载封面失败导致的中断
- :bug: 修复 `config.ini` 配置项为空串时，`hutool Setting#getStr` 不能触发默认值
- :recycle: 下载完毕后删除临时目录
- :recycle: 移除 `CrawlUtils#normalizeUrl`
- :recycle: 修改 `windows` 非法文件名替换
- :recycle: 优化 `Crawler.java`、`ChapterParser.java`
- :recycle: 优化 `BookSourceTest.java`
- :recycle: 修改配置文件默认值
- :arrow_up: `hutool` 升级至 `5.8.37`
- :memo: 更新书源 5、6、12、13 信息
- :memo: 更新起点榜单

---

## v1.8.0 (2025-04-24)

### ✨ 新特性

- 支持导出 PDF 格式

### 🐛 修复

- 修复书源 3 搜索失效（更换域名）
- 暂时禁用书源 13 #148

### ♻️ 重构

- 更新初始界面 UI
- 优化书源延迟测试
- 优化日志格式
- 更新书源 5 规则

### 📝 文档

- 修复问题反馈 issue 模板中的错误链接
- 更新推荐书源 issue 模板
- add donation methods to README

---

## v1.8.0-beta.2 (2025-04-17)

### ✨ 新特性

- 新增书源：八一中文网 #140
- 新增书源：悠久小说网 #154
- 新增 Docker 安装脚本 #144

### 🐛 修复

- 修复封面获取 NPE
- 修复 #157
- corrected full-width period in chapter filenames #158

### ♻️ 重构

- 修改 epub 文件名格式

### 📝 文档

- 更新 `README.md`
- 更新 `BOOK_SOURCES.md`
- 更新 `qidian_rank`

---

## v1.8.0-beta.1 (2025-04-06)

### ✨ 新特性

- 新增聚合搜索功能（显著提升用户体验） #106
- 新增书源 16：96读书 #102
- 新增书源 17：速读谷 #115
- 书源 5 替换为：新天禧小说 #112
- 书源 9 替换为笔趣阁 #129
- 新增配置项 search-limit
- 书籍详情抓取更多信息

### 🐛 修复

- 修复失效书源
- 修复正文内容包含 \<br\>
- 修复搜索结果列错位
- 修复部分书源目录章节链接错误
- 修复部分书源分页目录仅获取首页

### ♻️ 重构

- 重构 `SourceUtils.java`
- 重构 `BookSourceQualityTest.java`
- 重构 `SearchResultParser` 并重命名为 `SearchParser`
- 重命名 `SearchResultsHandler#handle`
- 新增属性 `SearchResult#sourceId`
- 新增重载 `SearchResultParser#parse`

### 🎨 UI

- 修改 ANSI 样式
- 修改功能选项文本

### 📝 文档

- 精简 `BOOK_SOURCES.md`
- 修复 `ISSUE_TEMPLATE` 无效链接
- 更新 `qidian_rank`

---

## v1.7.11 (2025-03-19)

### 🐛 Bug 修复

- 修复 #116
- 修复书源一览显示过长
- 修复非小写扩展名导致下载失败

### ♻️ 重构优化

- 改进搜索结果为空时的后续操作体验
- 优化书源 9 目录解析速度
- 优化书源 1、9 规则
- 重构 `ChapterFilter.java`
- 重构 `ChapterParser.java`

### 📝 其它

- 更新 Scoop 安装命令 #117
- 更新 ISSUE_TEMPLATE
- 升级依赖

---

## v1.7.10 (2025-03-01)

### ✨ 新特性

- 新增书源 13：得奇小说网 #98
- 新增书源 14：新笔趣阁
- 新增书源 15：略更网 #100

### 🐛 Bug 修复

- 修复 #101
- 修复短目录范围下载失败
- 修复部分书源封面下载失败
- 修复分页正则

### ♻️ 重构优化

- 优化书源规则
- 优化 JS 规则
- 优化搜索结果列显示
- 下载格式不分大小写
- 重构 `JsoupUtils#select`
- 重构 `SearchResultParser`
- 重构 `TocParser#extractPaginationUrls`

### 🚀 部署

- 为项目添加 `Dockerfile`

---

## v1.7.9 (2025-02-22)

### ✨ 新特性

- 新增书源 11：笔尖中文
- 新增书源 12：零点小说 #96
- 新增批量下载功能 #76

### 🐛 Bug 修复

- 修复 #97
- 修复 #91
- 修复分页搜索单页异常
- 修复 NPE 错误

### ♻️ 重构优化

- 更新书源规则
- 更新书源测试

---

## v1.7.8 (2025-02-09)

### ✨ 新特性

- 新增书源 8：大熊猫文学 #86
- 新增书源 9：369小说网 #90
- 新增书源 10：天天看小说
- 新增封面获取途径：纵横中文网
- 书源规则支持 JS
- rule.json 新增 limitPage 和 needProxy 属性

### 🐛 Bug 修复

- 更新书源 1 域名
- 修复 BookSourceTest.java
- 修复非全本下载时，生成的目录文件内容错误
- 修复正文 html 标签含有属性时引发的错误
- 修复搜索结果为空时报错

### ♻️ 重构优化

- 更新起点榜单
- 优化 rule.json
- 重构 CoverUpdater#matchBook
- 重构 CrawlUtils.java 解耦
- 重构 Source#jsoup
- 重构 FileUtils.java
- 重构 BookSourceQualityTest.java
- 重构 rule.json 书籍详情与封面规则
- 重构 BookSourceTest.java
- 解耦 BookParser.java
- 清理 rule.js
- 目录重命名为 TOC

> [!NOTE]
>
> 最新书源信息见：#50

---

## v1.7.7 (2025-02-03)

### ✨ 新特性

- 新增书源 7: [69书吧(官方)](https://69shuba.cx/) #40
- 支持详情页 URL 下载 #78
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
> 这一版新增的 ``69书吧(官方)`` 有限流，使用前必须修改默认配置，否则会被该网站封禁 IP（至少几个小时无法使用）
>
> 且搜索页有 CF，故暂不支持搜索，只能通过输入书籍详情页网址下载

---

## v1.7.6 (2025-01-29)

### ✨ 新特性

- 支持繁体中文(台湾)  、简体中文互转
- 新增书源 6：[全本小说网](https://quanben5.com/) #82

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

---

## v1.7.5 (2025-01-20)

### :sparkles: 新特性

- 新增下载最新章节功能 #77

### :bug: Bug 修复

- 重复标题 2 次修复 #83
- 修复书源 5 搜素结果异常

### :recycle: 重构

- 优化书源 2 规则
- 优化源站搜索结果
- 过滤正文空标签
- auto-update 默认值改为 0
- 修改自动更新进度条样式

### :wrench: 其它

- 更新 launch4j 脚本
- 更新 jpackage 脚本

---

## v1.7.4 (2025-01-08)

### :sparkles: 新特性

- 支持两种交互模式 (见 config.ini)
- 兼容目录分页的书源
- 新增书源一览功能 #52

### :bug: Bug 修复

- 修复代理配置问题 #69
- 修复进度条显示异常
- 修复下载失败日志文件编码问题

### :recycle: 重构

- Pretty output config.ini
- 重构 proxy 配置项
- 清理冗余代码

### :wrench: 其它

- 更新 GHP 地址
- 更新 ISSUE_TEMPLATE
- 关闭配置文件加载日志

---

## v1.7.3 (2024-12-31)

### :sparkles: 新特性

- 新增书源：[69書吧](https://69shux.co/) #65, #61, #41, #40, #13
- 不指定配置项 `source-id` 时，使用随机书源

### :bug: Bug 修复

- 修复爬取间隔设置失效
- 修复部分书源搜索结果唯一时，搜索结果为空的问题

### :recycle: 重构

- 重构 `Source.java`
- 优化重复代码

### 🔧 其它

- 更新起点榜单
- 更新 `README.md`、`readme.txt`
- 新增 ISSUE_TEMPLATE：`问题反馈`、`功能请求`

> [!TIP]
>
> 69書吧限流较为严重，下载速度明显慢于其他书源。
>
> 建议先使用以下配置，之后根据需要进行微调。
>
> ```ini
> # config.ini
> 
> [crawl]
> # 爬取最小间隔 (毫秒)
> min = 1000
> # 爬取最大间隔 (毫秒)
> max = 2000
> # 爬取线程数，-1 表示自动设置
> threads = 1
> 
> [retry]
> # 最大重试次数
> max-attempts = 5
> # 重试爬取最小间隔 (毫秒)
> min = 2000
> # 重试爬取最大间隔 (毫秒)
> max = 5000
> ```
>
> 还需要指定书源 ID， 大陆用户可能需要设置代理
>
> ```ini
> [base]
> # 启用69書吧
> source-id = 5
> # 网络代理
> proxy-host = 127.0.0.1
> proxy-port = your port
> ```

---

## v1.7.2 (2024-12-24)

更新内容

✨ 新增书源 4

✨ 更新书源 1、3 过滤规则

✨ epub 内容首页增加封面；txt、html 目录增加封面

🐛 修复正文包含重复标题 #57

🚸 默认不启用自动更新 #53

♻️ 重构 txt 合并代码

---

## v1.7.1 (2024-12-05)

更新内容：

✨ 新增书源 3 (http://www.mcmssc.la/)

✨ TXT 开头添加书籍信息

✨ 新增书源质量检测 #50

🚸 改善交互体验 #46

🚸 启动时校验 source-id 合法性

💄 优化 EPUB、HTML、TXT 正文排版

⚡ EPUB 生成后删除临时目录

🐛 修复部分书源封面 URL 获取异常

🐛 修复某些情况下搜索失败

📝 更新使用说明 readme.txt

---

## v1.7.0 (2024-12-02)

更新内容:

🚸 支持动态加载配置文件，修改配置文件后无需重启

✨ 新增配置项 auto-update（启动时是否自动更新）

🐛 修复封面下载失败会中断 epub 生成

🐛 修复自动更新时预览版本号比较问题

📝 新增 Homebrew 安装方法，感谢 [Weizhao](https://github.com/ownia)

---

## v1.7.0-beta.2 (2024-11-28)

更新内容

- :bug: 修复 txt 格式首行多余文本
- :memo: 更新使用须知
- :wrench: 更新配置文件默认值
- :zap: 提高下载成功率
- :children_crossing: 优化自动更新体验

---

## v1.7.0-beta.1 (2024-11-28)

更新内容

- :sparkles: 新增书源 #33
- :bug: 修复下载章数即使不为 0，epub 也不生成的问题
- :children_crossing: 优化交互体验，可重选下载序号

---

## v1.6.4 (2024-11-28)

更新内容

- :sparkles: 新增重选下载序号功能
- :sparkles: 新增章节爬取失败重试功能
- :sparkles: 新增config.ini下载重试参数，并设置其它参数的默认值

---

## v1.6.3 (2024-11-20)

更新内容

:bug: 修复部分用户 macOS、Linux 启动报错

---

## v1.6.2 (2024-11-19)

更新内容

- :sparkles: 更改交互方式，改为按 Tab 键选择功能
- :sparkles: 新增软件更新功能
- :sparkles: 新增查看配置文件功能
- :bug: 修复最新封面替换失效

---

## v1.6.1 (2024-11-08)

:arrow_up: 依赖升级

| dependency            | before  | after   |
|-----------------------|---------|---------|
| jsoup                 | 1.17.2  | 1.18.1  |
| hutool                | 5.8.28  | 5.8.33  |
| lombok                | 1.18.32 | 1.18.34 |
| junit-jupiter-api     | 5.10.2  | 5.11.3  |
| launch4j-maven-plugin | 2.5.1   | 2.5.2   |

---

## v1.6.0 (2024-09-22)

更新内容

- 适配 Linux
- Windows 版统一包含 JRE，不再区分有无 JRE 版本
- 更新 CI 构建脚本

> [!WARNING]
>
> 致 Scoop 用户：v1.6.0 开始，so-novel-with-jre 已弃用，请卸载并重装 so-novel

```bash
scoop uninstall so-novel-with-jre
scoop bucket add freeok https://github.com/freeok/scoop-bucket
scoop install freeok/so-novel
scoop install so-novel
```

---

## v1.5.9 (2024-08-10)

更新内容

- 章节下载失败会生成日志
- 无下载章节时不生成 epub
- 新增推荐书源 issue template
- 修复输入提示信息重复
- 重构书源爬取规则，XPath 替换为 CSS selectors

---

## v1.5.8 (2024-07-02)

更新内容

- 移除 epub 默认样式 #11

---

## v1.5.7 (2024-06-19)

更新内容

- 新增章节下载失败时提示原因
- 修复 Apple Books 部分章节报错
- 修复 Apple Books 无法使用中文字体 #9

---

## v1.5.6 (2024-05-06)

更新内容

- 修复合并 epub 时 NPE
- 修改 windows release 文件名

---

## v1.5.5 (2024-05-06)

更新内容

- 新增 macOS 发布自动化
- JRE 升级至 17.0.11+9
- 依赖升级

---

## v1.5.4.1 (2024-04-27)

更新内容

- 更新书源 url

---

## v1.5.4 (2024-04-04)

更新内容

- html 导出格式新增翻页功能（支持左右键）
- html 导出格式新增目录文件
- 优化 epub 体积

---

## v1.5.3.1 (2024-03-31)

下载选择

## Windows

- 如果已安装 JDK 17 或 JRE 17：sonovel-windows.tar.gz
- 否则：sonovel-windows-with-jre.tar.gz

## macOS

- sonovel-macos_x64.tar.gz
- sonovel-macos_arm64.tar.gz

---

## (2024-03-26)

更新内容

- 修复 txt 章节名识别错误

---

## v1.5.2 (2024-03-26)

更新内容

- 配置文件格式改为 ini
- 封面优先从起点获取

---

## v1.5.1 (2024-03-23)

更新内容

- 新增 Windows 免安装版，解压即用

---

## v1.5.0 (2024-03-17)

更新内容

- 支持导出格式：epub、txt、html

---

## v1.4.0 (2024-03-09)

更新内容

- 更新书源 url
- 修改爬取间隔
- 新增全本下载功能
- 依赖升级
- 新增配置项 threads
- 去除正文无关内容
- txt 格式增加章节标题

---

## v1.3.0 (2023-08-12)

更新内容

- 优化、重构代码

---

## v1.2.0 (2023-03-16)

更新内容

- 优化、重构代码

---

## v1.1.0 (2022-12-07)

书源已失效，勿用