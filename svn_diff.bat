@echo off
set version=%1
set url=%2
set file=%3
svn diff -c %version% %url% > %file%