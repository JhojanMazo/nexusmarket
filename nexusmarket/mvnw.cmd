@REM ----------------------------------------------------------------------------
@REM Maven Wrapper Executable for Windows
@REM ----------------------------------------------------------------------------

@echo off
setlocal

WHERE mvn >nul 2>nul
IF %ERRORLEVEL% EQU 0 (
  mvn %*
  exit /b %ERRORLEVEL%
)

set WRAPPER_JAR="%~dp0.mvn\wrapper\maven-wrapper.jar"
if exist %WRAPPER_JAR% (
  java -jar %WRAPPER_JAR% %*
  exit /b %ERRORLEVEL%
)

echo Error: Maven not found on PATH and wrapper jar missing.
exit /b 1
