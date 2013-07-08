#!/usr/bin/env bash

make -C$IG_HOME full-clean
cd $IG_HOME && ./setup.sh -f $1
if [ ! -d $2 ]; then
    mkdir $2
fi
cp -r $IG_HOME/../eclipse $2
make -C$2/eclipse
