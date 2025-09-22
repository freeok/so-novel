#!/bin/bash
set -e  # 出现错误立即退出

# launch4j-maven-plugin 仅支持 windows，mvn package 触发
# Windows 发布脚本 (x86_64)

# 最小 JRE，JDK 升级后要修改文件名版本号
jre_filename="jre-21.0.8+9-x64_windows.tar.gz"
dist_filename="sonovel-windows.tar.gz"

# 获取项目根目录
project_path="$( cd "$(dirname "$0")"/.. && pwd )"
dist_path="$project_path/dist"
target_dir="$project_path/target/SoNovel"

prepare_dist_dir() {
    mkdir -p "$dist_path"
}

run_maven() {
    mvn clean package -Pwindows-x86_64 '-Dmaven.test.skip=true' '-DjrePath=runtime'
}

copy_files() {
    cp "bundle/$jre_filename" "$target_dir"
    cp "target/app-jar-with-dependencies.jar" "$target_dir/app.jar"
    cp -r bundle/rules "$target_dir/"
    cp bundle/config.ini bundle/sonovel.l4j.ini bundle/readme.txt "$target_dir"
    cp "bundle/支持 & 赞助.png" "$target_dir"
    # 创建chcp批处理脚本
    cat > "$target_dir/run.bat" <<EOF
@echo off
chcp 65001 >nul
start "" "sonovel.exe"
EOF
}

extract_jre() {
    cd "$target_dir"
    tar zxf "$jre_filename"
    rm "$jre_filename"
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