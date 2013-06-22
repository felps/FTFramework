#!/bin/bash
# config.sh - configuration script for the InteGrade System. This script
# asks a series of questions and make various configurations that eases
# the deployment of the system.
#
# Author: Andrei Goldchleger
# Modified by: Ricardo Luiz de Andrade Abrantes
#

#FUNCTIONS--FUNCTIONS--FUNCTIONS--FUNCTIONS--FUNCTIONS--FUNCTIONS--FUNCTIONS--

  #######################################################################################
  #Asks for a path and checks if it really exists
  #params:
  # message - message that will be echoed to the user
  #Modifies the second parameter to contain the validated path entered by the user
  validatePath(){

    echo $1
    local path
    read -e path
    while ! test -d ${path}; do
      echo "Invalid Directory. try again: "
      read -e path
    done
    eval "$2=${path}"
  }

  #######################################################################################
#   compressSlashes(){
#
#     local newPath
#     newPath=`echo $1 | sed 's#/\+#/#g'`
#     eval "$2=${newPath}"
#   }

  #######################################################################################
  #Checks if a path ends with a slash
   endsWithSlash?(){

#   echo "endsWithSlash" ${1}
   if echo ${1} | grep '/$'  > /dev/null;then
     return 0;
   else
     return 1;
   fi
  }

  #######################################################################################
  #Adds a slash to the end of a path, if it does not contain one

  #Modifies the second parameter to contain a normalized path
  normalizePath(){

   local path=${1}
   if endsWithSlash? ${path}; then
     eval "${2}=${path}"
   else
     path=${path}/
#     echo "Path: " ${path}
     eval "${2}=${path}"
   fi

  }

  #######################################################################################
  #Tries to create a subdir in an existing dir
  #params:
  #message - message that will be echoed to the user
  #creationPath - the directory that will contain the subdirectory

  #Modifies the third parameter to contain the full path to the newly created parameter
  createDir(){

    local message=${1}
    local creationPath=${2}

    while true; do

      echo $message
      local newDir
      read newDir

      if ! test -d ${creationPath}${newDir}; then
        mkdir ${creationPath}${newDir}
        eval "$3=${creationPath}${newDir}"
        break
      else
        echo "!!!WARNING!!! Directory exists! Proceed? (y/n)"
        read confirm

        case "${confirm}" in

          y|Y)
            eval "$3=${creationPath}${newDir}"
            break #directory exists and we are going to use it
          ;;

          n|N)
          ;;

          *)
            echo "Please answer y or n"
          ;;
        esac

      fi
    done
  }

  #######################################################################################
  #Verifies if a file can be created on a specified path
  #Tries to create a subdir in an existing dir
  #params:
  # message - message that will be echoed to the user
  # creationPath - the directory that will contain the subdirectory

  #Modifies the third parameter to contain the full path to the file to be created

  validateIorFile(){

    local message=${1}
    local creationPath=${2}

    while true; do

      echo ${message}
      local newFile
      read newFile

      if ! test -f ${creationPath}${newFile}; then
        eval "$3=${creationPath}${newFile}"
        break
      else
        echo "!!!WARNING!!! File exists! Proceed? (y/n)"
        read confirm

        case "${confirm}" in

          y|Y)
            eval "$3=${creationPath}${newFile}"
            break #file exists and we are going to use it
          ;;

          n|N)
          ;;

          *)
            echo "Please answer y or n"
          ;;
        esac

      fi
    done
  }

  #######################################################################################
  #alter a jacorb.properties to contais the path to a trader reference
  #params:
  # propertiesDir - the dir containing the file to be altered
  # traderIorPath - full path to a valid treader IOR
  alterProperties(){

    local propertiesDir=${1}
    local propertyArgument=${2}
    local propertyName=${3}
    local jacorbPropsTmp="jacorb.properties.tmp"

    sed s#${propertyName}=.*#${propertyName}=${propertyArgument}# \
    < ${propertiesDir}"jacorb.properties" > ${propertiesDir}${jacorbPropsTmp}
    cp ${propertiesDir}${jacorbPropsTmp} ${propertiesDir}"jacorb.properties"
    rm -f ${propertiesDir}${jacorbPropsTmp}
  }

  #######################################################################################
  #alter a variable values at startservices.sh
  # varName - variable's name
  # varValue - the new variable's value
  alterVariable(){

    local varName=${1}
    local varValue=${2}


    sed -i s#${varName}=.*#${varName}=${varValue}# startservices.sh
  }
  
  #######################################################################################
  #alter a string in order to set paths in scripts
  #params:
  # filepath - the file to be altered
  # srcString - string to be replaced
  # dstString - replacement
  alterString(){

    local filepath=${1}
    local srcString=${2}
    local dstString=${3}

    sed s#"${srcString}"#"${dstString}"# \
    < ${filepath} > ${filepath}".tmp"
    cp ${filepath}".tmp" ${filepath}
    rm -f ${filepath}".tmp"
  }



