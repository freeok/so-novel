> 请务必仔细阅读，否则容易导致无法使用。

## 书源基本信息

| 书源 ID | 书源名称   | 大陆 IP | 非大陆 IP | 支持搜索 | 需要额外注意                                                           | 域名注册时间     | 域名过期时间     | 网址                          |
|-------|--------|-------|--------|------|------------------------------------------------------------------|------------|------------|-----------------------------|
| 1     | 香书小说   | ✅     | ❌      | ✅    | 暂无                                                               | 2023-09-11 | 2025-09-11 | http://www.xbiqugu.la/      |
| 2     | 书海阁小说网 | ✅     | ✅      | ✅    | 搜索有反爬，爬取过快会丢包 (Unexpected end of file from server)               | 2020-06-16 | 2025-06-16 | https://www.shuhaige.net/   |
| 3     | 梦书中文   | ✅     | ❌      | ✅    | 搜索有限流 (Connect timed out)                                        | 2023-04-06 | 2025-04-06 | http://www.mcmssc.la/       |
| 4     | 鸟书网    | ✅     | ❌      | ✅    | 数量15w+，搜索有限流 (Read timed out)                                    | 2023-09-11 | 2025-09-11 | http://www.99xs.info/       |
| 5     | 新天禧小说  | ✅     | ❌      | ✅    | 仅限大陆IP访问，章节无限流                                                   | 2024-05-07 | 2025-05-07 | https://www.tianxibook.com/ |
| 6     | 全本小说网  | ❌     | ✅      | ✅    | 需要梯子，只能搜老书，搜索需加密参数b，有时很卡甚至打不开网站                                  | 2014-12-31 | 2026-12-31 | https://quanben5.com/       |
| 7     | 69阅读   | ❌     | ✅      | ❌    | 需要梯子，搜索有CF，章节无限流（暂时）                                             | 2024-06-02 | 2025-06-02 | https://www.69yuedu.net/    |
| 8     | 大熊猫文学  | ✅     | ✅      | ✅    | 暂无                                                               | 2024-04-29 | 2025-04-29 | https://www.dxmwx.org/      |
| 9     | 369小说网 | ✅     | ✅      | ✅    | 网站被攻击导致数据丢失，搜索暂不可用，搜索 (Read timed out)，章节有限流，章节爬取过快会封IP，书籍数量57w+ | 2024-05-12 | 2025-05-12 | https://www.369book.cc/     |
| 10    | 天天看小说  | ✅     | ✅      | ✅    | 老书全，搜索有限流 (Read timed out)                                       | 2019-11-26 | 2026-11-26 | https://cn.ttkan.co/        |
| 11    | 笔尖中文   | ✅     | ❌      | ✅    | 暂无                                                               | 2022-01-06 | 2026-01-06 | http://www.xbiquzw.com/     |
| 12    | 零点小说   | ✅     | ✅      | ✅    | 限流程度和69书吧相似，爬取过快会封IP，发布页：https://www.cxysb.com/                  | 2019-11-23 | 2025-11-23 | https://www.0xs.net/        |
| 13    | 得奇小说网  | ✅     | ✅      | ✅    | 基本只有新书，爬取频率过快会永久封禁IP (Remote host terminated the handshake)      | 2023-03-30 | 2025-03-30 | https://www.deqixs.com/     |
| 14    | 新笔趣阁   | ✅     | ✅      | ✅    | 暂无                                                               | 2025-02-07 | 2026-02-07 | https://www.xbqg06.com/     |
| 15    | 略更网    | ✅     | ✅      | ✅    | 暂无                                                               | 2015-08-29 | 2025-08-29 | https://www.luegeng.com/    |
| 16    | 96读书   | ✅     | ❌      | ✅    | 晋江的书多，非大陆IP会跳CF，章节有限流，推荐线程数2                                     | 2023-08-23 | 2025-08-23 | https://www.96dushu.com/    |

使用大陆 IP 为 ❌ 的书源时，国内用户需要梯子（需要非大陆 IP）

使用非大陆 IP 为 ❌ 的书源时，国外用户需要梯子（需要大陆 IP）

根据需要决定是否在 `config.ini` 中设置 HTTP 代理（TUN 模式、路由级代理无需设置）。

```ini
# config.ini
[proxy]
# 是否启用 HTTP 代理 (1 开，0 关)
enabled = 1
host = 127.0.0.1
port = 改为你的代理端口
```

使用支持搜索为 ❌ 的书源时，**仅能输入书籍详情页的 URL 下载**。

## 若书源无法使用，请参考以下步骤进行排查

1. 许多书源对 IP 有要求，确保你的 IP 符合要求（见上文）
2. 需要代理 IP 的书源，如果启用代理 IP 后仍无法使用，就更换节点再试
3. 一些书源在某些时间可能无法访问（例如维护、被攻击、数据同步），建议多次重试、换个时间再试
4. 检查爬取频率等参数是否合理，否则会被部分书源封禁 IP 或限流
5. 如果按照以上方法还是不行，大概率是书源挂了（比如更换域名）

## 目前无法使用的书源

- 书源 9（网站被攻击，数据丢失，很多书搜不到）

## 书源质量对比

> 可能跳过的书源： 不支持搜索的、搜索有限流的、搜索意义不大的、暂时无法访问的

[起点月票榜](qidian_rank/1-起点月票榜.md)

[起点畅销榜](qidian_rank/2-起点畅销榜.md)

[起点阅读指数榜](qidian_rank/3-起点阅读指数榜.md)

[起点推荐榜·月榜](qidian_rank/4-起点推荐榜·月榜.md)

[起点收藏榜](qidian_rank/5-起点收藏榜.md)

[起点签约作者新书榜](qidian_rank/6-起点签约作者新书榜.md)