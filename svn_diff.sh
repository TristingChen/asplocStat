#!/bin/bash
version=$1
url=$2
file=$3
svn diff -c ${version} ${url} > ${file}