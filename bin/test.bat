@echo off

set SCRIPT_DIR=%~d0%~p0
set CLASS=net.sourceforge.segment.ui.console.Test

"%SCRIPT_DIR%"/exec.bat %CLASS% %*