#SCRIPT--SCRIPT--SCRIPT--SCRIPT--SCRIPT--SCRIPT--SCRIPT--SCRIPT--SCRIPT--SCRIPT--

input=$1
outfile="setup.conf"
kerberosDomain=""
kerberosConfigPath=""

if [ ! -f startservices.sh ] 
then
	make generate
fi

if [ ! $1 ] 
then
    input="empty"
fi

if [ $input = "--help" -o $input = "-h" ]
then
    echo "Usage: setup.sh [options]"
    echo
    echo "Where options stands for:"
    echo "   --file, or -f <config file name>: Loads the configuration from a "
    echo "                                     file (usually called setup.conf)."
    echo "   --help, or -h                   : Tthis screen"
    echo
    exit
fi
if [ $input = "--file" -o $input = "-f" ]
then
    setup_conf=$2
    if [ ! $setup_conf ]
    then
	echo "No input file"
	exit
    fi
   . $setup_conf

else
#Asking for the configuration
#Defining and normalizing all the paths

    echo "This script asks for a series of paths"
    echo "You can press TAB for path completion"
    echo "Run it again with --help for more ooptions"
    echo
    inteGradeHome=`pwd`
    echo "This machine will act as a server (y/n)?"
    read -e ServersRunInThisMachine
    while [ "$ServersRunInThisMachine" != "y" -a "$ServersRunInThisMachine" != "Y" -a "$ServersRunInThisMachine" != "n" -a "$ServersRunInThisMachine" != "N" ] 
    do
      echo "(y/n)"
      read -e ServersRunInThisMachine
    done
    if [ "${ServersRunInThisMachine}" = "y" -o "${ServersRunInThisMachine}" = "Y"  ] 
    then
      GRMMachineName=`hostname`
      ServersRunInThisMachine="true"
    else
      ServersRunInThisMachine="false"
      echo "Enter the name of the GRM/Nameserver machine"
      read -e GRMMachineName
    fi
    validatePath "Enter the path for the JacORB installation" jacorbHome
    validatePath "Enter the path for the ANT installation" antHome
    validatePath "Enter the path for the Java installation" javaHome
    validatePath "Enter the path for the lua installation" luaPath 
    validatePath "Enter the path for the tomcat installation (required only for the portal interface)" catalinaHome
    traderPath=${inteGradeHome}/trader/
    secureAppRepos=" "
    echo "Do you want to enable Security in Application Repository (y/n)?"
    echo "You MUST configure your Kerberos BEFORE doing this"
    echo "For more information read the document INSTALL"
    read -e secureAppRepos
    while [ "$secureAppRepos" != "y" -a "$secureAppRepos" != "Y" -a "$secureAppRepos" != "n" -a "$secureAppRepos" != "N" ] 
    do
      echo "(y/n)"
      read -e secureAppRepos
    done
    if [ "${secureAppRepos}" = "y" -o "${secureAppRepos}" = "Y"  ] 
    then
	secureAppRepos="true"
	echo "Enter your Kerberos domain?"
	read -e kerberosDomain
	echo "Enter the absolute path to your Kerberos configuration file?"
	read -e kerberosConfigPath
	echo "Do you want to generate the keytabs (y/n)?"
	read -e generateKeytabs
	while [ "$generateKeytabs" != "y" -a "$generateKeytabs" != "Y" -a "$generateKeytabs" != "n" -a "$generateKeytabs" != "Y" ] 
	  do
	  echo "(y/n)"
	  read -e generateKeytabs
	done
	if [ "${generateKeytabs}" = "y" -o "${generateKeytabs}" = "Y"  ] 
	then
	    generateKeytabs="true"
	else
	    generateKeytabs="false"
	fi
    else
	secureAppRepos="false"
    fi

    echo "Do you want to set a parent GRM (y/n)?"
    read -e useGRMParent
    while [ "$useGRMParent" != "y" -a "$useGRMParent" != "Y" -a "$useGRMParent" != "n" -a "$useGRMParent" != "N" ] 
    do
      echo "(y/n)"
      read -e useGRMParent
    done
    if [ "${useGRMParent}" = "y" -o "${useGRMParent}" = "Y"  ] 
    then
    	useGRMParent="true"
	echo "Enter the hostname of the parent GRM:"
	read -e parentGRMHostName
    else
        useGRMParent="false"
    fi

