#!/bin/bash

# start in console: 
# -J-Xms128M -J-Xmx512m -J-server
# -Dhttp.port=1234 -Dhttp.address=127.0.0.1
/home/voxfin/lolo/lolopay-1.0-SNAPSHOT/bin/update-database-collections -Dhttp.port=9001 -Dconfig.resource=application-test.conf
