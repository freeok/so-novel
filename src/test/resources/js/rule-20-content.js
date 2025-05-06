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
r = ''
r = r.replace(/<script>\s*document\.writeln\(qsbs\.bb\('([^']+)'\)\);\s*<\/script>/g, function (a, b) {
  return qsbs.bb(b)
});
r = r.replace(/<p>相邻推荐:[\s\S]*$/, '');

console.log(qsbs.bb('PHA+4oCc5ZiO5ZCx77yB4oCdPC9wPg=='));
console.log(qsbs.bb('PHA+5LiA6L6G57qi55m955u46Ze055qE5o235YWL5pav56eR6L6+5YWs5Lqk6L2m5Zyo5YmN6Zeo6ZmE6L+R5YGc5LqG5LiL5p2l44CCPC9wPg=='));
console.log(qsbs.bb('PHA+546w5Zyo5o235YWL6L+Y5piv57qi6Imy6Zi16JCl77yM5YWo56ew5Y+r5o235YWL5pav5rSb5LyQ5YWL56S+5Lya5Li75LmJ5YWx5ZKM5Zu977yM6L+Z5bm05aS06L+b5Y+j55qE5Y2h6L2m5ZKM5a6i6L2m77yM5b6I5aSa6YO95piv5Lic5qyn5Lqn55qE77yM5pav5p+v6L6+5ZCO5p2l6KKr5aSn5LyX5pS26LSt5LqG44CCPC9wPg=='));
console.log(qsbs.bb('PHA+5Lit6Z2S5oql55qE6ICB6K6w6ICF55ub5rC45b+X6LS55Yqy55qE5oyk5LqG5LiL5p2l77yM5pOm5LqG5pOm5rGX77yM55CG5LqG55CG5qOJ6LSo6KGs6KGr77yM6K6p6KGj552A55yL5LiK5Y675pW05rSB5LiA54K544CCPC9wPg=='));
console.log(qsbs.bb('PHA+5LuW5LiA55u05Zyo5Lit6Z2S5oql5bel5L2c77yM6ZqP552A5oql57q45aSN5YiK6ICM5Zue5b2S5bKX5L2N77yM5L2G5bmy5LiN5LqG5Yeg5bm05Lmf6KaB6YCA5LyR5LqG77yM5oql56S+5bm06L275Lq65bCR77yM5b6I5rOo6YeN5Z+55YW75LiL5LiA5Luj44CCPC9wPg=='));
console.log(qsbs.bb('PHA+4oCc5bCP5LqO77yM5b+r54K577yB4oCdPC9wPg=='));
console.log(qsbs.bb('PHA+4oCc5p2l5LqG5p2l5LqG77yB4oCdPC9wPg=='));
console.log(qsbs.bb('PHA+5LqO5L2z5L2z5b+r6LeR5Yeg5q2l6LW25Yiw6L+R5YmN44CCPC9wPg=='));
console.log(qsbs.bb('PHA+5aW5MjDlsoHlh7rlpLTvvIzpoofmnInlh6DliIblp7/oibLvvIzliJrliJrmjqXmm7/niLbkurLnmoTlspfkvY3vvIzmiJDkuLrkuobkuIDlkI3oj5zpuJ/orrDogIXvvIzlpbnmirHnnYDkuKrljIXvvIzph4zpnaLmmK/kuIDpg6jnj43otLXml6Dmr5TnmoTnhafnm7jmnLrjgII8L3A+'));
console.log(qsbs.bb('PHA+4oCc5bCP5LqO77yM5LiA5Lya5L2g5Lmf6Zeu5Yeg5Liq6Zeu6aKY44CC4oCdPC9wPg=='));
console.log(qsbs.bb('PHA+4oCc5ZWK77yf5oiR5LiN55+l6YGT6Zeu5LuA5LmI5ZGA44CC4oCdPC9wPg=='));
console.log(qsbs.bb('PHA+4oCc5b2T6K6w6ICF5oCO5LmI6IO95LiN55+l6YGT6Zeu5LuA5LmI77yM5bim5L2g5Ye65p2l5bCx5piv6K6p5L2g5aSa5Y6G57uD77yM5aSa56ev57Sv57uP6aqM77yM5L2g5aW95aW95oOz5oOz44CC4oCdPC9wPg=='));
console.log(qsbs.bb('PHA+5L+p5Lq66LWw5LqG5LiA5q6177yM5Yiw5LqG566t5qW85Lic5L6n77yM5p6c54S255yL6KeB5LiA5Liq6Iy25pGK77yM5Y2B5p2l5Liq5Lq65q2j5Zyo5b+Z56KM77yM6aG+5a6i6Z2e5bi46Z2e5bi45aSa44CCPC9wPg=='));
console.log(qsbs.bb('PHA+55ub5rC45b+X5rKh6KGo5piO6Lqr5Lu977yM6ICM5piv5o6S6Zif5LiK5YmN77yM6YGT77ya4oCc5p2l5Lik56KX6Iy277yB4oCdPC9wPg=='));
console.log(qsbs.bb('PHA+4oCc5aW95Zie77yB4oCdPC9wPg=='));
console.log(qsbs.bb('PHA+4oCc5LiA5YWx5Zub5YiG6ZKx77yB4oCdPC9wPg=='));
console.log(qsbs.bb('PHA+6buE5Y2g6Iux5oSI5Y+R54af57uD55qE5oub5ZG877yM55ub5rC45b+X56uv6LW35aSn56KX556n5LqG556n77yM6Ze75LqG6Ze777yM54S25ZCO5omN5Zad5LqG5LiA5Y+j77yM56yR6YGT77ya4oCc6L+Z5piv6IyJ6I6J6Iqx6Iy277yf4oCdPC9wPg=='));
console.log(qsbs.bb('PHA+4oCc5piv5ZGi77yB4oCdPC9wPg=='));
console.log(qsbs.bb('PHA+4oCc5Lqs5Z+O5rC06LSo5LiN5aW977yM6IyJ6I6J6Iqx6Iy25LiN5oyR5rC06LSo77yM5pyA6YCC5ZCI5pGG5pGK44CC4oCdPC9wPg=='));
console.log(qsbs.bb('PHA+4oCc5ZOf77yM5oKo6L+Y5piv6KGM5a6277yM5oKo5piv5Lqs5Z+O5pys5Zyw5Lq65ZCn77yf4oCd6buE5Y2g6Iux54Wn5L6L6Zmq6IGK44CCPC9wPg=='));
console.log(qsbs.bb('PHA+4oCc5Zev77yM5L2g5Lus5q+P5aSp6YO96L+Z5LmI5aSa6aG+5a6i77yf4oCdPC9wPg=='));
console.log(qsbs.bb('PHA+4oCc5beu5LiN5aSa77yM546w5Zyo55+l6YGT5oiR5Lus55qE5Lq66LaK5p2l6LaK5aSa5LqG77yM5aSn5a626YO95b6I5pSv5oyB44CC4oCdPC9wPg=='));
console.log(qsbs.bb('PHA+55ub5rC45b+X6IGK5LqG5LiA5Lya77yM5omN56yR552A5Ly45Ye65omL77yM6YGT77ya4oCc5L2g5aW977yM5q2j5byP6Ieq5oiR5LuL57uN5LiA5LiL77yM5oiR5piv5Lit6Z2S5oql55qE6K6w6ICF55ub5rC45b+X77yM6L+Z5L2N5piv5bCP5LqO5ZCM5b+X77yM5oiR5Lus5p2l6YeH6K6/44CC4oCdPC9wPg=='));
console.log(qsbs.bb('PHA+4oCc6YeH6YeH6YeH77yM6YeH6K6/77yf77yf4oCdPC9wPg=='));
console.log(qsbs.bb('PHA+6buE5Y2g6Iux5LiA5LiL5oe15LqG77yM57uT5be06YGT77ya4oCc5Li65LuA5LmI6YeH6K6/5oiR5Lus5ZWK77yf4oCdPC9wPg=='));