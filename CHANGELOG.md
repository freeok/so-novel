## v1.8.1 (2025-04-30)

### 更新内容

- :boom: 重构 `HttpURLConnection` 为 `OkHttp`
- :boom: `JS Runtime` 由 `Nashorn` 升级为 `Javet` (V8 嵌入)
- :sparkles: 新增书源：阅读库 [#155](https://github.com/freeok/so-novel/issues/155)
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