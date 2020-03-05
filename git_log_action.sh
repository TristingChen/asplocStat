oldDate=$1
newDate=$2
url=$3
file=$4

cd /opt/aspLoc/git
cd ${url}

git log --since=${oldDate} --until=${newDate} --name-status --pretty=format:"%%H | %%an | %%ai" --no-renames > /opt/aspLoc/${file}