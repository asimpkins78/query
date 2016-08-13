del ant.zip
del /s /q ant
scripts\DownloadFile http://www-eu.apache.org/dist//ant/binaries/apache-ant-1.9.7-bin.zip ant.zip
scripts\UnzipFile ant.zip ant
SET JAVA_HOME=C:\Program Files\Java\jdk1.8.0
SET PATH=%CD%\maven\apache-maven-3.2.5\bin;%JAVA_HOME%\bin;%PATH%
REM SET MAVEN_OPTS=-XX:MaxPermSize=2g -Xmx4g
REM SET JAVA_OPTS=-XX:MaxPermSize=2g -Xmx4g
REM scripts\xml pom.xml SET /empty:project/empty:version %APPVEYOR_BUILD_VERSION%
ant\apache-ant-1.9.7\bin\ant
REM move target\simpkins-query-%APPVEYOR_BUILD_VERSION%.jar simpkins-query.jar
jar cvf simpkins-query.jar out\production\simpkins-query
