function func(input) {
  // 从输入的字符串中提取书籍ID和章节文件名
  var matches = input.match(/javascript:Chapter\('(\d+)','(\d+)\.html'\)/);
  // 如果正则匹配成功
  if (matches) {
    return "https://www.369book.cc/read/" + matches[1] + "/" + matches[2] + ".html";
  }
  // 如果输入格式不正确，返回原始输入
  return input;
}

// 示例调用
var input = "https://www.369book.cc/javascript:Chapter('341494','66302420.html')";
console.log(func(input));