"C:/Program Files/Java/jdk1.8.0_161/bin/javac" -d bin/ -cp "jar/wallet-desktop.jar" src/com/credits/test/*.java
"C:/Program Files/Java/jdk1.8.0_161/bin/jar" cvfm CreditsLoadTest.jar manifest.txt -C ./bin/ . jar
