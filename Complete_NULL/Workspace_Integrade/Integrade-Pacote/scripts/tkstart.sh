#!/bin/bash
path=$1
server=$2
me=`hostname`
# inicia os serviodres
cd $path
source ./startservices.sh env
# Stop All
echo "Stopping all Services"
./stopservices.sh servers
./stopservices.sh lrm
# Start All

if [ $me == $server ] 
then
    ./startservices.sh servers &
fi
./startservices.sh lrm &
