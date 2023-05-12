#!/bin/bash

# define connection details
remoteHost="5.35.212.50"
remoteUsername="voxfin"
remotePassword="akdhas7%#$"
remoteFolder="/home/voxfin/lolo"
deployZip="lolopay-1.0-SNAPSHOT.zip"

echo "Copy migrations files to server ..."
sshpass -p $remotePassword scp -rp /home/alin/JavaWorkspace/lolopay/migrationFolder $remoteUsername@$remoteHost:$remoteFolder
