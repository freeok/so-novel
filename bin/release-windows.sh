#!/bin/bash
set -e  # 出现错误立即退出

# =====================
# Windows 发布脚本 (x64)
# =====================

# 最小 JRE，JDK 升级后要修改文件名版本号
jre_filename="jre-21.0.8+9-x64_windows.tar.gz"
dist_filename="sonovel-windows.tar.gz"
dist_dirname="SoNovel"

# 获取项目根目录
project_path="$( cd "$(dirname "$0")"/.. && pwd )"
dist_path="$project_path/dist"
target_dir="$project_path/target/$dist_dirname"

prepare_dist_dir() {
    mkdir -p "$dist_path"
}

run_maven() {
    mvn clean package -Pwindows-x64 '-Dmaven.test.skip=true' '-DjrePath=runtime'
}

copy_files() {
    cp "bundle/$jre_filename" "$target_dir"
    cp "target/app-jar-with-dependencies.jar" "$target_dir/app.jar"
    cp -r bundle/rules "$target_dir/"
    cp bundle/config.ini bundle/sonovel.l4j.ini bundle/readme.txt "$target_dir"
    cp "bundle/支持 & 赞助.png" "$target_dir"
}

extract_jre() {
    cd "$target_dir"
    tar zxf "$jre_filename"
    rm "$jre_filename"
}

package_artifacts() {
    cd "$project_path/target"
    tar czf "$dist_filename" "$dist_dirname"
    mv "$dist_filename" "$dist_path"
}

main() {
    cd "$project_path"
    prepare_dist_dir
    run_maven
    copy_files
    extract_jre
    package_artifacts
    echo "✅ Windows x64 构建完成！产物: $dist_filename"
}

main