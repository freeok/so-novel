Windows 用户
运行 sonovel.exe

macOS 用户
运行 run-macos.sh

Linux 用户
运行 run-linux.sh


温馨提示
- 繁體中文版 Windows 使用者請務必在 sonovel.l4j.ini 修改 -Dfile.encoding=Big5
- 为获得最佳使用体验，请将终端窗口最大化。
- 如果书名搜不到，就用作者名称搜，反之亦然。
- config.ini 是配置文件，每个配置项有对应的注释，修改保存后需重启应用。
- 书源规则文件位于 rules 目录下，切换已有书源需要在 config.ini 修改 active-rules 属性


WebUI 模式用法
- 在 config.ini 开启 Web 服务
- 浏览器地址栏输入 localhost:7765

CLI 模式用法
# Windows
.\sonovel.exe -h
# Linux
./runtime/bin/java -jar app.jar -h
# macOS
./runtime/Contents/Home/bin/java -jar app.jar -h
# Docker (不推荐这样用)
docker run -it --rm -v /sonovel/config.ini:/sonovel/config.ini -v /sonovel/downloads:/sonovel/downloads -v /sonovel/rules:/sonovel/rules sonovel:v1.8.5 -h


自定义书源步骤
通过 rules/rule-template.json5 理解 rules/main-rules.json
打开对应网页，按快捷键 Ctrl + Shift + C，单击对应元素后
在开发者工具右击此元素，依次选择复制、复制 selector 或 Xpath
将复制内容粘贴到 rules/rule-template.json5 对应属性
将模板文件 rule-template.json5 重命名为 xx.json (可选)
在 config.ini 修改 active-rules = xx.json

问题反馈
- 使用时遇到问题
- 希望增加某个新功能
上述情况请在此反馈：https://github.com/freeok/so-novel/issues/new/choose
非上述情况在此反馈：https://github.com/so-novel/discussions/new/choose

在提供反馈之前，请务必先在以下链接查找解决方法：
- https://github.com/freeok/so-novel/issues?q=label%3A%22usage%20question%22
- https://github.com/freeok/so-novel/discussions?discussions_q=
如果没有您的情况，欢迎提交反馈！提交前请务必按照要求填写，否则不予处理。


下载地址：https://github.com/freeok/so-novel/releases
书源一览：https://github.com/freeok/so-novel/blob/main/BOOK_SOURCES.md