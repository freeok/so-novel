project_path=$(
  cd "$(dirname "$0")" || exit
  cd ..
  pwd
)
cd "$project_path" || exit

mvn clean package "-Dmaven.test.skip=true"
mkdir -p nobug/jpackage
mv nobug/app-jar-with-dependencies.jar nobug/jpackage

echo "开始构建 exe"
echo "jpackage version: $(jpackage --version)"
jpackage \
--name "SoNovel" \
--type "app-image" \
--win-console \
--input "nobug/jpackage" \
--dest dist \
--icon assets/logo-1.ico \
--app-version 1.7.4 \
--copyright "Copyright (C) 2025 SoNovel. All rights reserved." \
--description "开源搜书神器" \
--vendor "FreeOK" \
--main-jar "app-jar-with-dependencies.jar"

cp config.ini input/readme.txt dist/SoNovel
rm dist/SoNovel/*.ico

echo "开始压缩打包"
tar czvf dist/sonovel-win.tar.gz dist/SoNovel