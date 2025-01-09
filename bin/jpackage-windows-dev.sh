#

rm -rf dist

mvnd clean package "-Dmaven.test.skip=true"

mkdir target/jpackage
mv target/app-jar-with-dependencies.jar target/jpackage

# app-image 表示免安装版，部分参数不支持
# 换行符 psh 为 `，cmd 为 ^，linux 为 \
jpackage \
--name "SoNovel" \
--type "app-image" \
--win-console \
--input "target/jpackage" \
--dest dist \
--icon assets/logo.ico \
--app-version 1.5.3 \
--copyright "Copyright (C) 2024 pcdd. All rights reserved." \
--description "开源搜书神器" \
--vendor "github.com/freeok" \
--main-jar "app-jar-with-dependencies.jar"

cp config.ini dist/SoNovel
rm dist/SoNovel/*.ico




