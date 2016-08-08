del maven.zip
del /s /q maven
scripts\DownloadFile http://www-eu.apache.org/dist//ant/binaries/apache-ant-1.9.7-bin.zip ant.zip
scripts\UnzipFile ant.zip ant
SET JAVA_HOME=C:\Program Files\Java\jdk1.8.0
SET PATH=%CD%\maven\apache-maven-3.2.5\bin;%JAVA_HOME%\bin;%PATH%
SET MAVEN_OPTS=-XX:MaxPermSize=2g -Xmx4g
SET JAVA_OPTS=-XX:MaxPermSize=2g -Xmx4g
scripts\xml pom.xml SET /empty:project/empty:version %APPVEYOR_BUILD_VERSION%
mvn clean package --batch-mode -DskipTest
move target\simpkins-query-%APPVEYOR_BUILD_VERSION%.jar simpkins-query.jar