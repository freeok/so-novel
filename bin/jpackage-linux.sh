# 测试通过

rm -rf dist

mvn clean package "-DskipTests"

mkdir target/jpackage
mv target/app-jar-with-dependencies.jar target/jpackage

# 在 linux 环境会转换为 .deb 文件
# type=app-image 记得去掉--about-url
jpackage \
--name "SoNovel" \
--input "target/jpackage" \
--dest dist \
--icon input/logo.ico \
--app-version 1.5.3 \
--copyright "Copyright (C) 2024 pcdd. All rights reserved." \
--description "开源搜书神器" \
--vendor "github.com/pcdd-group" \
--about-url "github.com/pcdd-group/so-novel" \
--main-jar "app-jar-with-dependencies.jar"

cp config.ini dist/SoNovel
rm dist/SoNovel/*.ico

echo "开始打包压缩"
tar czvf dist/SoNovel_linux.tar.gz dist/SoNovel
