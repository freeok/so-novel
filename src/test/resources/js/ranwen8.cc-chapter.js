var qsbs = {
  _keyStr: "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",
  bb: function (input) {
    var output = "";
    var chr1, chr2, chr3;
    var enc1, enc2, enc3, enc4;
    var i = 0;
    input = input.replace(/[^A-Za-z0-9+\/=]/g, "");
    while (i < input.length) {
      enc1 = this._keyStr.indexOf(input.charAt(i++));
      enc2 = this._keyStr.indexOf(input.charAt(i++));
      enc3 = this._keyStr.indexOf(input.charAt(i++));
      enc4 = this._keyStr.indexOf(input.charAt(i++));
      chr1 = (enc1 << 2) | (enc2 >> 4);
      chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
      chr3 = ((enc3 & 3) << 6) | enc4;
      output = output + String.fromCharCode(chr1);
      if (enc3 != 64) {
        output = output + String.fromCharCode(chr2)
      }
      if (enc4 != 64) {
        output = output + String.fromCharCode(chr3)
      }
    }
    output = qsbs._utf8_decode(output);
    return output
  }, _utf8_encode: function (string) {
    string = string.replace(/\r\n/g, "\n");
    var utftext = "";
    for (var n = 0; n < string.length; n++) {
      var c = string.charCodeAt(n);
      if (c < 128) {
        utftext += String.fromCharCode(c)
      } else if ((c > 127) && (c < 2048)) {
        utftext += String.fromCharCode((c >> 6) | 192);
        utftext += String.fromCharCode((c & 63) | 128)
      } else {
        utftext += String.fromCharCode((c >> 12) | 224);
        utftext += String.fromCharCode(((c >> 6) & 63) | 128);
        utftext += String.fromCharCode((c & 63) | 128)
      }
    }
    return utftext
  }, _utf8_decode: function (utftext) {
    var string = "";
    var i = 0;
    var c = c1 = c2 = 0;
    while (i < utftext.length) {
      c = utftext.charCodeAt(i);
      if (c < 128) {
        string += String.fromCharCode(c);
        i++
      } else if ((c > 191) && (c < 224)) {
        c2 = utftext.charCodeAt(i + 1);
        string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
        i += 2
      } else {
        c2 = utftext.charCodeAt(i + 1);
        c3 = utftext.charCodeAt(i + 2);
        string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
        i += 3
      }
    }
    return string
  }
}

