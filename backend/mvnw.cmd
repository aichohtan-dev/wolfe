@echo off
setlocal
set "MVN_VERSION=3.9.11"
set "BASE_DIR=%~dp0"
set "CACHE_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MVN_VERSION%"
set "MVN_HOME=%CACHE_DIR%\apache-maven-%MVN_VERSION%"
set "ARCHIVE=%CACHE_DIR%\apache-maven-%MVN_VERSION%-bin.zip"
if not exist "%MVN_HOME%\bin\mvn.cmd" (
  if not exist "%CACHE_DIR%" mkdir "%CACHE_DIR%"
  if not exist "%ARCHIVE%" (
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MVN_VERSION%/apache-maven-%MVN_VERSION%-bin.zip' -OutFile '%ARCHIVE%.tmp'"
    move /Y "%ARCHIVE%.tmp" "%ARCHIVE%" >nul
  )
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force '%ARCHIVE%' '%CACHE_DIR%'"
)
call "%MVN_HOME%\bin\mvn.cmd" -f "%BASE_DIR%pom.xml" %*
endlocal
