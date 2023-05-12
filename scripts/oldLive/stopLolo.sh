#!/bin/bash

# Get pid
pid=`cat $HOME/lolopay/lolopay-1.0-SNAPSHOT/RUNNING_PID`

# Kill PID
kill -15 $pid

rm -f $HOME/lolopay/lolopay-1.0-SNAPSHOT/RUNNING_PID

# Display remaining processes for confirmation
netstat -ntlp | grep LISTEN | grep 9000
