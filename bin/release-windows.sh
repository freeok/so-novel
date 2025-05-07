#!/bin/bash
set -e  # 出现错误立即退出

# launch4j-maven-plugin 仅支持 windows，mvn package 触发
# Windows 发布脚本 (x86_64)
# JDK 升级后需要修改下面 2 个版本号

# 配置部分
jre_filename="jre-17.0.11+9-x64_windows.tar.gz"
jre_dirname="jdk-17.0.11+9-jre"
dist_filename="sonovel-windows.tar.gz"

# 获取项目根目录
project_path="$( cd "$(dirname "$0")"/.. && pwd )"
dist_path="$project_path/dist"
target_dir="$project_path/target/SoNovel"

prepare_dist_dir() {
    mkdir -p "$dist_path"
}

run_maven() {
    mvn clean package -Pwindows-x86_64 -Dmaven.test.skip=true -DjrePath=runtime -Denv=prod
}

copy_files() {
    cp config.ini bundle/SoNovel.l4j.ini bundle/readme.txt "$target_dir"
    cp -r bundle/fonts "$target_dir/"
    cp "bundle/$jre_filename" "$target_dir"
}

extract_jre() {
    cd "$target_dir"
    tar zxf "$jre_filename"
    rm "$jre_filename"
    mv "$jre_dirname" runtime
}

package_artifacts() {
    cd "$project_path/target"
    tar czf "$dist_filename" SoNovel
    mv "$dist_filename" "$dist_path"
}

main() {
    cd "$project_path"
    prepare_dist_dir
    run_maven
    copy_files
    extract_jre
    package_artifacts
    echo "Windows done!"
}

main