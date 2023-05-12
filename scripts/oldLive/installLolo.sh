#!/bin/bash

echo "Delete instalation folder"
rm -rf $HOME/lolopay/*

echo "Unzip new release"
unzip $HOME/lolopay-1.0-SNAPSHOT.zip -d $HOME/lolopay

echo "Delete zip"
rm $HOME/lolopay-1.0-SNAPSHOT.zip

echo "Set up new release"
chmod -R +x $HOME/lolopay/lolopay-1.0-SNAPSHOT/bin

