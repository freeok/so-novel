mvn clean package "-DskipTests"

cd target
mkdir tmp
mv app-jar-with-dependencies.jar tmp
mv classes tmp
cd ..

echo $PSVersionTable.PSVersion
jpackage --version
echo "开始构建exe"
# app-image 表示免安装版，部分参数不支持
#jpackage --name "SoNovel" --type "app-image" --win-console --input "target/tmp" --dest dist --icon assets/logo.ico --app-version 1.5.1 --copyright "Copyright (C) 2024 pcdd. All rights reserved." --description "开源搜书神器" --vendor "github.com/pcdd-group" --main-jar "app-jar-with-dependencies.jar"
jpackage --name "SoNovel" --type "app-image" --input "target/tmp" --dest dist --icon assets/logo.ico --app-version 1.5.1 --copyright "Copyright (C) 2024 pcdd. All rights reserved." --description "开源搜书神器" --vendor "github.com/pcdd-group" --main-jar "app-jar-with-dependencies.jar --win-console"
echo "开始压缩 zip"
tar czvf dist/SoNovel-Portable.tar.gz dist/SoNovel

# powershell 压缩为 zip
#Compress-Archive -Path dist\SoNovel -DestinationPath dist\SoNovel-Portable.zip


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

# msi 表示安装版
#jpackage `
#--type "msi" `
#--name "So Novel" `
#--input "target/tmp" `
#--dest dist `
#--icon assets/logo.ico `
#--app-version 1.5.1 `
#--copyright "Copyright (C) 2024 pcdd. All rights reserved." `
#--description "开源搜书神器" `
#--install-dir "So Novel" `
#--win-console `
#--win-dir-chooser `
#--win-shortcut `
#--win-shortcut-prompt `
#--win-menu `
#--vendor "github.com/pcdd-group" `
#--about-url "github.com/pcdd-group/so-novel" `
#--win-update-url "github.com/pcdd-group/so-novel/release" `
#--main-jar app-jar-with-dependencies.jar
