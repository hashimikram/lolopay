#!/bin/bash

#update errors table
/srv/www/lolo/lolopay-1.0-SNAPSHOT/bin/init-errors-data -Dconfig.resource=application-test.conf

# start in background: 
# nohup $HOME/lolo/lolopay-1.0-SNAPSHOT/bin/lolopay -Dhttp.port=9001 -Dconfig.resource=application-test.conf > /dev/null 2>&1&
nohup /srv/www/lolo/lolopay-1.0-SNAPSHOT/bin/lolopay -Dhttp.port=9001 -Dconfig.resource=application-test.conf > /srv/www/lolo/logs/applog.log 2>&1&