fi

#normalizing variables
normalizePath $inteGradeHome inteGradeHome
normalizePath $jacorbHome jacorbHome
normalizePath $antHome antHome
normalizePath $javaHome javaHome
normalizePath $luaPath luaPath
normalizePath $traderPath traderPath
normalizePath $catalinaHome catalinaHome

headersPath=${luaPath}include/
libsPath=${luaPath}lib/
dbPath=${traderPath}traderdb/
traderIorPath=${traderPath}traderior
corbalocLinkLocalNS="corbaloc::${GRMMachineName}:47039/NameService"

if [ "${useGRMParent}" = "true"  ] 
then
corbalocLinkParentNS="corbaloc::${parentGRMHostName}:47039/NameService"
fi

cosNamingIdlPath=${inteGradeHome}/shared/idls/CosNaming.idl
cdrmIdlPath=${inteGradeHome}/shared/idls/Cdrm.idl
ckpReposManagerIdlPath=${inteGradeHome}/shared/idls/CkpReposManager.idl

#configuring startservices
alterVariable "secureAppRepos" $secureAppRepos
alterVariable "IG_HOME" $inteGradeHome
alterVariable "ANT_HOME" $antHome
alterVariable "JACORB_HOME" $jacorbHome
alterVariable "JAVA_HOME" $javaHome
alterVariable "LUA_HOME" $luaPath
alterVariable "CATALINA_HOME" $catalinaHome

#configuring Makefile
alterString "Makefile" "IG_HOME=.*" "IG_HOME=${inteGradeHome}"
alterString "Makefile" "ANT_HOME=.*" "ANT_HOME=${antHome}"
alterString "Makefile" "JACORB_HOME=.*" "JACORB_HOME=${jacorbHome}"
alterString "Makefile" "JAVA_HOME=.*" "JAVA_HOME=${javaHome}"

#Configuring LRM
echo "LUAINCDIR="$headersPath > resourceProviders/lrm/Makefile.vars
echo "LUALIBDIR="$libsPath >>   resourceProviders/lrm/Makefile.vars
# { IMPI
echo "LUAINCDIR="$headersPath >   libs/mpiLib/Makefile.vars
echo "LUALIBDIR="$libsPath >>     libs/mpiLib/Makefile.vars
# } IMPI

alterString "resourceProviders/lrm/asct.conf" "orbPath.*" "orbPath $luaPath?"
alterString "resourceProviders/lrm/asct.conf" "serverNameRef.*" "serverNameRef ${corbalocLinkLocalNS}"
alterString "resourceProviders/lrm/lrm.conf" "orbPath.*" "orbPath $luaPath?"
alterString "resourceProviders/lrm/arsc.conf" "orbPath.*" "orbPath $luaPath?"
alterString "resourceProviders/lrm/lrm.conf" "serverNameRef.*" "serverNameRef ${corbalocLinkLocalNS}"
alterString "resourceProviders/lrm/arsc.conf" "serverNameRef.*" "serverNameRef ${corbalocLinkLocalNS}"

#Configuring ADR
echo "LUAINCDIR="$headersPath  > resourceProviders/adr/Makefile.vars
echo "LUALIBDIR="$libsPath    >> resourceProviders/adr/Makefile.vars
echo port 4001                                 > resourceProviders/adr/adr.conf
#echo ipAddress 127.0.0.1                     >> resourceProviders/adr/adr.conf
echo storagePath /tmp/adr                     >> resourceProviders/adr/adr.conf
echo serverNameRef ${corbalocLinkLocalNS}     >> resourceProviders/adr/adr.conf
echo orbPath ${luaPath}?                      >> resourceProviders/adr/adr.conf
echo cosNamingIdlPath $cosNamingIdlPath       >> resourceProviders/adr/adr.conf
echo cdrmIdlPath $cdrmIdlPath                 >> resourceProviders/adr/adr.conf

#Configuring access broker
echo "LUAINCDIR="$headersPath  > libs/broker/Makefile.vars
echo "LUALIBDIR="$libsPath    >> libs/broker/Makefile.vars
echo port 4001                                        > libs/broker/broker.conf
echo serverNameRef ${corbalocLinkLocalNS}            >> libs/broker/broker.conf
echo orbPath ${luaPath}?                             >> libs/broker/broker.conf
echo cosNamingIdlPath $cosNamingIdlPath              >> libs/broker/broker.conf
echo cdrmIdlPath $cdrmIdlPath                        >> libs/broker/broker.conf
echo ckpReposManagerIdlPath $ckpReposManagerIdlPath  >> libs/broker/broker.conf

