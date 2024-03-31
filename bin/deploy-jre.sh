# launch4j-maven-plugin 仅支持 windows

# 项目根目录，根据当前文件所在路径获取相对路径
project_path=$(
  cd "$(dirname "$0")" || exit
  cd ..
  pwd
)
cd "$project_path" || exit

# 指定 jre 目录为当前 runtime 目录
mvn clean package -DskipTests -DjrePath=runtime
cp config.ini target/SoNovel

cd target
tar czvf sonovel-win.tar.gz SoNovel