r = `
<br>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A7MDAxLuWwj+S7memYv+aZiw=='));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A75L2V5pmL5Zyo5LqM6aOf5aCC5omT5LqG5Liq57OW6YaL5o6S6aqo5ZKM55m96I+c6IKJ57OK77yM5bCx56uv552A6aOf55uS5oWi5oKg5oKg5Zyw5b6A5a6/6IiN6YeM6LWw44CC'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A76YCa5b6A55qE5a6/6IiN5qW855qE5bCP6YGT5LiK77yM6JC95LqG5LiA5Zyw55qE6ZO25p2P5ZKM5qKn5qGQ5Y+25a2Q77yM6buE6KSQ6Imy6YeM5aS5552A54mH54mH6YeR5omH77yM6Lip5Zyo6ISa5LiL5Y+R5Ye65ZiO5ZCx5ZiO5ZCx55qE5ZON5aOw44CC'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A76Lqr6L6557uP6L+H5LiA576k6KOF5omu6Z2T5Li955qE5aWz55Sf77yM54aZ54aZ5pSY5pSY77yM5LiA6Lev55a+6LWw77yM5Ly05aiH5L+P55qE56yR5aOw77yM5aW55Lus5omL6YeM5ouO552A5b2p55CD5LiA57G75ZCG5Zad5Yqp6Zi155So55qE6YGT5YW377yM6LCI6K+d6Ze05ZCQ6Zyy5Ye64oCc572R55CD4oCd'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A744CB4oCc5qCh6I2J4oCd'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A76L+Z5qC355qE5YWz6ZSu6K+N77yM5rSL5rqi55qE54Ot5oOF5rW45p+T552A56iN5pi+5Ya35rap55qE5rex56eL77yM5Y+q5piv55yL552A5aW55Lus5Zyo6L+Z5Liq5a2j6IqC6L+Y6KO4fOmcsuWcqOWklueahOS4gOadoeadoeWkp+mVv+iFv++8jOS9leaZi+mDveW/jeS4jeS9j+aKluS6huaKluOAgg=='));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A75YmN5pa555qE5bKU6YGT6YCa5ZCR5a2m5qCh572R55CD5Zy677yM55yL5p2l5LuK5aSp5LiL5Y2I5Y+I6KaB5Li+5Yqe5LuA5LmI6YeN6YeP57qn55qE5q+U6LWb5LqG77yM6ICM5LiU77yM5aWz55Sf5Y+j5Lit55qE4oCc5qCh6I2J4oCd'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A75Lmf5Lya5Y+C5Yqg44CC'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A75L2G5piv77yM6L+Z5Lqb5L2V5pmL6YO95LiN5YWz5b+D4oCU4oCU5Zyo6L+Z5Liq5qCh5Zut5bqm6L+H5LqG5Lik5bm05Y2K77yM6K+l5paw6bKc55qE6YO95paw6bKc6L+H5LqG77yM6K+l57uP5Y6G55qE5LuW5Lmf6YO957uP5Y6G5LqG44CC'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A75aSn5LiJ5piv5LiT5Lia6K++5pyA57mB5b+Z55qE5LiA5bm077yM5bCk5YW25LiL5Y2K5a2m5pyf5byA5aeL5ZCO77yM5aSn5a2m55Sf5rS755qE6L275p2+54OC5ryr5Ly85LmO5bey57uP6L+c5Y6777yM6ICM5a+55pyq5p2l55qE54Sm54G85LiO5b235b6o5oSf5byA5aeL5Zyo5bm057qn6YeM6YCQ5riQ6JST5bu244CC'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A75L2V5pmL5Y+55LqG5Y+j5rCU77yM5Zue5oOz552A5pio5pma55qE5LiA6YCa5a6255S177yM6YKj5Liq5aWz5Lq65Zyo55S16K+d6YeM6K+077yM5LuW54i45Zyo6ICB5a625Li65LuW5a6J5o6S5aW95LqG5bel5L2c77yM5piv55+l5ZCN55qE5LqL5Lia5Y2V5L2N77yM5YaF6YOo5bCx6IO95o6o6I2Q5YGa5YWs5Yqh5ZGY77yM5bel6LWE6auY56aP5Yip5aW977yM562J5LuW5LiA5q+V5Lia5bCx6IO95Zue5Y675LiK54+t4oCm4oCm'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A75LiN55+l5oCO5LmI55qE77yM5L2V5pmL5bCx5oSf6KeJ5pyJ54K55Y6M54Om44CC'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A75b+r5Yiw5a6/6IiN5qW85pe277yM6L+O6Z2i56qB54S25Yay5Ye65p2l5Yeg5Liq5Lq65b2x77yM6I695pKe5Zyw5pOm552A5LuW55qE6IOz6IaK6LeR6L+H77yM5bim6LW36Zi16Zi16aOO5bCY77yM5oGw6YCi5L2V5pmL5Zyo5Ye656We77yM5oqx552A6aWt55uS5YK75oSj5Zyo5Y6f5Zyw77yM5Lul6Iez6Zif5YiX5pyA5ZCO6YKj5Lq66Zeq6YG/5LiN5Y+K77yM6Lef5LuW5pKe5LqG5Liq5ruh5oCA44CC'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A74oCc5ZOQ5b2T4oCd'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A75LiA5aOw77yM5L2V5pmL5LiA5Liq6LaU6LaE77yM6aOf55uS6YO95pGU5Zyo5LqG5Zyw5LiK44CC'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A74oCc5a+55LiN6LW34oCm4oCm4oCd'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A76YKj5Lq65ouJ5LqG5LuW5LiA5oqK77yM5b6F5LuW56uZ56iz5LqG5omN5byv6IWw5pu/5LuW5o2h6LW355uS5a2Q77yM4oCc5rKh57uZ5L2g5pGU5Z2P5ZCn77yf4oCd'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A74oCc5rKh5LqL44CC4oCd'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A75L2V5pmL5o6l6L+H55uS5a2Q77yM6L+Z5pivbG9ja+WHuueahOS4gOasvuacieacuueOu+eSg+mlreebku+8jOWvhuWwgeaAp+S4jemUme+8jOaRlOS4jeeijuS5n+eguOS4jeeDgu+8jOeul+i/meWwj+WtkOi/kOawlOWlve+8jOS4jeeUqOi1lOS6huOAgg=='));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A75L2V5pmL5oqs55y85omT6YeP5LqG5LuW5LiA55y877yM5Y+q6KeB6YKj5Lq656m/552A5LiA5Lu26JOd55m955u46Ze055qEcG9sb+ihq++8jOaKq+edgOiAkOWFi+eahOS8kemXsuWkluWll++8jOS6uumrmOiHgumVv++8jOS4gOWktOWIqee0oueahOefreWPke+8jOeojeaYvueLremVv+eahOWPjOecvO+8jOWYtOWUh+aKv+edgO+8jOeci+i1t+adpeacieeCueS4jeiLn+iogOeskeOAgg=='));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A74oCU4oCU5piv572R55CD6Zif55qE77yM5oCl552A6LW25Y675q+U6LWb77yf'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A75L2V5pmL5pGG5pGG5omL56S65oSP55yf55qE5rKh5LqL77yM5Lmf5rKh5YaN6K+05LuA5LmI77yM5oqx552A6aWt55uS5LiO5LuW6ZSZ6Lqr6ICM6L+H44CC'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A75Yeg5bm05YmN77yM5YWo5Zu96auY5qCh6IGU55uf5Y+35Y+s5ZCE5aSn6Zmi5qCh5pS55ZaE5a2m55Sf5L2P5a6/546v5aKD77yM5ZCR5aSW5Zu95ZCN5qCh6Z2g5oui77yM5bm255Sx5pS/5bqc57uZ6YOo5YiG6YeN54K56Zmi5qCh5ouo5qy+77yM54m555So5LqO5pS55bu66ZmI5pen5qCh6IiN77yM5L2V5pmL5LiK5aSn5a2m5pe25Yia5aW96LW25LiK5LqG6L+Z5Liq56aP5Yip44CC'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A75LuW5omA5Zyo55qE5Y2O5aSn5bCx5bGe5LqO6KKr5ouo5qy+5pS56YCg55qE6IyD5Zu077yM6ICB5qCh5bqf5byD5LqG6KKr5peg5pWw5qCh5Y+L5ZCQ5qe96L+H55qE4oCc54yq5ZyI5a6/6IiN4oCd'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A777yM5pS55oiQ5LqG5paw5byP55qE5LqM5Lq65ZCI5L2P6Ze077yM5q+P5Lik6Ze05Y2z5Zub5Lq65YWx55So5LiA5Liq6K6o6K665YW855So6aSQ5a6k77yM5pyJ5Yaw566x5pyJ5b6u5rOi54KJ77yM6L+Y5pyJ56m66LCD5ZKM5pqW5rCU77yM5pS56YCg5ZCO55qE5p2h5Lu25aW95b6X6K6p5LiN5bCR56eB56uL5LiJ5pys6Zmi5qCh5a2m55Sf6YO9576h5oWV44CC'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A75L2V5pmL5byA5LqG6Zeo77yM5Lik5Liq5a6/6IiN55qE5YW25L2Z5LiJ5Lq65q2j5Zu05Z2Q5Zyo5qGM6L655ZCD6aWt77yM6KeB5Yiw5LuW77yM5LiA5Lq65Y+r6YGT77ya4oCc5pmL5ZOl77yM5L2g5ZKL6L+Z5LmI5oWi77yf4oCd'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A76K+06K+d55qE5Lq65Y+r5L6v5Lic5b2m77yM5piv5L2V5pmL55qE5ZCM5oi/6Ze06IiN5Y+L77yM6Lqr5p2Q55im5bCP77yM5aGM6by75a2Q5aSn6ICz5buT77yM6ZW/5b6X5YOP5p6B5LqG54y05a2Q77yM5aSW5Y+35b2T54S25Lmf5Y+r4oCc54y05a2Q4oCd'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A744CC'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A74oCc5ouQ5LqG6Laf5a2m5Yqh5aSE5omN5Y676aOf5aCC77yM5omA5Lul5pma5LqG44CC4oCd'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A75L2V5pmL5ouJ5byA56m65bqn5Z2Q5LiL77yM5omT5byA6aWt55uS5Y205YK75LqG55y877yM5Y+q6KeB6YeM5aS055qE5o6S6aqo5ZKM55m96I+c5Zug5Li65Yia5omN55qE5pGU5rua6buP5oiQ5LqG5LiA5Zui77yM5pys5p2l6aOf5aCC6I+c5Y2W55u45bCx5LiN5aW977yM546w5Zyo57qi57qi55m955m955qE57OK54q25pu05piv6K6p5Lq65LiN5b+N55u06KeG44CC'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A74oCc5ZGD5ZGA77yM5L2g6L+Z5omT55qE5LuA5LmI6I+c77yM55yf5oG25b+D44CC4oCd'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A75L6v5Lic5b2m55u055m95Zyw5ZCQ5qe944CC'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A75L2V5pmL77ya4oCc4oCm4oCm4oCd'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A74oCc5a2m5Yqh5aSE77yf5L2g5Y+I6KaB5b+Z5ZWl5rS75Yqo5ZWm77yf4oCd'));</script>
<script>document.writeln(qsbs.bb('PGJyIC8+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A75Y+m5aSW5LiA5Lq66Zeu6YGT44CC'));</script>
`

r = r.replace(/<script>\s*document\.writeln\(qsbs\.bb\('([^']+)'\)\);\s*<\/script>/g, function (a, b) {
  return qsbs.bb(b)
});

console.log(r)