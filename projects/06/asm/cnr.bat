@echo off
::
:: compile and run given java class
::
set root=C:\Users\Wil.ly Hellcat\code\asm
set imports=%root%\..\algs4\jars\algs4.jar;.
set bin=%root%\bin
javac -cp "%root%;%imports%" -d "%bin%" -g -encoding UTF-8 -Xlint:all -Xlint:overrides -Xmaxwarns 10 -Xmaxerrs 10 %1.java
if %errorlevel% neq 0 exit /b %errorlevel%
java -cp "%imports%;%bin%" %*