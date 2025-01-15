# https://docs.oracle.com/en/java/javase/17/docs/specs/man/jpackage.html
# 执行该脚本前前需注释 pom.xml > launch4j-maven-plugin

export LANG=zh_CN.UTF-8

project_path=$(
  cd "$(dirname "$0")" || exit
  cd ..
  pwd
)
cd "$project_path" || exit

rm -rf dist
mvnd clean package "-Dmaven.test.skip=true"
mkdir target/jpackage
mv target/app-jar-with-dependencies.jar target/jpackage

echo "jpackage version: $(jpackage --version)"
# app-image 表示免安装版，部分参数不支持
# 换行符 psh 为 `，cmd 为 ^，linux 为 \
jpackage \
--name "SoNovel" \
--type "app-image" \
--win-console \
--input "target/jpackage" \
--dest dist \
--icon assets/logo-1.ico \
--app-version 1.7.4 \
--copyright "Copyright (C) 2025 SoNovel. All rights reserved." \
--description "开源搜书神器" \
--vendor "FreeOK" \
--main-jar "app-jar-with-dependencies.jar"

mkdir -p dist/SoNovel
cp config.ini input/readme.txt dist/SoNovel