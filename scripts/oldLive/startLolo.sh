#!/bin/bash

nohup $HOME/lolopay/lolopay-1.0-SNAPSHOT/bin/lolopay -Dconfig.resource=application-live.conf > $HOME/logs/applog.log 2>&1 &
