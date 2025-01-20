# so-novel

<div align="center">
  <img src="assets/logo-1.ico" alt="logo" style="width: 128px; border-radius: 25px">
</div>

## 前言

适合用户：既想免费看正版付费新书，又追求更佳阅读体验的网络文学爱好者。

适用场景：国内网上 98% 的 TXT 、EPUB 等格式的小说都是完本。如果想看新书，要么去起点等一类的正版平台付费阅读，要么去笔趣阁等一类网站，要么用“阅读”（仅限安卓）等一类
APP。其实这些方案足够很多人用了，但总会有众口难调的情况：嫌弃 UI
难看的、吐槽功能的、受限于平台的。这时阅读器的优势便显现出来了——DIY。这个工具最大的意义就是能把连载的新书免费下载为 EPUB
等电子书格式，从而导入自己喜爱的阅读器。

对于完本小说，本工具同样可以搜索下载。若出现错别字、排版等问题，建议自行搜索下载对应精校版。

## 介绍

交互式小说下载器，Windows、macOS、Linux 解压即用

可根据书名、作者搜索并下载小说

支持导出格式：EPUB、TXT、HTML（支持翻页）

结合以下电子书阅读器使用更佳

- 电脑：[koodo-reader](https://www.koodoreader.com/zh)
- 手机：[Apple Books](https://www.apple.com/apple-books/)、[Moon+ Reader](https://moondownload.com/chinese.html)、<del>
  微信读书</del>（2024.4 更新后非付费会员每月最多导 3 本书）

> [!WARNING]
>
> iOS 16 Apple Books 目录不能定位到当前章节，如果看到很多章了，就需要从上一直向下划，十分不便
>
> iOS 17 修复了此 Bug

## 效果

保留部分下载日志

![sample.jpg](assets/sample.jpg)

## 使用

### 普通安装

1. 下载最新版 https://github.com/freeok/so-novel/releases
2. 根据 [readme.txt](input%2Freadme.txt) 使用

### 🍨 Scoop 安装

```bash
# 若搜不到 so-novel，请先执行下面这一行命令
scoop bucket add spc https://ghp.ci/https://github.com/lzwme/scoop-proxy-cn
scoop install so-novel
```

### 🍺 Homebrew 安装

```bash
brew tap ownia/homebrew-ownia
brew install so-novel
```

> [!TIP]
>
> 许多书源会屏蔽国外 IP，需关闭代理后使用
>
> 由于书源 URL 会变动，旧版可能无法使用，请使用最新版
>
> 若最新版的书源无法使用，请 New issue，将尽快修复
>
> 欢迎在 issue 推荐高质量书源（无需点击“确认您是真人”、能搜到[起点人气榜单](https://www.qidian.com/rank/)的书、错别字少、排版不错乱）

## 常见问题

报错：SocketTimeoutException

- 浏览器是否可以打开书源网站
- 关闭代理
- 更换网络
- 多次尝试，或稍后再试

报错：UnknownHostException (https://github.com/freeok/so-novel/issues/25)

- 检查 DNS 解析 (nslookup 命令)
- 更换 DNS

WPS 打不开 EPUB，提示：不支持打开该类型文件或文件已损坏 (https://github.com/freeok/so-novel/issues/54)

- 使用专业的电子书阅读器打开 EPUB 文件
- 不用 EPUB 格式，config.ini 中修改

macOS & Linux 运行 .sh 失败，尝试以下命令

 ```bash
 dos2unix macos-run.sh
 或
 dos2unix linux-run.sh
 ```

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=freeok/so-novel&type=Date)](https://star-history.com/#freeok/so-novel&Date)

## 防护测试 (GitCode Motherfucker)

对于 GitCode 的无耻行为，只能以其人之道还治其人之身。以下词汇可能触发某些平台对内容的自动审查

```text
香港 单字：王、江、周、鸡、胡、刘、李、吴、毛、温、习、贺、贾、彭、令、金、唐、蒋、潭、撸、枪、党、杀、鑫、三、胖、性、淫、春、馿、轭、翠
中共、共产、共铲党、共残党、共惨党、共匪、赤匪、裆中央 北京当局 北韩、北韩当局、平壤、平壤当局、平壤政府 朝鲜劳动党、劳动党 中宣、真理部
十九大、19大 上海帮 团派 人兽恋 人兽婚姻 九常委、九长老 政治局常委内幕 朝鲜、朝鲜内幕、朝鲜真相、朝鲜崩溃、北韩 中国崩溃论
锦涛、hujin 家宝、影帝、wenjiabao、wjb 近平、xijinping、xjp 假庆淋、jiaqinglin 李月月鸟 回良玉 汪洋 王山支山、wangqishan 张高丽
俞正声 徐才厚 李洪志 蔡英文 郭伯雄 梁光烈 孟建柱 戴秉国 马凯 计划 法拉利 韩正 章沁生 陈世炬 王毅 陈吉宁 华春莹 两会
两会翻白眼、翻白眼、梁相宜、蓝衣女记者、张慧君 泽民、贼民、先皇、太上皇、蛤蟆、续命、长者、驾崩、镇江、扬州、+1s、jiangzemin、jzm、koutakumin（“江泽民”日语读法）
邓小平 毛润之、mzd、moutakutou（“毛泽东”日语读法） 庆红 罗干 likeqiang zhouyongkang lichangchun wubangguo heguoqiang 老人政治
老人干政 四人帮、江青、王洪文、张春桥、姚文元 徐勤先 金正恩、金三胖、金三、三胖、3胖、鑫胖、三月半、金二胖plus、金肥、金三月巴、金三膘、三膘、月半、月票、月巴、Kim
Jong-un 李雪主 金正日、金二胖、金二、二胖、2胖、二月半、金大胖plus、金肥、金二月巴、金二膘、二膘、Kim Jong-il 金敬姬
金日成、金大胖、金大、大胖、大月半、金大月巴、Kim ll-sung 金正男 金韩松 金正哲 玄松月 金圣爱 柳真雅 张成泽 李英浩 黄炳誓 崔龙海
脱北者、逃北、脱北 欢乐组 藤本健二 朝鲜绑架日本人问题、拉致、拉致问题、绑架、绑架问题、绑架日本人 朝鲜饥荒、苦难的行军
金家王朝、金氏朝鲜、金氏王朝、朝鲜王朝 朝鲜核问题 80后 陈独秀 （亲人称呼）逼 李大钊 张国焘 蒋介石、蒋中正 蒋经国 汪精卫、汪兆铭
张学良 杨虎城 张灵甫 马步芳 袁世凯 溥仪、宣统 皇帝、emperor、帝制、复辟 李登辉、岩里政男 陈水扁、阿扁
陈光诚事件、自由光诚、陈光诚.*使馆/使馆.*陈光诚、光诚.*沂南/沂南.*光诚、要有光.*要有诚/要有诚.*要有光 马驰.*新加坡/新加坡.*
马驰 职称英语.*答案/答案.*职称英语 公务员.*答案/答案.*公务员 熙来、薄督、不厚、薄瓜瓜、谷开来、海伍德、尼尔伍德、heywood、neil.*
wood/wood.*neil 天线宝宝.*康师傅/康师傅.*天线宝宝、天线宝宝.*方便面/方便面.*天线宝宝、天线宝宝.*轮胎/轮胎.*天线宝宝、轮胎.*
方便面/方便面.*轮胎 政变、暴动、枪声、戒严、3\.19 北京事件、北京.*出事了/出事了.*北京 逃港 叶城.*砍杀/砍杀.*叶城 弟弟.*
睡/睡.*弟弟 杨杰 陈刚 山水文园 跑官 移动.*十年兴衰/十年兴衰.*移动 连承敏 陈坚 戴坚 冯珏 罗川 马力 盛勇 谢岷 谢文 杨希 叶兵
张斌 陈瑞卿 高念书 华如秀 鲁向东 曲乃杰 孙静晔 涂志森 于剑鸣 张晓明 赵志强 郑建源 丘小雄 公诉 右派 增城 宣言 莫日根
内蒙古.*抗议/抗议.*内蒙古 西乌旗 天府 人民公园 埃及、突尼斯 马杜罗、瓜伊多 支那、支那人、支那豚、支那狗、支那猪、chink、chink
pig、胡扎 日本、大日本帝国、天皇陛下万岁、皇军、精日、精神日本人、日语、天皇、japan、japanese 南京大屠杀、南京事件 反清复明 扬州十日
嘉定三屠 茉莉、jasmine.*revolution/revolution.*jasmine、moli 革命、revolution 集会 浙大招生办 被就业 鲁昕 1949年、民国38年、昭和24年
六.四、六
四、六\.四、64、天安门、八九、平反64、六月四日、5月35日、5月35号、4月65号、4月65日、89动乱、64memo、sixfour、tiananmen、8964、天安.*
事件/事件.*天安、1989.*天安门/天安门.*
1989、开枪、广场、1989年、平成元年、昭和64年、民国78年、令和前31年、大正78年、宣统81年、198964、9891、40609891、9875321、农（阴）历五月初一、己巳蛇年
89.*学生动乱/学生动乱.*89、89.*学生运动/学生运动.*89、64.*学生运动/学生运动.*64、64.*镇压/镇压.*64、64.*真相/真相.*64
学潮、罢课、民运、学运、学联、学自联、高自联、工自联 坦克人、挡坦克、tankman、木犀地、维园晚会、blood is on the square 耀邦、紫阳
鲍彤、鲍朴 改革.*历程/历程.*改革、国家的囚徒、prisoner of the state 民联、民阵 中国民主党、中国民主正义党、中国民主运动
世纪中国基金会 姜维平 艾未未、艾末末、路青、发课 余杰 辛子陵 茅于轼 铁流 赛风, Psiphon Shadowsocks, 影梭, SS Lantern, 蓝灯
同性恋、gay、les、同性婚姻 liu.*xiaobo/xiaobo.*liu、刘霞 我没有敌人、我的最后陈述 零八.*宪章/宪章.*零八、08.*宪章/宪章.*
08、八宪章、8宪章、零八.*县长/县长.*零八、08县长、淋巴县长 月月 诺贝尔和平奖、Nobel Peace Prize 诺贝尔奖、诺贝尔 陈西 谭作人
高智晟 冯正虎 丁子霖 唯色 焦国标 何清涟 方励之 严家其 柴玲 乌尔凯西 封从德 炳章 苏绍智 Shadowrocket SNI DNS污染 Surge
Potatso 2 Brook 陈一谘 韩东方 辛灏年 曹长青 陈破空 盘古乐队 盛雪 伍凡 魏京生 司徒华 黎安友 张宏堡 地下教会 冤民大同盟 达赖
藏独、free tibet 雪山狮子 西藏流亡政府 青天白日旗 民进党 洪哲胜 独立台湾会 中华台北 台湾政论区 台湾自由联盟 台湾建国运动组织
台湾.*独立联盟/独立联盟. 台湾 新疆.*独立/独立.*新疆 东土耳其斯坦、east.*turkistan/turkistan.*east 世维会 港独、Hong Kong
independce、占中、占领中环 港英、港英政府、港英当局 迪里夏提 明报 纽约时报、New York Times 美国之音、VOA 自由亚洲电台、RFA、Radio
free asia 记者无疆界 维基解密.
*中国/中国.*维基解密 伊斯兰国,、ISIS、达伊沙 世界经济导报 AWP Fuck Cock Ass Adult hentai（日语“变态”之意） 中国数字时代
赵家人、精赵、配姓赵 巴拿马文件、panama pappers 蟹农场 ^ytht 新语丝 ^creaders ^tianwang 中国.
*禁闻/禁闻.*中国 阅后即焚 阿波罗网、阿波罗新闻 大参考、^bignews 多维 看中国 博讯、^boxun、peacehall ^hrichina 独立中文笔会
华夏文摘 开放杂志 大家论坛 华夏论坛 中国论坛 木子论坛 争鸣论坛 大中华论坛 反腐败论坛 新观察论坛 新华通论坛 正义党论坛
热站政论网 华通时事论坛 华语世界论坛 华岳时事论坛 两岸三地论坛 南大自由论坛 人民之声论坛 万维读者论坛 你说我说论坛
东西南北论坛 东南西北论坛 知情者 红太阳的陨落 和谐拯救危机 血房 一个孤僻的人 河殇 天葬 黄祸 我的奋斗、Mein kampf 希特勒
东条英机 靖国神社 墨索里尼 法西斯、法西斯主义 斯大林、苏联大清洗 历史的伤口 改革年代政治斗争、改革年代的政治斗争 关键时刻
超越红墙 梦萦未名湖 一寸山河一寸血 北国之春 北京之春 原子弹、atomic bombs 中子弹 核武器 中国之春 东方红时空 婴儿汤 代开.
*发票/发票.*代开 钓鱼岛、钓鱼台、尖阁诸岛、Diaoyu islands、Senkaku islands ^triangle 女保镖 chinese people eating babies 洗脑
网特 内斗 性侵 党魁 文字狱、乾隆 一党专政 一党独裁 新闻封锁 ^freedom ^freechina 反社会 维权人士 维权律师 异见人士、异议人士
高瞻 地下刊物 غا tits boobs 色情、三级片、R片、咸湿 云雨 春药 春宫 女优 花花公子 冲田杏梨 吉泽明步 泷泽萝拉 小泽玛利亚 水菜丽
宇都宫紫苑 日野雫 天使萌 桃乃木香奈 明日花绮罗 三上悠亚 DMM BDSM 性教育 东莞 中功 法轮、falun 明慧、minghui 退党 三退
九评、nine commentaries 洪吟 神韵艺术、神韵晚会 人民报、renminbao 纪元、^dajiyuan、epochtimes 新唐人、ntdtv 全美电视台
新生网、^xinsheng 正见网、zhengjian 追查国际 真善忍 法会 正念 经文 天灭、天怒、迫害 酷刑 自杀、杀人 凌迟 邪恶 罪恶 圣经、Bible
讲真相 马三家 善恶有报、活摘器官、群体灭绝 防火长城、great.*firewall/firewall.*great、gfw.
*什么/什么.*gfw、国家防火墙、翻墙、代理 方滨兴 vpn.*免费/免费.*vpn、vpn.*下载/下载.*vpn、vpn.
*世纪/世纪.*vpn hosts文件、hosts、修改hosts文件 hotspot.
*shield/shield.*hotspot goagent 无界、ultrasurf 动态网 花园网 ^freenet safeweb ^cache 纳米比亚 委内瑞拉 任天堂 ^\s*
海峰\s*$ ^\s*威视公司\s*$ ^\s*nuctech\s*$ ^\s*逍遥游\s*$ ^\s*自由门\s*$ ^\s*自由门\s*$ ^\s*自由之门\s*$ ^\s*freegate\s*$
^\s*freegate download\s*$ ^\s*download freegate\s*$ ^\s*自由门下载\s*$ ^\s*自由门下载\s*$ ^\s*
无界浏览\s*$ ^\s*无界浏览\s*$ ^\s*动网通\s*$ ^\s*dynaweb\s*$ ^\s*dongtaiwang\s*$ 十年、Ten Years 树大招风、Trivisa
习包子、小熊维尼 国语·晋语四、通商宽农、通商宽衣 格萨尔王传、萨格尔王 鸡鸡、JJ、丁丁 低端人口[13] 膜、蛤、膜蛤、膜法、膜蛤文化、蛤诞节
MC天佑、天佑、李天佑 李赣、李老八、抽象工作室、6324 喊麦 陈一发儿 高考、中考（仅每年6月） 艾莎公主、艾莎门、elsa、elsagate
邪典、儿童邪典视频 小猪佩奇 小熊维尼 吸精瓶 戊戌、戊戌变法、戊戌六君子 信女愿一生吃素 袁世凯 刁大犬 社会摇 杨清柠 王乐乐
牌牌琦 全民共振 英国广播公司、BBC HBO 施一公 郑州 metoo 俺也一样 老子也是 俺也一样凉了 中删大学 杜汶泽 孙向文
文化大革命、文革、Cultual revolution 鸿茅药酒 谭秦东 洁洁良、田佳良 毒疫苗、长生生物 莎普爱思 P2P 曼联 孟宏伟 刘强东 范冰冰
庹震 刘芳菲 刘希泳 李书福 南京应用技术学校 宋祖德 南方公园 武汉肺炎 兄贵 哲学 批斗 社会主义 金像奖 主席、领导人、总书记
SCP基金会、SCP 加速主义 润学 政治庇护、BH移民 工会 玻璃心 无依之地 躺平 地狱笑话 
```