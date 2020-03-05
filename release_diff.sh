#!/bin/bash
workdir=$1
url=$2
rold=$3
rnew=$4
file=$5

echo "cd ${workdir}"
cd ${workdir}

echo "svn co -q ${url}${rold}"
svn co -q ${url}${rold}

echo "svn co -q ${url}${rnew}"
svn co -q ${url}${rnew}

echo "diff -r -x ".svn" ${rold} ${rnew} -u > ../${file}"
diff -r -x ".svn" ${rold} ${rnew} -u > ../${file}

cd ..
cd ..

rm -fr ${workdir}