cp libs/broker/broker.conf resourceProviders/lrm/broker.conf
cp libs/broker/broker.conf clusterManagement/grm/broker.conf

#Configuring bspLib
echo "LUAINCDIR="$headersPath >   libs/bspLib/Makefile.vars
echo "LUALIBDIR="$libsPath >>     libs/bspLib/Makefile.vars

#Configuring checkpointing
echo "LUAINCDIR="$headersPath       > libs/checkpointing/Makefile.vars
echo "LUALIBDIR="$libsPath         >> libs/checkpointing/Makefile.vars

if ! test -d ${traderPath}; then
   mkdir ${traderPath}
fi

if ! test -d ${dbPath}; then
   mkdir ${dbPath}
fi

#Configuring other modules
alterProperties "clusterManagement/grm/" "file:$traderIorPath" "ORBInitRef\.TradingService"
alterProperties "clusterManagement/grm/" $corbalocLinkLocalNS "ORBInitRef\.NameService"
if [ "${useGRMParent}" = "true" ] 
then
alterProperties "clusterManagement/grm/" $corbalocLinkParentNS "ORBInitRef\.ParentNameService"
fi
alterProperties "clusterManagement/cdrm/" "file:$traderIorPath" "ORBInitRef\.TradingService"
alterProperties "clusterManagement/cdrm/" $corbalocLinkLocalNS "ORBInitRef\.NameService"

alterProperties "clusterManagement/applicationRepository/" "file:$traderIorPath" "ORBInitRef.TradingService"
alterProperties "clusterManagement/applicationRepository/" $corbalocLinkLocalNS "ORBInitRef.NameService"
alterString "clusterManagement/applicationRepository/apprepos.conf" "secureAppRepos.*" "secureAppRepos ${secureAppRepos}"
mkdir "clusterManagement/applicationRepository/repository_root" 2> /dev/null

alterProperties "tools/asct/" $corbalocLinkLocalNS "ORBInitRef\.NameService"
alterString "tools/asct/asct.conf" "isSecurityEnabled.*" "isSecurityEnabled ${secureAppRepos}"
alterProperties "tools/testInteGrade/" $corbalocLinkLocalNS "ORBInitRef\.NameService"

alterString "resourceProviders/lrm/lrm.conf" "secureAppRepos.*" "secureAppRepos ${secureAppRepos}"

