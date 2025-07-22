var qsbs = {
  _keyStr: "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",
  bb: function (a) {
    var b = "",
      d, c, i, e, f, h, j, g = 0;
    a = a.replace(/[^A-Za-z0-9+\/=]/g, "");
    while (g < a.length) {
      e = this._keyStr.indexOf(a.charAt(g++));
      f = this._keyStr.indexOf(a.charAt(g++));
      h = this._keyStr.indexOf(a.charAt(g++));
      j = this._keyStr.indexOf(a.charAt(g++));
      d = (e << 2) | (f >> 4);
      c = ((f & 15) << 4) | (h >> 2);
      i = ((h & 3) << 6) | j;
      b += String.fromCharCode(d);
      if (h != 64) b += String.fromCharCode(c);
      if (j != 64) b += String.fromCharCode(i)
    }
    return this._utf8_decode(b)
  },
  _utf8_encode: function (a) {
    a = a.replace(/\r\n/g, "\n");
    var b = "";
    for (var d = 0; d < a.length; d++) {
      var c = a.charCodeAt(d);
      if (c < 128) {
        b += String.fromCharCode(c)
      } else if (c < 2048) {
        b += String.fromCharCode((c >> 6) | 192);
        b += String.fromCharCode((c & 63) | 128)
      } else {
        b += String.fromCharCode((c >> 12) | 224);
        b += String.fromCharCode(((c >> 6) & 63) | 128);
        b += String.fromCharCode((c & 63) | 128)
      }
    }
    return b
  },
  _utf8_decode: function (a) {
    var b = "",
      d = 0,
      c = 0,
      i = 0,
      e = 0,
      f = 0;
    while (d < a.length) {
      c = a.charCodeAt(d);
      if (c < 128) {
        b += String.fromCharCode(c);
        d++
      } else if (c < 224) {
        e = a.charCodeAt(d + 1);
        b += String.fromCharCode(((c & 31) << 6) | (e & 63));
        d += 2
      } else {
        e = a.charCodeAt(d + 1);
        f = a.charCodeAt(d + 2);
        b += String.fromCharCode(((c & 15) << 12) | ((e & 63) << 6) | (f & 63));
        d += 3
      }
    }
    return b
  }
};

