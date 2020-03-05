#!/bin/bash
oldDate=$1
newDate=$2
url=$3
file=$4
svn log -r {${oldDate}}:{${newDate}} -v --xml ${url} > ${file}
