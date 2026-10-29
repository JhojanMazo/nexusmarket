@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper Startup Script for Windows (only-script distribution)
@REM ----------------------------------------------------------------------------

@echo off
setlocal enabledelayedexpansion

set "MAVEN_PROJECTBASEDIR=%~dp0"
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

set "WRAPPER_PROPS=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"
if not exist "%WRAPPER_PROPS%" (
  echo Error: Cannot find %WRAPPER_PROPS% 1>&2
  exit /b 1
)

set "distributionUrl="
for /f "usebackq tokens=1,* delims==" %%A in ("%WRAPPER_PROPS%") do (
  if "%%A"=="distributionUrl" set "distributionUrl=%%B"
)

if "%distributionUrl%"=="" (
  echo Error: distributionUrl is not defined in %WRAPPER_PROPS% 1>&2
  exit /b 1
)

for %%F in ("%distributionUrl%") do set "distributionUrlName=%%~nxF"
set "distributionUrlNameMain=%distributionUrlName:-bin.zip=%"

if "%MAVEN_USER_HOME%"=="" set "MAVEN_USER_HOME=%USERPROFILE%\.m2"
set "MAVEN_HOME_PARENT=%MAVEN_USER_HOME%\wrapper\dists\%distributionUrlNameMain%\installed"
set "MAVEN_BIN=%MAVEN_HOME_PARENT%\%distributionUrlNameMain%\bin\mvn.cmd"

if exist "%MAVEN_BIN%" (
  call "%MAVEN_BIN%" %*
  exit /b !ERRORLEVEL!
)

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$ErrorActionPreference = 'Stop'; " ^
  "New-Item -ItemType Directory -Force -Path '%MAVEN_HOME_PARENT%' | Out-Null; " ^
  "$zip = Join-Path '%MAVEN_HOME_PARENT%' '%distributionUrlName%'; " ^
  "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; " ^
  "Invoke-WebRequest -Uri '%distributionUrl%' -OutFile $zip -TimeoutSec 15; " ^
  "Expand-Archive -Path $zip -DestinationPath '%MAVEN_HOME_PARENT%' -Force; " ^
  "Remove-Item $zip -Force" >nul 2>nul

if exist "%MAVEN_BIN%" (
  call "%MAVEN_BIN%" %*
  exit /b !ERRORLEVEL!
)

where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  call mvn %*
  exit /b !ERRORLEVEL!
)

echo Error: Could not download Maven from %distributionUrl% and no local 'mvn' installation was found on PATH. 1>&2
exit /b 1
