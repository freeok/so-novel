# jpackage 支持 Windows、MacOS

mvn clean package "-DskipTests"

mkdir -p nobug/jpackage
mv nobug/app-jar-with-dependencies.jar nobug/jpackage

jpackage --version
echo "开始构建exe"

# TODO fix Error: Invalid Option: [target]
jpackage --name "SoNovel" --type "app-image" --input "nobug/jpackage" --dest dist --icon assets/logo.ico --app-version 1.5.3 --copyright "Copyright (C) 2024 pcdd. All rights reserved." --description "开源搜书神器" --vendor "github.com/freeok" --main-jar "app-jar-with-dependencies.jar" --win-console

# cp: cannot create regular file 'dist/SoNovel': No such file or directory
cp config.ini dist/SoNovel
# rm: cannot remove ‘dist/Sollovel/*.ico′: Noj such file or directory
rm dist/SoNovel/*.ico

echo "开始打包压缩"
tar czvf dist/sonovel-win.tar.gz dist/SoNovel
