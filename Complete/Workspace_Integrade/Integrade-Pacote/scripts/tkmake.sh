#!/bin/bash

#O deployment é responsável por deixar as depêndencias no local correto
path=$1
grm=$2
java=$3

rm -f deploy_setup.conf

echo "GRMMachineName=$grm" >> deploy_setup.conf
echo "useGRMParent=false" >> deploy_setup.conf
echo "parentGRMHostName=" >> deploy_setup.conf
echo "inteGradeHome=$path" >> deploy_setup.conf
echo "antHome=$path/deps/ant/ant-built" >> deploy_setup.conf
echo "jacorbHome=$path/deps/jacorb/jacorb-built" >> deploy_setup.conf
echo "javaHome=$java" >> deploy_setup.conf
echo "luaPath=$path/deps/lua/lua-built" >> deploy_setup.conf
echo "catalinaHome="$path/deps/tomcat/tomcat-built >> deploy_setup.conf
echo "traderPath=$path/trader/" >> deploy_setup.conf
echo "ServersRunInThisMachine=true" >> deploy_setup.conf
echo "secureAppRepos=false" >> deploy_setup.conf
echo "kerberosDomain=" >> deploy_setup.conf
echo "kerberosConfigPath=" >> deploy_setup.conf
echo "generateKeytabs=" >> deploy_setup.conf

#configura
$path/setup.sh -f deploy_setup.conf

source ./startservices.sh env

#Workaround para Path Jacorb
export CLASSPATH=$path/deps/jacorb/jacorb-built/lib/idl.jar:$path/deps/jacorb/jacorb-built/lib/jacorb.jar:$path/deps/jacorb/jacorb-built/lib/antlr-2.7.2.jar:$path/deps/jacorb/jacorb-built/lib/avalon-framework-4.1.5.jar:$path/deps/jacorb/jacorb-built/lib/backport-util-concurrent.jar:$path/deps/jacorb/jacorb-built/lib/logkit-1.2.jar:$path/deps/jacorb/jacorb-built/lib/picocontainer-1.2.jar:$path/deps/jacorb/jacorb-built/lib/wrapper-3.1.0.jar 

echo ">>>>>>>>>>"
echo $CLASSPATH
echo ">>>>>>>>>>"

cd $path && make all && cd -
