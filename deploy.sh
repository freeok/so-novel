mvnd clean package "-Dmaven.test.skip=true"

# --win-console 控制台应用程序
# --win-shortcut 桌面上创建快捷方式
# --win-dir-chooser 用户可以选择安装目录
# --win-menu 添加到开始菜单（搜索用）
# vendor 发布者

jpackage --name "SoNovel" `
--win-console `
--win-dir-chooser `
--win-shortcut `
--win-menu `
--input . `
--icon assets/logo.ico `
--app-version 1.5.0 `
--vendor "pcdd" `
--description "开源搜书神器" `
--main-jar target/app-jar-with-dependencies.jar