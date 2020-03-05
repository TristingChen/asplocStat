oldDate=$1
newDate=$2
url=$3
file=$4

cd git
cd ${url}

git fetch

git log --since=${oldDate} --until=${newDate} --numstat --pretty=format:"%%H | %%an | %%ai" --no-renames > ${file}