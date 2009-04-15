@echo off

set SCRIPT_DIR=%~d0%~p0
set CLASS=net.sourceforge.segment.ui.console.Performance
set JAVA_OPTS=-Xmx256M

"%SCRIPT_DIR%"/exec.bat %CLASS% %*
