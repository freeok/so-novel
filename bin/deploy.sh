# launch4j-maven-plugin 仅支持 windows

# 定义 Maven 命令并保存到变量中
maven_command=""
artifacts=""
# 根据传入的参数执行不同的操作
if [ "$1" == "jre" ]; then
  maven_command="mvn clean package -DskipTests -DjrePath=runtime"
  artifacts="sonovel-win-with-jre.tar.gz"
else
  maven_command="mvn clean package -DskipTests"
  artifacts="sonovel-win.tar.gz"
fi

# 项目根目录，根据当前文件所在路径获取相对路径
project_path=$(
  cd "$(dirname "$0")" || exit
  cd ..
  pwd
)
cd "$project_path" || exit

$maven_command
cp config.ini target/SoNovel
if [ "$1" == "jre" ]; then
  cp input/*.txt input/*.rar target/SoNovel
fi

cd target
tar czvf $artifacts SoNovel
