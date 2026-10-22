@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script for Windows
@REM ----------------------------------------------------------------------------
@SETLOCAL
@IF "%JAVA_HOME%"=="" (
    SET "JAVA_EXE=java"
) ELSE (
    SET "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
)

@SET "MAVEN_PROJECTBASEDIR=%~dp0"
@IF "%MAVEN_PROJECTBASEDIR:~-1%"=="\" SET "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

@SET "WRAPPER_JAR=%~dp0.mvn\wrapper\maven-wrapper.jar"

"%JAVA_EXE%" -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
@ENDLOCAL
