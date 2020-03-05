@echo off
set oldDate=%1
set newDate=%2
set url=%3
set file=%4
cd git

cd %url%

git fetch

git log --since=%oldDate% --until=%newDate% --numstat --pretty=format:"%%H | %%an | %%ai" --no-renames > %file%