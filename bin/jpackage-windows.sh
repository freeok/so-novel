# jpackage 支持 Windows、MacOS

mvn clean package "-DskipTests"

mkdir -p nobug/jpackage
mv nobug/app-jar-with-dependencies.jar nobug/jpackage

jpackage --version
echo "开始构建exe"

# GitHub Actions 日志: "Error: Invalid Option: [target]" 一个奇怪的错误
jpackage --name "SoNovel" --type "app-image" --input "nobug/jpackage" --dest dist --icon input/logo.ico --app-version 1.5.3 --copyright "Copyright (C) 2024 pcdd. All rights reserved." --description "开源搜书神器" --vendor "github.com/pcdd-group" --main-jar "app-jar-with-dependencies.jar" --win-console

# cp: cannot create regular file 'dist/SoNovel': No such file or directory
cp config.ini dist/SoNovel
# rm: cannot remove ‘dist/Sollovel/*.ico′: Noj such file or directory
rm dist/SoNovel/*.ico

echo "开始打包压缩"
tar czvf dist/sonovel-win.tar.gz dist/SoNovel

# 加上以下代码，工作流不会执行失败，很神奇
#cd dist/SoNovel
#pwd
#ls

# --win-console 控制台应用程序
# --win-shortcut 桌面上创建快捷方式
# --win-dir-chooser 用户可以选择安装目录
# --win-menu 添加到开始菜单（搜索用）
# --vendor 应用程序的供应商（作者）
# --input 待打包文件所在输入目录的路径
# --dest 生成的输出文件放置的路径
# --install-dir 默认安装位置下面的相对子路径
# --about-url 应用程序主页的 URL
# 参数有 : 是非法字符，放在尾行

# app-image 表示免安装版，部分参数不支持
#jpackage \
#--name "SoNovel" \
#--type "app-image" \
#--win-console \
#--input "target/tmp" \
#--dest dist \
#--icon input/logo.ico \
#--app-version 1.5.3 \
#--copyright "Copyright (C) 2024 pcdd. All rights reserved." \
#--description "开源搜书神器" \
#--vendor "github.com/pcdd-group" \
#--main-jar "app-jar-with-dependencies.jar"

# msi 表示安装版
#jpackage `
#--type "msi" `
#--name "SoNovel" `
#--input "target/tmp" `
#--dest dist `
#--icon input/logo.ico `
#--app-version 1.5.3 `
#--copyright "Copyright (C) 2024 pcdd. All rights reserved." `
#--description "开源搜书神器" `
#--install-dir "SoNovel" `
#--win-console `
#--win-dir-chooser `
#--win-shortcut `
#--win-shortcut-prompt `
#--win-menu `
#--vendor "github.com/pcdd-group" `
#--about-url "github.com/pcdd-group/so-novel" `
#--win-update-url "github.com/pcdd-group/so-novel/release" `
#--main-jar app-jar-with-dependencies.jar