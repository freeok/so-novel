# JRE 升级后需要修改下面 3 个版本号
# JRE arm64 文件名
jre_filename_arm64="jre-17.0.11+9-arm64_mac.tar.gz"
# JRE x64 文件名
jre_filename_x64="jre-17.0.11+9-x64_mac.tar.gz"
# JRE 解压后的目录名相同
jre_dirname="jdk-17.0.11+9-jre"

# 最终产物的压缩文件名和解压后的目录名
dist_filename_arm64="sonovel-macos_arm64.tar.gz"
dist_filename_x64="sonovel-macos_x64.tar.gz"
dist_dirname_arm64="SoNovel-macOS_arm64"
dist_dirname_x64="SoNovel-macOS_x64"

project_path=$(
  cd "$(dirname "$0")" || exit
  cd ..
  pwd
)
cd "$project_path" || exit

mvn clean package -DskipTests
mkdir -p out
mkdir "target/$dist_dirname_arm64"
mkdir "target/$dist_dirname_x64"

# 复制配置文件、使用说明、启动脚本、JRE
cp config.ini input/readme.txt input/macos-run.sh "input/$jre_filename_arm64" "target/$dist_dirname_arm64"
cp config.ini input/readme.txt input/macos-run.sh "input/$jre_filename_x64" "target/$dist_dirname_x64"

# 复制 jar
cd target
mv app-jar-with-dependencies.jar app.jar
cp app.jar "$dist_dirname_arm64"
cp app.jar "$dist_dirname_x64"

cd "$dist_dirname_arm64"
tar zxf "$jre_filename_arm64" && rm "$jre_filename_arm64"
mv "$jre_dirname" runtime
cd ..

cd "$dist_dirname_x64"
tar zxf "$jre_filename_x64" && rm "$jre_filename_x64"
mv "$jre_dirname" runtime
cd ..

tar czf "$dist_filename_arm64" "$dist_dirname_arm64"
tar czf "$dist_filename_x64" "$dist_dirname_x64"

mv "$dist_filename_arm64" $project_path/out
mv "$dist_filename_x64" $project_path/out

echo done!