#Grants execution permission to database files (needed to avoid an I/O Error)
chmod +x clusterManagement/grm/database/*.db

#Configuring security
if [ "${secureAppRepos}" = "true" ] 
then
    echo "LUAINCDIR="$headersPath > shared/arsc/C++/Makefile.vars
    echo "LUALIBDIR="$libsPath >>  shared/arsc/C++/Makefile.vars
    echo "CPPARSCGFLAGS = -DSECURITY" >>   resourceProviders/lrm/Makefile.vars
    echo "KERBEROSLIBDIR= -L /usr/lib  -lkrb5 -lgssapi_krb5" >>   resourceProviders/lrm/Makefile.vars
    echo "CRYPTO++LIBDIR= -l crypto++" >>   resourceProviders/lrm/Makefile.vars
    echo "ARSC_OBJECTS =  \$(ARSC_PATH)/ArscImpl.o \\" >>   resourceProviders/lrm/Makefile.vars
    echo -e "\t\$(ARSC_PATH)/ArsmStub.o \\" >>   resourceProviders/lrm/Makefile.vars
    echo -e "\t\$(ARSC_PATH)/MessagePacket.o" >>   resourceProviders/lrm/Makefile.vars
    echo "CPPARSCFILE=src/SecureApplicationRepositoryStub.cpp" >>   resourceProviders/lrm/Makefile.vars
    alterString "clusterManagement/applicationRepository/arscLogin.conf" "principal=.*;" "principal=\"apprepos/ARSM@${kerberosDomain}\";"
    alterString "clusterManagement/arsm/arsmLogin.conf" "principal=.*;" "principal=\"srv/ARSM@${kerberosDomain}\";"
    alterString "clusterManagement/arsm/build.xml" "-Djava.security.krb5.conf=\'.*\'" "-Djava.security.krb5.conf=\'${kerberosConfigPath}\'"
    alterString "clusterManagement/applicationRepository/build.xml" "-Djava.security.krb5.conf=\'.*\'" "-Djava.security.krb5.conf=\'${kerberosConfigPath}\'"
    mkdir "clusterManagement/applicationRepository/repository_root/Public" 2> /dev/null

    alterString "tools/asct/build.xml" "-Djava.security.krb5.conf=\'.*\'" "-Djava.security.krb5.conf=\'${kerberosConfigPath}\'"
    alterString "resourceProviders/lrm/arsc.conf" "principal.*" "principal ${GRMMachineName}/ARSC@${kerberosDomain}"
    alterProperties "clusterManagement/arsm/" $corbalocLinkLocalNS "ORBInitRef\.NameService"

    #Creating keytabs
    if [ "${generateKeytabs}" = "true" ] 
    then
	arsm_keytab="clusterManagement/arsm/arsm.keytab"
	appRepos_keytab="clusterManagement/applicationRepository/appRepos.keytab"
	lrm_keytab="resourceProviders/lrm/lrm.keytab"
	pwd
	echo "Kerberos admin username:"
	read -e admin_user
	echo "Kerberos admin password:"
	read -s admin_passwd
	if [ ${ServersRunInThisMachine} = "true" ]
	then
	    keytabs="${arsm_keytab}:srv/ARSM@${kerberosDomain} ${appRepos_keytab}:apprepos/ARSM@${kerberosDomain} ${lrm_keytab}:${GRMMachineName}/ARSC@${kerberosDomain} "
	else
	    keytabs="${lrm_keytab}:${GRMMachineName}/ARSC@${kerberosDomain} "
	fi
	kadmin="/usr/sbin/kadmin"
	for i in $keytabs
	do
	  file=`echo $i | cut -f1 -d:`
	  principal=`echo $i | cut -f2 -d:`
	  if [ -f ${file} ]
	  then
	      rm -f ${file}
	  fi
	  echo "addprinc -randkey $principal" | $kadmin -p ${admin_user}@${kerberosDomain} -w ${admin_passwd} 1,2>/dev/null
	  echo "ktadd -k $file $principal" | $kadmin -p ${admin_user}@${kerberosDomain} -w ${admin_passwd} 1,2>/dev/null
	done
    fi

    # Setting up Makefile
    # { IMPI
    alterString "Makefile" "all:.*" "all: arsm arsc broker cdrm grm applicationRepository asctGui lupa lrm adr dataConverters bspLib checkpointing mpiLib"
    # } IMPI
else
    # Setting up Makefile
    # { IMPI
    	alterString "Makefile" "all:.*" "all: broker cdrm grm applicationRepository asctGui lupa lrm adr dataConverters bspLib checkpointing mpiLib"
    # } IMPI
fi

echo "trader ${traderIorPath} -d ${dbPath}" > clusterManagement/grm/launchTrader/settr

#Fixing bspcc
alterString libs/bspLib/bspcc.sh "bspLib=.*" "bspLib=${inteGradeHome}libs/bspLib/"
alterString libs/bspLib/bspcc.sh "libLuaPath=.*" "libLuaPath=${libsPath}"

#Set up system variables
IG_HOME=${inteGradeHome}
ANT_HOME=${antHome}
JACORB_HOME=${jacorbHome}
JAVA_HOME=${javaHome}
CATALINA_HOME=${catalinaHome}
PATH=${jacorbHome}/bin:${javaHome}/bin:${antHome}/bin:$PATH

export IG_HOME
export ANT_HOME
export JACORB_HOME
export JAVA_HOME
export CATALINA_HOME
export PATH

if [ $input = "empty" ]
then
# Backing up all the data collected
    echo "GRMMachineName=${GRMMachineName}" >${outfile}
    echo "useGRMParent=${useGRMParent}" >> ${outfile}
    echo "parentGRMHostName=${parentGRMHostName}" >> ${outfile}
    echo "inteGradeHome=${inteGradeHome}" >> ${outfile}
    echo "antHome=${antHome}" >> ${outfile} 
    echo "jacorbHome=${jacorbHome}" >> ${outfile} 
    echo "javaHome=${javaHome}" >> ${outfile} 
    echo "luaPath=${luaPath}" >> ${outfile} 
    echo "catalinaHome=${catalinaHome}" >> ${outfile}
    echo "traderPath=${traderPath}" >> ${outfile}
    echo "ServersRunInThisMachine=${ServersRunInThisMachine}" >> ${outfile}
    echo "secureAppRepos=${secureAppRepos}" >> ${outfile}
    echo "kerberosDomain=${kerberosDomain}" >> ${outfile}
    echo "kerberosConfigPath=${kerberosConfigPath}" >> ${outfile}
    echo "generateKeytabs=${generateKeytabs}" >> ${outfile}
fi

echo "Setup finished!"
if [ "${secureAppRepos}" = "true" ] 
then
echo "Type 'make all-security' to compile Integrade."
else
echo "Type 'make all' to compile Integrade."
fi
