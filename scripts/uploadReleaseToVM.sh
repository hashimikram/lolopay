#!/bin/bash

remoteUserName="cornel"
remoteHost="10.0.2.15"
remoteIdFile="/home/cornel/.ssh/ubuntuVM"
remoteFolder="/srv/www/lolo"
deployFolder="lolopay-1.0-SNAPSHOT"
deployZip="$deployFolder.zip"

appLocation="$HOME/git/lolopay"
deployZipLocation="$appLocation/target/universal"

if ssh -i $remoteIdFile $remoteUserName@$remoteHost "[ ! -d $remoteFolder ]"; then
	printf "Directory $remoteFolder DOES NOT exists on remote destination.\n"
	exit 1;
fi

remoteFolderOwner=$(ssh -i $remoteIdFile $remoteUserName@$remoteHost "ls -l /srv/www/ | sed '/total/d' | awk '/lolo/{print \$3}'") 

if [ $remoteFolderOwner != "cornel" ]; then
	printf "Invalid owner of $remoteFolder on remote destination\n";
	exit 1;
fi

printf "Build application ... \n"
cd $appLocation && sbt clean dist

printf "Copy new release archive to server ...\n"
scp -i $remoteIdFile $deployZipLocation/$deployZip $remoteUserName@$remoteHost:$remoteFolder

printf "Untar archive in server and delete archive\n"
ssh -i $remoteIdFile $remoteUserName@$remoteHost "cd $remoteFolder && unzip -o $deployZip && rm -f $deployZip"

printf "Run command on server: sudo sh $remoteFolder/lolopay-1.0-SNAPSHOT/scripts/localVM/start.sh\n"
ssh -i $remoteIdFile $remoteUserName@$remoteHost -tt 'echo cornel | sudo -S sh '$remoteFolder'/lolopay-1.0-SNAPSHOT/scripts/localVM/start.sh'

