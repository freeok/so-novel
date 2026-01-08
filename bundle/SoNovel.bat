@echo off
:: 默认 JVM 参数
set JVM_OPTS=-Xmx1G -XX:+UseZGC -XX:+ZGenerational -Dmode=web -Dfile.encoding=GBK
"C:\Program Files\Java\jdk-21\bin\java.exe" %JVM_OPTS% -cp ".\app-jar-with-dependencies.jar" com.pcdd.sonovel.Main %*
pause
