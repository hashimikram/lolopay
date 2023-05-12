#!/bin/bash

# Get pid
pid=`ps aux | grep lolopay | awk '{print $2}'`

# Kill PID
kill -9 $pid

rm -f /srv/www/lolo/lolopay-1.0-SNAPSHOT/RUNNING_PID

# Display remaining processes for confirmation
netstat -ntlp | grep LISTEN
