@echo off
rem Compila o FrameArt com o JDK 25 portatil (..\runtime) e copia o jar para ..\server\plugins
cd /d "%~dp0"
for /d %%D in ("%~dp0..\runtime\jdk-25*") do set "JAVA_HOME=%%~fD"
if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"

call gradlew.bat build || ( echo [ERRO] Build falhou. & pause & exit /b 1 )

if exist "%~dp0..\server\plugins" (
    for %%J in ("plugin\build\libs\FrameArt-*.jar") do copy /y "%%~fJ" "%~dp0..\server\plugins\FrameArt.jar" >nul
    echo Jar copiado para server\plugins\FrameArt.jar
)
pause
