del maven.zip
del /s /q maven
scripts\DownloadFile http://www.us.apache.org/dist/maven/maven-3/3.2.5/binaries/apache-maven-3.2.5-bin.zip maven.zip
scripts\UnzipFile maven.zip maven
SET JAVA_HOME=C:\Program Files\Java\jdk1.8.0
SET PATH=%CD%\maven\apache-maven-3.2.5\bin;%JAVA_HOME%\bin;%PATH%
SET MAVEN_OPTS=-XX:MaxPermSize=2g -Xmx4g
SET JAVA_OPTS=-XX:MaxPermSize=2g -Xmx4g
scripts\xml pom.xml SET /empty:project/empty:version %APPVEYOR_BUILD_VERSION%
mvn clean package --batch-mode -DskipTest