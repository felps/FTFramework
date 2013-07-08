#!/usr/bin/env bash
# Environment Variables
export IG_HOME=/usr/local/integrade/
export ANT_HOME=/usr/local/ant/
export JACORB_HOME=/usr/local/JacORB/
export JFREECHART_HOME=
export PATH=${JACORB_HOME}/bin:${ANT_HOME}/bin:$PATH
export NS_PORT=47039
case "$1" in
    "servers")
       echo "Stoping servers"

       pid=`ps aux | grep "TradingService" | grep -v "grep" | awk '{print $2}'`
       echo "Stoping Trader (${pid})"
       kill -9 ${pid} 2>/dev/null  1>/dev/null

       pid=`ps aux | grep "GrmLauncher" | grep -v "grep" | awk '{print $2}'`
       echo "Stoping Grm (${pid})"
       kill -9 ${pid} 2>/dev/null 1>/dev/null

       pid=`ps aux | grep "ApplicationRepository" | grep -v "grep" | awk '{print $2}'`
       echo "Stoping Application Repository (${pid})"
       kill -9 ${pid}  2>/dev/null 1>/dev/null

       pid=`ps aux | grep "NameServer" | grep -v "grep" | awk '{print $2}'`
       echo "Stoping Name Server (${pid})"
       kill -9 ${pid}  2>/dev/null 1>/dev/null

       pid=`ps ux | grep "Djacorb.home" | grep CORBA | grep -v "grep" | awk '{print $2}'`
       kill -9 ${pid}  2>/dev/null 1>/dev/null

       echo Done!
       shift
    ;;
    "lrm")

       pid=`ps aux | grep "LrmLauncher" | grep -v "grep" | awk '{print $2}'`
       echo "Stoping LRM (${pid})"
       if [ "$pid" != "" ]
       then
       		kill -9 ${pid}
       		echo Done!
       else
		echo failed!
       fi 
    ;;
    "adr")
       pid=`ps aux | grep "AdrLauncher" | grep -v "grep" | awk '{print $2}'`
       echo "Stoping ADR (${pid})"
       if [ "$pid" != "" ]
       then
       		kill -9 ${pid}
       		echo Done!
       else
		echo failed!
       fi 
    ;;
	"portal")
	   echo Stoping catalina
	   cd ${CATALINA_HOME}
	   echo "java ${JAVA_HOME}"
	   ./bin/catalina.sh stop
	   shift
	;;
    "all")
      shift
      echo Killing all
      $0 servers
      $0 lrm
      $0 adr
    ;;
    *)
    echo " servers     -> Initializes server side modules(GRM and Application Repository)"
    echo " lrm         -> Initializes the Local Resorce Manager"
    ;;
esac
