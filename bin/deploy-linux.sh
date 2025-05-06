#!/bin/bash
# ====================================================
# JDK 升级后需要修改下面 2 个版本号
# ====================================================

# JRE 压缩文件名
jre_filename="jre-17.0.12+7_linux.tar.gz"
# JRE 解压后的目录名
jre_dirname="jdk-17.0.12+7-jre"

# 最终产物的压缩文件名
dist_filename="sonovel-linux.tar.gz"
# 解压后的目录名
dist_dirname="SoNovel-Linux"

project_path=$(
  cd "$(dirname "$0")" || exit
  cd ..
  pwd
)
cd "$project_path" || exit

mvn clean package -Dmaven.test.skip=true -DjrePath=runtime
mkdir -p dist
mkdir "target/$dist_dirname"

# 复制配置文件、使用说明、启动脚本、JRE
cp config.ini bundle/readme.txt bundle/linux-run.sh "bundle/$jre_filename" "target/$dist_dirname"
cp -r bundle/fonts "target/$dist_dirname"

cd target
# 重命名
mv app-jar-with-dependencies.jar app.jar
cp app.jar "$dist_dirname"

cd "$dist_dirname"
# 解压
tar zxf "$jre_filename" && rm "$jre_filename"
# 重命名
mv "$jre_dirname" runtime
cd ..
# 压缩
tar czf "$dist_filename" "$dist_dirname"
# 剪切
mv "$dist_filename" $project_path/dist

echo Linux done!