#!/bin/bash

remoteUserName="cornel"
remoteHost="34.232.244.170"
remoteIdFile="/media/veracrypt1/anm-cornel-private.pem"
remoteFolder="/srv/www/lolo"
deployFolder="lolopay-1.0-SNAPSHOT"
deployZip="$deployFolder.zip"

jppHostName="10.0.14.12"
jppUserName="cornel"
app1Host="10.0.11.12"
app2Host="10.0.12.12"
wuaHost="10.0.21.22"
woaHost="10.0.22.24"

if ssh -i $remoteIdFile $remoteUserName@$remoteHost "ssh $jppUserName@$jppHostName \"[ ! -d $remoteFolder ]\""; then
	printf "Directory $remoteFolder DOES NOT exists on remote destination.\n"
	exit 1;
fi

remoteFolderOwner=$(ssh -i $remoteIdFile $remoteUserName@$remoteHost "ssh $jppUserName@$jppHostName \"ls -l /srv/www/ | sed '/total/d'\"" | awk '/lolo/{print $3}')
if [ $remoteFolderOwner != "cornel" ]; then
	printf "Invalid owner of $remoteFolder on remote destination\n";
	exit 1;
fi

appLocation="$HOME/git/lolopay"
deployZipLocation="$appLocation/target/universal"

printf "Copy live environment variables file ... \n"
cp /media/veracrypt1/lolo/live_vars.env $appLocation/conf

printf "Copy live server password ... \n"
password=$(cat /media/veracrypt1/lolo/password)

printf "Build application ... \n"
cd $appLocation && sbt clean dist

printf "Delete live environment variables file ... \n"
rm -f $appLocation/conf/live_vars.env

printf "Copy new release archive to ANM ... \n"
scp -i $remoteIdFile $deployZipLocation/$deployZip $remoteUserName@$remoteHost:~

printf "Copy new release archive from ANM server to JPP server ...\n"
ssh -i $remoteIdFile $remoteUserName@$remoteHost "scp ~/$deployZip $jppUserName@$jppHostName:$remoteFolder"

printf "Delete archive from ANM server ... \n"
ssh -i $remoteIdFile $remoteUserName@$remoteHost "rm -f ~/$deployZip"

printf "Untar archive in JPP server and delete archive ... \n"
ssh -i $remoteIdFile $remoteUserName@$remoteHost "ssh $jppUserName@$jppHostName \"cd $remoteFolder && unzip -o $deployZip && rm -f $deployZip\""

printf "Reload deamon on remote server ... \n"
ssh -i $remoteIdFile $remoteUserName@$remoteHost "ssh $jppUserName@$jppHostName -tt 'echo $password | sudo -S sh $remoteFolder/$deployFolder/scripts/live/start.sh'"

printf "Empty LoloPaySdkStorage.tmp on APP1 ... \n"
ssh -i $remoteIdFile $remoteUserName@$remoteHost "ssh $jppUserName@$app1Host 'find /var/www -type f -name LoloPaySdkStorage.tmp -exec cp /dev/null {} \;'"

printf "Empty LoloPaySdkStorage.tmp on APP2 ... \n"
ssh -i $remoteIdFile $remoteUserName@$remoteHost "ssh $jppUserName@$app2Host 'find /var/www -type f -name LoloPaySdkStorage.tmp -exec cp /dev/null {} \;'"

printf "Empty LoloPaySdkStorage.tmp on WUA ... \n"
ssh -i $remoteIdFile $remoteUserName@$remoteHost "ssh $jppUserName@$wuaHost 'find /var/www -type f -name LoloPaySdkStorage.tmp -exec cp /dev/null {} \;'"

printf "Empty LoloPaySdkStorage.tmp on WOA ... \n"
ssh -i $remoteIdFile $remoteUserName@$remoteHost "ssh $jppUserName@$woaHost 'find /var/www -type f -name LoloPaySdkStorage.tmp -exec cp /dev/null {} \;'"

printf "END\n"

