#!/bin/bash

# check if base folder exists and create it
echo "Search for application folder"
if [ ! -d "/srv/www/lolo" ]; then
	echo "Application folder NOT found ... created!"
	mkdir "/srv/www/lolo"
fi

# check if backup folder exists
echo "Search for application back up folder"
if [ ! -d "/srv/www/lolo/oldVersions" ]; then
	echo "Application back up folder NOT found ... created!"
	mkdir "/srv/www/lolo/oldVersions"
fi

# check if release folder exists
echo "Check app installed"
if [ -d "/srv/www/lolo/lolopay-1.0-SNAPSHOT" ]; then
	
	echo "Backup previous release"

	# create a new name for backup folder 
	newDate=$(date '+%d-%b-%Y-%H-%M-%s')

	# create new backup folder
	mkdir "/srv/www/lolo/oldVersions/$newDate"
	
	# backup current release
	mv "/srv/www/lolo/lolopay-1.0-SNAPSHOT" "/srv/www/lolo/oldVersions/$newDate"

	# delete current release
	rm -rf "/srv/www/lolo/lolopay-1.0-SNAPSHOT"
fi

# check if release folder exists
echo "Check documents folder"
if [ ! -d "/srv/www/lolo/documents" ]; then
	
	echo "Create documents folder"

	# create documents folder
	mkdir "/srv/www/lolo/documents"
	chmod +x "/srv/www/lolo/documents"
fi

# unzip new release
# echo "Unzip new release"
# unzip "/srv/www/lolo/lolopay-1.0-SNAPSHOT.zip" -d "/srv/www/lolo"

# delete remaining archive
echo "Delete zip"
rm "/srv/www/lolo/lolopay-1.0-SNAPSHOT.zip"

# set new release permissions
echo "Set up new release"
chmod +rx "/srv/www/lolo/lolopay-1.0-SNAPSHOT/bin"
