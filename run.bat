@echo off
REM Cine Java - one-command run (Windows)
cd /d %~dp0
echo Compiling...
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
del sources.txt
echo Running... (demo login: demo / demo123)
java -cp out cine.Main
pause
