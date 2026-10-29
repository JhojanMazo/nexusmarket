@echo off
setlocal
set "ROOT_DIR=%~dp0"
call "%ROOT_DIR%nexusmarket\mvnw.cmd" %*
exit /b %ERRORLEVEL%
