lastday=$(cat lastday.txt)
#echo $lastday
today=$(date -d "now" +%Y-%m-%d)
#echo $today
tomorrow=$(date -d 'tomorrow' +%Y-%m-%d)
#echo $tomorrow

datadir="/opt/aspLoc/data/421/"

echo -e "\n" > $datadir$today.txt
echo -e "\n" > $datadir$today-amd.txt

cd /opt/aspLoc/git/10.12.3.97_80_shop/workbench/

git checkout develop
git fetch
git branch -r|grep -v master |grep -v release_V|grep -v develop |while read line
do
    brname=${line#*origin/}
    echo "#run $brname"
    git branch -D $brname
    #echo "#2 checkout $brname"
    git checkout -b $brname $line
    #echo "#3 pull $brname"
    git pull
    #echo "#4 stats $brname"
    echo -e "\n#branch# $brname\n"  >> $datadir$today.txt
    echo -e "\n#branch# $brname\n"  >> $datadir$today-amd.txt
    echo "git log --since=$lastday --until=$tomorrow --numstat --pretty=format:\"%H | %an | %ai\" --no-renames >> $datadir$today.txt" 
    git log --since=$lastday --until=$tomorrow --numstat       --pretty=format:"%H | %an | %ai" --no-renames >> $datadir$today.txt
    git log --since=$lastday --until=$tomorrow --name-status --pretty=format:"%H | %an | %ai" --no-renames >> $datadir$today-amd.txt
done

cd $datadir
lastday=$(date -d "2 days ago" +%Y-%m-%d)
echo $lastday > lastday.txt
