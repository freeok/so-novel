mvn clean package "-DskipTests"

cd target
mkdir tmp
mv app-jar-with-dependencies.jar tmp
mv classes tmp
pwd
ls
cd ..
pwd
ls

jpackage \
--name "So Novel" \
--input "target/tmp" \
--dest dist \
--icon assets/logo.ico \
--app-version 1.5.1 \
--copyright "Copyright (C) 2024 pcdd. All rights reserved." \
--description "开源搜书神器" \
--vendor "github.com/freeok" \
--about-url "github.com/freeok/so-novel" \
--main-jar app-jar-with-dependencies.jar

tar -zcvf app.tar.gz dist
