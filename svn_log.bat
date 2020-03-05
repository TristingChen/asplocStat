@echo off
set oldDate=%1
set newDate=%2
set url=%3
set file=%4
svn log -r {%oldDate%}:{%newDate%} -v --xml %url% > %file%
