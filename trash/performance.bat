@echo off
set SCRIPT_DIR=%~p0
java -cp "%CLASSPATH%;%SCRIPT_DIR%..\lib\split.jar" ui.console.Performance %1 %2 %3 %4 %5 %6 %7 %8 %9