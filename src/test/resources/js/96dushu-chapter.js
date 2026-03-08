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

r = r.replace(/<script>\s*document\.writeln\(qsbs\.bb\('([^']+)'\)\);\s*<\/script>/g, function (a, b) {
  return qsbs.bb(b)
});

console.log(r)