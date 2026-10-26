## 书源集信息

> 书源规则文件均位于 rules/xx.json

`main.json`：默认书源，均支持搜索、大陆 IP

| 书源名称                                 | 大陆 IP | 非大陆 IP | ⚠️ 需要注意                                                       |
|--------------------------------------|-------|--------|---------------------------------------------------------------|
| [香书小说](http://www.ibiqugu.net/)      | ✅     | ❌      |                                                               |
| [书海阁小说网](https://www.shuhaige.net/)  | ✅     | ✅      | 搜索限流 (Unexpected end of file from server)                     |
| [梦书中文](http://www.mcxs.la/)          | ✅     | ❌      | 搜索限流 (Connect timed out)                                      |
| [鸟书网](http://www.99wx.info/)         | ✅     | ❌      | 搜索限流 (Read timed out)                                         |
| [笔尖中文](http://www.xbiquwk.com/)      | ✅     | ❌      |                                                               | |
| [悠久小说网](http://www.ujxsw.org/)       | ✅     | ❌      |                                                               |
| [阅读库](http://www.yeudusk.com/)       | ✅     | ❌      |                                                               |
| [笔趣阁365](https://www.biquge365.net/) | ✅     | ✅      | 搜索间隔 15 秒                                                     |
| [第八中文网](https://www.d8zw.com/)       | ✅     | ✅      | 目录自动翻页（每页 100 章），站点偶发 502                                     |
| [笔趣阁52](https://www.52bqg.com/)      | ✅     | ❌      | 目录/章节链接为 JS onclick（规则用 @js 转成绝对链接），正文 base64 编码              |
| [少年小说网](https://www.snxsw.com/)      | ✅     | ✅      | 同笔趣阁52；两次搜索需间隔约 20 秒，否则返回空结果                                  |
| [新笔趣阁小说网](https://www.xbiquge.info/) | ✅     | ✅      | 第八中文网同款 CMS 镜像，站点偶发 502                                       |
| [笔趣阁info](https://www.bqg.info/)     | ✅     | ✅      | 搜索 GET `/search/?searchkey=`；详情页内嵌完整目录；正文章内分页；收录主流书本体及大量同人/续写 |

`proxy-required.json`：需要代理的书源 (必须是非大陆 IP)，需在 config.ini 设置 cf-bypass

| 书源名称                             | 支持搜索 | ⚠️ 需要注意                                                             |
|----------------------------------|------|---------------------------------------------------------------------|
| [69书吧](https://www.69shuba.com/) | ❌    | 章节页有 CF，推荐线程数 <= 5，若绕过失败则提示正文内容为空                                   |
| [全本小说网](https://quanben5.com/)   | ✅    | 完本很全，连载基本搜不到，同 quanben5.io, big5.quanben5.com, quanben-xiaoshuo.com |
| [大熊猫文学](https://www.dxmwx.org/)  | ✅    |                                                                     |                                           |
| [101看书](https://www.101kks.com/) | ✅    | 章节页有 CF，推荐线程数 <= 5，UI 同 69                                          |                                           |
| [顶点小说](https://www.wxsy.net/)    | ✅    | 搜索有 CF，搜索、详情限流                                                      |

`rate-limit.json`：下载限流的书源

| 书源名称                              | 大陆 IP | 非大陆 IP | 支持搜索                  | ⚠️ 需要注意                       |
|-----------------------------------|-------|--------|-----------------------|-------------------------------|
| [新天禧小说](https://www.xtxxs9.com/)  | ✅     | ❌      | ✅                     | 下载过快会导致章节内容为空，建议线程数不大于 5      |
| [零点小说](https://www.0xs.net/)      | ✅     | ✅      | ✅                     | 限流程度和 69 相似，爬取过快会封 IP 且获取不到正文 |
| [速读谷](https://www.suduguu.com/)   | ✅     | ✅      | ✅                     | 章节限流，线程数 1                    |
| [笔趣阁中文网](https://www.xbiquge.cc/) | ✅     | ✅      | 同笔趣阁52；该站连接偶有超时，失败请重试 |

`non-search.json`：不支持搜索的书源，需要输入书籍详情页地址下载

| 书源名称                               | 大陆 IP | 非大陆 IP | ⚠️ 需要注意      |
|------------------------------------|-------|--------|--------------|
| [小说虎](https://www.xiaoshuohu.com/) | ✅     | ✅      | 正文广告较多，需手动过滤 |
| [天天看小说](https://cn.ttkan.co/)      | ✅     | ✅      | 曾经可以搜索       |
| [小说之家](https://xszj.org/)          | ✅     | ✅      | 章节限流         |

`cloudflare.json`：有 Cloudflare 保护的书源，需在 config.ini 设置 cf-bypass

| 书源名称                                | 大陆 IP | 非大陆 IP | ⚠️ 需要注意                                                       |
|-------------------------------------|-------|--------|---------------------------------------------------------------|
| [黄易天地](http://www.xhytd.com/)       | ✅     | ✅      | 非大陆IP可能速度较慢                                                   |
| [96读书](https://www.96dushu.com/)    | ✅     | ✅      | 章节JS加密                                                        |
| [东滩小说](http://www.dongtanxs.com/)   | ✅     | ✅      |                                                               |
| [和图书](https://www.hetushu.com/)     | ✅     | ✅      | 规则将各 URL 指向本机 cf-bypass（默认 127.0.0.1:8000），需自行运行兼容的 HTML 代理服务 |
| [哔哩轻小说](https://www.linovelib.com/) | ✅     | ✅      | 403 页命中程序自带 CF 检测，配置 cf-bypass 即可；轻小说/日轻                      |

`rule-template.json5`：书源规则模板文件

> [!IMPORTANT]
> 使用大陆 IP 为 ❌ 的书源时，大陆用户可能需要非大陆 IP 才能使用
>
> 使用非大陆 IP 为 ❌ 的书源时，非大陆用户可能需要大陆 IP 才能使用
>
> ⚠️ IP 要求仅供参考，不保证完全准确
>
> 根据需要决定是否在 `config.ini` 中设置 HTTP 代理（TUN 模式、路由级代理无需设置）。

```ini
# config.ini
[proxy]
# 是否启用 HTTP 代理 (1 开，0 关)
enabled = 1
host = 127.0.0.1
port = 改为你的代理端口
```

使用不支持搜索的书源时，**仅能输入书籍详情页的 URL 下载**。

## 若书源无法使用，请参考以下步骤排查

1. 许多书源对 IP 有要求，确保你的 IP 符合要求（见上文）
2. IP 被部分书源（永久）封禁了，尝试更换 IP（原因：搜索频率过快、下载间隔过小，下载线程数过大）
3. 一些书源在某些时间可能无法访问（例如维护、被攻击、数据同步），建议多次重试、换个时间再试
4. 检查爬取频率等参数是否合理，否则会被部分书源封禁 IP 或限流
5. 如果按照以上方法还是不行，大概率是书源挂了（比如更换域名，增加了云防护等）

## 切换书源集

修改 `config.ini` 的 `active-rules`，改为上述 json 的全文件名

```ini
[source]
active-rules = xx.json
```

## 自定义书源

参考 rules 目录下的 `rule-template.json5`(模板文件) 和 `main.json`(书源集)

支持语法: css selector, xpath, javascript, regex