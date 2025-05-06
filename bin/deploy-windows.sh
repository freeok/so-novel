#!/bin/bash
# ====================================================
# launch4j-maven-plugin 仅支持 windows，mvn package 触发
# JDK 升级后需要修改下面 2 个版本号
# ====================================================

# JRE 压缩文件名
jre_filename="jre-17.0.11+9-x64_windows.tar.gz"
# JRE 解压后的目录名
jre_dirname="jdk-17.0.11+9-jre"

# 压缩包文件名
artifacts="sonovel-windows.tar.gz"
# -DskipTests：不执行测试用例。-Dmaven.test.skip=true：不执行测试用例，也不编译测试用例类
maven_command="mvn clean package -Dmaven.test.skip=true -DjrePath=runtime -Denv=prod"

# 项目根目录，根据当前文件所在路径获取相对路径
project_path=$(
  cd "$(dirname "$0")" || exit
  cd ..
  pwd
)
cd "$project_path" || exit
# -p 表示：如果存在则没有错误，根据需要创建父目录
mkdir -p dist/

$maven_command
# 拷贝配置文件、使用说明
cp config.ini bundle/SoNovel.l4j.ini bundle/readme.txt target/SoNovel
cp -r bundle/fonts target/SoNovel/
# 拷贝环境依赖（JRE）
cp "bundle/$jre_filename" target/SoNovel

cd target/SoNovel
tar zxf "$jre_filename" && rm "$jre_filename"
mv "$jre_dirname" runtime
cd ..
tar czf $artifacts SoNovel
mv $artifacts $project_path/dist

echo Windows done!