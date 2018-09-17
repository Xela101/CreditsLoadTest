"C:/Program Files/Java/jdk-10.0.1/bin/javac" -d bin/ -cp "jar/wallet-desktop.jar" src/com/credits/test/*.java
"C:/Program Files/Java/jdk-10.0.1/bin/jar" cvfm CreditsLoadTest.jar manifest.txt -C ./bin/ . jar
pause
