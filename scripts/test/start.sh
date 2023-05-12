#!/bin/bash

printf "Stop lolo in case of previous installations\n"
sudo systemctl stop lolo

printf "Disable lolo in case of previous installations\n"
sudo systemctl disable lolo

printf "Delete /etc/systemd/system/lolo.service in case of previous installations\n"
sudo rm -f /etc/systemd/system/lolo.service

printf "Copy lolopay to /etc/systemd/system\n"
sudo cp -f /srv/www/lolo/lolopay-1.0-SNAPSHOT/scripts/test/lolo.service /etc/systemd/system/

printf "Set owner of file to root\n"
sudo chown root:root /etc/systemd/system/lolo.service

printf "Set permisions of file\n"
sudo chmod 644 /etc/systemd/system/lolo.service

printf "Enable lolo\n"
sudo systemctl enable lolo

printf "Restart lolo\n"
sudo systemctl restart lolo