r = `
<h3>第560章（第1页）</h3>
<div class="read_btn">
 <a href="/novel/3998/read_1895234.html">上一章</a><a href="/novel/3998/">章节目录</a><a rel="nofollow" onclick="shuqian(3998,1895235,'https%3A%2F%2Fwww.wxsy.net%2Fnovel%2F3998%2Fread_1895235.html')">保存书签</a><a href="/novel/3998/read_1895235_1.html">下一章</a>
</div>
<script>document.writeln(qsbs.bb('PHA+6aG56Zuo6Zeu6YGT77ya4oCc5be05be05aGU77yM5pei54S25L2g6L+Z5LmI5LqG6Kej6L+Z6ImY6aOe6Ii577yM5a6D6YeM6Z2i5pyJ5ZOq5Lqb6Zm36Zix5L2g5bqU6K+l5Lmf55+l6YGT5ZCn77yf4oCdPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+5be05be05aGU54K55LqG54K55aS06YGT77ya4oCc5b2T54S255+l6YGT5LqG77yM6L+b5YWl6L+Z6ImY6aOe6Ii555qE6Iix6Zeo5ZCO77yM5YWl5Y+j5aSE55qE6YCa6YGT5bCx5piv5LiA5Liq5r+A5YWJ6YCa6YGT77yM6YeM6Z2i55qE5r+A5YWJ6Laz5Lul6L275p2+5bCE5p2A6KGM5pif57qn5YWt6Zi25q2m6ICF44CCPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+5Zyo5r+A5YWJ6YCa6YGT55qE5bC95aS077yM6L+Y6K6+5pyJ5LiA5omH6aOe6Ii56Ze46Zeo77yM6Ze46Zeo5p2Q5paZ5Lmf5piv55So5LmM6ZKH6YeR5bGe5omT6YCg6ICM5oiQ77yM5b+F6aG76KaB5pyJ5oGS5pif57qn5Zub6Zi25q2m6ICF55qE5Yqb6YeP5omN6IO95bCG5YW25by66KGM5Li+6LW344CCPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+5LiN6L+H6L+Z5Lik6YGT5py65YWz5piveDgx5Z6L5Y+36aOe6Ii555qE6YCa55So5py65YWz77yM6Iez5LqO6aOe6Ii55YaF6YOo5pyJ5rKh5pyJ6KKr6aOe6Ii555qE5Li75Lq65ZCO5pyf5pS56YCg6L+H77yM6YKj5oiR5bCx5LiN6IO956Gu5a6a5LqG44CC4oCdPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+4oCc6KGM5LqG77yM55+l6YGT6L+Z5Lqb5bCx6Laz5aSf5LqG44CC4oCdPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+6aG56Zuo6K+d6JC977yM5pW05Liq5Lq655u05o6l5rKJ5YWl5Yiw5LqG5b2x5a2Q56m66Ze06YeM77yM54S25ZCO5YyW5L2c5LiA5Zui6buR6Imy55qE5b2x5a2Q5ZCR552A5r+A5YWJ6YCa6YGT5YmN6KGM5LqG6L+H5Y6744CCPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+5Zug5Li65b2x5a2Q56m66Ze05YGP5ZCR5LqO56m66Ze06IO95Yqb77yM5b2x5a2Q56m66Ze05omA5YyW5oiQ55qE5b2x5a2Q5bm25rKh5pyJ5bC45L2T77yM5omA5Lul5r+A5YWJ6YCa6YGT6YeM6Z2i55qE5oSf5bqU5py65YWz5qC55pys5bCx6K+G5Yir5LiN5Yiw5b2x5a2Q77yM6Ieq54S25Lmf5bCx5LiN5Lya6Kem5Y+R5r+A5YWJ5pS75Ye75LqG44CCPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+5Zyo6aG56Zuo55qE5pON5o6n5LiL77yM5LiA5Zui5ryG6buR55qE5b2x5a2Q5oKE5peg5aOw5oGv55qE56m/6L+H5LqG5r+A5YWJ6YCa6YGT77yM6aG6552A6aOe6Ii56Ze46Zeo55qE57yd6ZqZ77yM6ZK75YWl5Yiw5LqG6aOe6Ii55YaF6YOo44CCPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+4oCc5Y+u77yB4oCdPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+5bCx5Zyo6aG56Zuo6LaK6L+H6aOe6Ii56Ze46Zeo77yM5LuO5b2x5a2Q56m66Ze06YeM6Z2i6LWw5Ye65p2l55qE556s6Ze077yM6aOe6Ii55oyH5oyl5a6k6YeM54Gv5YWJ56qB54S25Lqu5LqG6LW35p2l44CCPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+5Zyo5oyH5oyl5a6k6YeM5Z2Q552A5LiA5ZCN6Lqr56m/5Yab6KOF55qE55S35Lq677yM5LuW55qE6Lqr5ZCO6L+Y56uZ552A5LiJ5ZCN56m/552A6buR6Imy5Yi25pyN55qE6buR6KGj5Lq677yM5LiN6L+H5LuW5Lus5YWo6YO95L+d5oyB552A5LiA5Yqo5LiN5Yqo55qE54q25oCB44CCPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+4oCc5Lq657G76KGM5pif57qn57K+56We5b+15biI6Zev5YWl77yB4oCdPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+4oCc5aiB6IOB56iL5bqm77yM5byx77yB4oCdPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+4oCc5Ye65Yqo6Ziy5b6h5py65Zmo5Lq677yM5Ye75p2A77yB4oCdPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+5Yab6KOF55S35Lq66Z2i5YmN55qE5bGP5bmV56qB54S25Lqu6LW377yM6YeM6Z2i5Lyg5Ye65LiA6Zi155S15a2Q5py65qKw6Z+z77yM6aOe6Ii555qE6Ziy5b6h57O757uf556s6Ze05r+A5rS744CCPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+4oCc5ZKU5ZKU5ZKU4oCdPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+5Zyo6aOe6Ii55YaF6YOo55qE5LiA5Liq5bCB6Zet55qE5a2Y5YKo6Iix6YeM77yM5LiJ5Liq6Lqr6auY5Lik57Gz5aSa55qE6YeR5bGe5py65Zmo5Lq677yM55y85Lit5ZCM5pe25Lqu6LW357qi5YWJ77yM5Zyo6aOe6Ii55pm66IO957O757uf55qE5pON5o6n5LiL77yM5ZCR552A6aG56Zuo5omA5Zyo55qE5pa55ZCR6LW25LqG6L+H5Y6744CCPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+4oCc6aG56Zuo77yM5bCP5b+D77yB5oiR5o6i5p+l5Yiw5pyJ5LiJ5YW36YeR5bGe5py65Zmo5Lq65q2j5Zyo5ZCR5L2g6aOe6YCf6Z2g6L+R77yM5YGa5aW96L+O5oiY5YeG5aSH77yB4oCdPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+6aG56Zuo6Ze76KiA77yM5oCl5b+Z5YGc5LiL5LqG6ISa5q2l77yM5byn5YiA55uY6aG/5pe25YyW5L2c5pWw55m+5oqK5pyI54mZ6aOe5YiA77yM5oKs5rWu5Zyo5LuW55qE6Lqr5peB6JOE5Yq/5b6F5Y+R44CCPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+4oCc56Cw56Cw56Cw4oCdPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+5Ly06ZqP552A6L275b6u55qE6ZyH5Yqo77yM5LiA6Zi16YeN54mp6JC95Zyw55qE5aOw6Z+z5b+954S25LuO6YCa6YGT5ouQ6KeS5aSE5Lyg5p2l77yM5LiJ5Liq5YWo6Lqr5ryG6buR77yM5Y+M55y85rOb57qi55qE6YeR5bGe5py65Zmo5Lq677yM57Sn6Lef552A5Ye6546w5Zyo5LqG6aG56Zuo55qE6KeG57q/5Lit77yM5YyW5L2c5LiJ6YGT5q6L5b2x5ZCR552A5LuW5Yay5p2A5LqG6L+H5p2l44CCPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+6JOE5Yq/5b6F5Y+R55qE5pWw55m+5oqK5pyI54mZ6aOe5YiA77yM5bCx5YOP5piv6YeR6Imy55qE5rWB5pif6Zuo5LiA6Iis77yM6ZO65aSp55uW5Zyw55qE5pyd552A5LiJ5YW36YeR5bGe5py65Zmo5Lq654iG5bCE6ICM5Y6744CCPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+4oCc5Y+u5Y+u5Y+u4oCdPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+5Y+q5LiN6L+H5b2T6L+Z5Lqb5aiB5Yqb5beo5aSn55qE5pyI54mZ6aOe5YiA5Ye75Lit6YeR5bGe5py65Zmo5Lq65ZCO77yM5Y205Y+q6IO95YuJ5by66K6p6YeR5bGe5py65Zmo5Lq66YCf5bqm5Y+Y5oWi77yM5qC55pys56C05LiN5byA5a6D5Lus55qE6Ziy5b6h44CCPC9wPg=='));</script>
<script>document.writeln(qsbs.bb('PHA+5LiJ5Liq6YeR5bGe5py65Zmo5Lq66aG2552A6aOe5YiA6aOO5pq077yM56Gs55Sf55Sf55qE6YC86L+R5Yiw6aG56Zuo5LiJ57Gz6IyD5Zu05YaF77yM5q+r5LiN55WZ5oOF55qE5Li+6LW36ZOB5ouz6Iis55qE5ouz5aS077yM5ZCR552A5LuW55qE5ZGo6Lqr6KaB5a6z56C45LqG6L+H5Y6744CCPC9wPg=='));</script>
<p>请勿开启浏览器阅读模式，否则将导致章节内容缺失及无法阅读下一章。</p>
<p style="font-size:12px;">相邻推荐:<a href="/novel/3992/">在柯南世界的悠闲生活</a>&nbsp;&nbsp;<a href="/novel/3983/">快穿：抢女主的男人会上瘾诶</a>&nbsp;&nbsp;<a href="/novel/3979/">华娱，不放纵能叫影帝吗？</a>&nbsp;&nbsp;<a href="/novel/3996/">华娱之女明星请自重</a>&nbsp;&nbsp;<a href="/novel/3978/">他嫌弃的病秧妻子，竟是白月光女帝</a>&nbsp;&nbsp;<a href="/novel/3990/">恶役少爷不想要破灭结局</a>&nbsp;&nbsp;<a href="/novel/3984/">这就是牌佬的世界吗？亚达贼！</a>&nbsp;&nbsp;<a href="/novel/3991/">综漫：我的女友是喰种</a>&nbsp;&nbsp;<a href="/novel/3982/">谢邀！人在小精灵，刚辞冠军</a>&nbsp;&nbsp;<a href="/novel/3987/">《归来复仇：侯夫人她又狠又飒》云翘沈煦</a>&nbsp;&nbsp;<a href="/novel/3980/">凤仪天下：紫禁幽澜</a>&nbsp;&nbsp;<a href="/novel/3994/">终极一班：无双</a>&nbsp;&nbsp;<a href="/novel/3985/">穿越艾尼路的我竟然不是群主</a>&nbsp;&nbsp;<a href="/novel/3995/">我的式神老婆超强</a>&nbsp;&nbsp;<a href="https://www.suyuege.com/novel/17865.html" target="_blank">综漫：系统忽悠我后直接跑了</a>&nbsp;&nbsp;<a href="/novel/3988/">人在不列颠，宝具神之键</a>&nbsp;&nbsp;<a href="/novel/3986/">从水元素开始的进化</a>&nbsp;&nbsp;<a href="/novel/3993/">武侠：陆地神仙被邀月挖出来</a>&nbsp;&nbsp;<a href="/novel/3981/">僵尸世界：快去请老祖出棺！</a>&nbsp;&nbsp;<a href="/novel/3989/">末世：打脸？不不！我在囤物资</a>&nbsp;&nbsp;</p>
<div class="read_btn">
 <a href="/novel/3998/read_1895234.html">上一章</a><a href="/novel/3998/">章节目录</a><a rel="nofollow" onclick="shuqian(3998,1895235,'https%3A%2F%2Fwww.wxsy.net%2Fnovel%2F3998%2Fread_1895235.html')">保存书签</a><a href="/novel/3998/read_1895235_1.html">下一章</a>
</div>
`
r = r.replace(/<script>\s*document\.writeln\(qsbs\.bb\('([^']+)'\)\);\s*<\/script>/g, function (a, b) {
  return qsbs.bb(b)
});
r = r.replace(/<p[^>]*?>相邻推荐:([\s\S]*?)<\/p>/, '');

console.log(r)