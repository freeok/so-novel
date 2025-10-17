Windows 用户
运行 sonovel.exe

macOS 用户
运行 run-macos.sh

Linux 用户
运行 run-linux.sh


温馨提示
- 为获得最佳使用体验，请将终端窗口最大化。
- config.ini 是配置文件，每个配置项有对应的注释，修改保存后需重启应用。
- 如果认为下载速度较慢，适当减小爬取间隔可能有助于提高速度，直到达到合适的平衡。
- 设置过小的爬取间隔会导致部分书源封禁 IP，从而无法使用。
- 如果书名搜不到，就用作者名称搜，反之亦然。


WebUI 模式用法
- config.ini 开启 Web 服务
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
