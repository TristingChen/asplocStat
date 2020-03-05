@echo off
set path=%1
set url=%2
cd %path%
"C:\Program Files\Git\cmd\git.exe" clone %url%