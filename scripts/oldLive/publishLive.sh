#!/bin/bash

cd $HOME/git/lolopay 

sbt clean dist

cd $HOME/git/lolopay/scripts/live

remoteHost="34.232.244.170"
remoteUsername="cornel"
remoteIdentityFile="/media/veracrypt1/anm-cornel-private.pem"
deployZip="lolopay-1.0-SNAPSHOT.zip"

sshRemoteHost="ssh -i $remoteIdentityFile $remoteUsername@$remoteHost"  

jppHost="10.0.14.11"
jppUserName="cornel"

app1Host="10.0.11.11"
app2Host="10.0.12.11"
wuaHost="10.0.21.21"
woaHost="10.0.22.22"
services1Host="10.0.30.11"
services2Host="10.0.31.11"

echo "Copy dist zip to anm"
scp -i $remoteIdentityFile $HOME/git/lolopay/target/universal/$deployZip $remoteUsername@$remoteHost:$HOME/$deployZip

echo "Copy dist zip from anm to jpp"
$sshRemoteHost "scp -r $HOME/$deployZip $jppUserName@$jppHost:$HOME/"

echo "Delete dist zip from anm"
$sshRemoteHost "rm -f $HOME/$deployZip"

echo "Stop lolo in jpp"
$sshRemoteHost "ssh $jppUserName@$jppHost 'kill -15 \`cat $HOME/lolopay/lolopay-1.0-SNAPSHOT/RUNNING_PID\`'"

echo "Delete instalation folder contents"
$sshRemoteHost "ssh $jppUserName@$jppHost 'rm -rf $HOME/lolopay/*'"

echo "Unzip new release"
$sshRemoteHost "ssh $jppUserName@$jppHost 'unzip $HOME/$deployZip -d $HOME/lolopay'"

echo "Delete zip"
$sshRemoteHost "ssh $jppUserName@$jppHost 'rm $HOME/$deployZip'"

echo "Set up new release"
$sshRemoteHost "ssh $jppUserName@$jppHost 'chmod -R +x $HOME/lolopay/lolopay-1.0-SNAPSHOT/bin'"

echo "Empty LoloPaySdkStorage.tmp on WUA"
$sshRemoteHost "ssh $jppUserName@$wuaHost 'find /var/www -type f -name LoloPaySdkStorage.tmp -exec cp /dev/null {} \;'"

echo "Empty LoloPaySdkStorage.tmp on WOA"
$sshRemoteHost "ssh $jppUserName@$woaHost 'find /var/www -type f -name LoloPaySdkStorage.tmp -exec cp /dev/null {} \;'"

echo "Empty LoloPaySdkStorage.tmp on APP1"
$sshRemoteHost "ssh $jppUserName@$app1Host 'find /var/www -type f -name LoloPaySdkStorage.tmp -exec cp /dev/null {} \;'"

echo "Empty LoloPaySdkStorage.tmp on APP2"
$sshRemoteHost "ssh $jppUserName@$app2Host 'find /var/www -type f -name LoloPaySdkStorage.tmp -exec cp /dev/null {} \;'"

# add diferent scripts to run before go live
echo "Init main Data = update main account record"
$sshRemoteHost "ssh $jppUserName@$jppHost '$HOME/lolopay/lolopay-1.0-SNAPSHOT/bin/init-main-data -Dconfig.resource=application-live.conf'"

echo "Update database collections = add new collections"
$sshRemoteHost "ssh $jppUserName@$jppHost '$HOME/lolopay/lolopay-1.0-SNAPSHOT/bin/update-database-collections -Dconfig.resource=application-live.conf'"

echo "Update errors table"
$sshRemoteHost "ssh $jppUserName@$jppHost '$HOME/lolopay/lolopay-1.0-SNAPSHOT/bin/init-errors-data -Dconfig.resource=application-live.conf'"

# echo "Start LoloPay"
# $sshRemoteHost "ssh $jppUserName@$jppHost 'nohup $HOME/lolopay/lolopay-1.0-SNAPSHOT/bin/lolopay -Dconfig.resource=application-live.conf > $HOME/logs/applog.log 2>&1 &'"




