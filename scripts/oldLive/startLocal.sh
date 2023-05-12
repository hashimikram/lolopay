sudo service lolopay stop
sudo cp /srv/www/lolopay/lolopay-1.0-SNAPSHOT/scripts/live/lolopay.service /etc/systemd/system/tengo-api.service
sudo chmod 664 /etc/systemd/system/tengo-api.service
sudo systemctl enable tengo-api
sudo systemctl daemon-reload
sudo service tengo-api start



