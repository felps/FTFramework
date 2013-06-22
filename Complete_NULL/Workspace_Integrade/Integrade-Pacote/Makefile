DOC_DIR=doc
LRM_PATH=resourceProviders/lrm
ADR_PATH=resourceProviders/adr
LUPA_PATH=resourceProviders/lupa
GRM_PATH=clusterManagement/grm
CDRM_PATH=clusterManagement/cdrm
ARSM_PATH=clusterManagement/arsm
ARSC_PATH=shared/arsc
APPREPOS_PATH=clusterManagement/applicationRepository
CLUSTERVIEW_PATH=tools/clusterView
ASCTGUI_PATH=tools/asct
DATACONVERTERS_PATH=libs/dataConverters
BSPLIB_PATH=libs/bspLib
BROKER_PATH=libs/broker
## { IMPI
MPILIB_PATH=libs/mpiLib
## } IMPI
CHECKPOINTING_PATH=libs/checkpointing
PORTAL_PATH=tools/portal

# FIXME Clean targets
all: broker cdrm grm applicationRepository asctGui lupa lrm adr dataConverters bspLib checkpointing mpiLib

system: broker cdrm grm applicationRepository asctGui lupa lrm adr dataConverters bspLib checkpointing mpiLib

all-security: generate arsc arsm system

clients: generate lupa lrm adr arsc clusterView asctGui dataConverters bspLib broker checkpointing portal

servers: generate cdrm arsc arsm grm applicationRepository

portal: broker cdrm arsc arsm grm asctGui
	cd ${PORTAL_PATH}; ant; cd ../..;

lupa:
	cd ${LUPA_PATH}; make DEBUG='false'; cd ../..;

lrm:
	cd ${LRM_PATH}; make DEBUG='false'; cd ../..;

adr:
	cd ${ADR_PATH}; make DEBUG='false'; cd ../..;

broker:
	cd ${BROKER_PATH}; make DEBUG='false'; cd ../..;

asctGui: 
	cd ${ASCTGUI_PATH}; ant; cd ../..; 


grm:    
	cd ${GRM_PATH}; ant; cd ../..; 

cdrm:    
	cd ${CDRM_PATH}; ant; cd ../..; 

arsm:    
	cd ${ARSM_PATH}; ant; cd ../..; 

arsc:   
	cd ${ARSC_PATH}; make DEBUG=false ; cd ../..; 

applicationRepository: 
	cd ${APPREPOS_PATH}; ant; cd ../..; 

clusterView:       
	cd ${CLUSTERVIEW_PATH}; ant; cd ../..; 

dataConverters:
	cd ${DATACONVERTERS_PATH}; make DEBUG='false'; cd ../..; 

bspLib:
	cd ${BSPLIB_PATH}; make DEBUG='false'; cd ../..; 

checkpointing:
	cd ${CHECKPOINTING_PATH}; make DEBUG='false'; cd ../..; 

## { IMPI
mpiLib:
	cd ${MPILIB_PATH}; make DEBUG='false'; cd ../..; 
## } IMPI

doc:
	doxygen Doxyfile;

clean:

	cd ${LUPA_PATH}; make clean; cd ../..; 
	cd ${LRM_PATH}; make clean; cd ../..; 
	cd ${ASCTGUI_PATH}; ant clean; cd ../..;
	cd ${PORTAL_PATH}; ant clean; cd ../..;
	cd ${GRM_PATH}; ant clean; cd ../..; 
	cd ${ARSM_PATH}; ant clean; cd ../..; 
	cd ${ARSC_PATH}; make clean; cd ../..; 
	cd ${APPREPOS_PATH}; ant clean; cd ../..; 
	cd ${CLUSTERVIEW_PATH}; ant clean; cd ../..; 
	cd ${BSPLIB_PATH}; make clean; cd ../..;
## { IMPI
	cd ${MPILIB_PATH}; make clean; cd ../..;
## } IMPI
	cd ${CHECKPOINTING_PATH}; make clean; cd ../..;
	cd ${DATACONVERTERS_PATH}; make clean; cd ../..;

full-clean: clean generate-clean
	rm -rf applicationRepository.log
	rm -rf cdrm.log
	rm -rf grm.log
	rm -rf lrm.log
	rm -rf clusterManagement/applicationRepository/appRepos.keytab
	rm -rf clusterManagement/cdrm/classes/
	rm -rf clusterManagement/cdrm/logs/
	rm -rf clusterManagement/cdrm/src/br/usp/ime/oppstore/corba/
	rm -rf libs/broker/TestAdrOperations
	rm -rf libs/broker/TestBrokerReal
	rm -rf libs/broker/src/dependencies/
	rm -rf libs/broker/src/objects/
	rm -rf libs/broker/src/ida/dependencies/
	rm -rf libs/broker/src/ida/objects/
	rm -rf libs/broker/src/jni/classes/
	rm -rf libs/broker/src/jni/dependencies/
	rm -rf libs/broker/src/jni/objects/
	rm -rf libs/checkpointing/testCkpTmp
	rm -rf resourceProviders/adr/AdrLauncher
	rm -rf resourceProviders/adr/src/dependencies/
	rm -rf resourceProviders/adr/src/objects/
	rm -rf resourceProviders/lrm/broker.log
	rm -rf resourceProviders/lrm/lrmData/
	rm -rf resourceProviders/lrm/lupaRequests.virtual.log
	rm -rf resourceProviders/lrm/machineIp.dat
	rm -rf resourceProviders/lrm/lupa.virtual.log 

generate:
	cp clusterManagement/applicationRepository/jacorb.properties.template clusterManagement/applicationRepository/jacorb.properties
	cp clusterManagement/cdrm/jacorb.properties.template clusterManagement/cdrm/jacorb.properties
	cp clusterManagement/grm/database/ExecutionManager.data.db.template clusterManagement/grm/database/ExecutionManager.data.db
	cp clusterManagement/grm/database/ExecutionManager.index.db.template clusterManagement/grm/database/ExecutionManager.index.db
	cp clusterManagement/grm/database/ExecutionManager.lock.db.template clusterManagement/grm/database/ExecutionManager.lock.db
	cp clusterManagement/grm/database/ExecutionManager.trace.db.template clusterManagement/grm/database/ExecutionManager.trace.db
	cp clusterManagement/grm/jacorb.properties.template clusterManagement/grm/jacorb.properties
	cp clusterManagement/grm/launchTrader/settr.template clusterManagement/grm/launchTrader/settr
	cp libs/bspLib/Makefile.vars.template libs/bspLib/Makefile.vars
	cp libs/bspLib/bspcc.sh.template libs/bspLib/bspcc.sh
	cp libs/mpiLib/Makefile.vars.template libs/mpiLib/Makefile.vars
	cp resourceProviders/lrm/Makefile.vars.template resourceProviders/lrm/Makefile.vars
	cp resourceProviders/lrm/arsc.conf.template resourceProviders/lrm/arsc.conf
	cp resourceProviders/lrm/asct.conf.template resourceProviders/lrm/asct.conf
	cp resourceProviders/lrm/lrm.conf.template resourceProviders/lrm/lrm.conf
	cp tools/asct/jacorb.properties.template tools/asct/jacorb.properties
	cp tools/testInteGrade/jacorb.properties.template tools/testInteGrade/jacorb.properties
	cp startservices.sh.template startservices.sh

generate-clean:
	rm -Rf  clusterManagement/applicationRepository/jacorb.properties
	rm -Rf  clusterManagement/cdrm/jacorb.properties
	rm -Rf  clusterManagement/grm/database/ExecutionManager.data.db
	rm -Rf  clusterManagement/grm/database/ExecutionManager.index.db
	rm -Rf  clusterManagement/grm/database/ExecutionManager.lock.db
	rm -Rf  clusterManagement/grm/database/ExecutionManager.trace.db
	rm -Rf  clusterManagement/grm/jacorb.properties
	rm -Rf  clusterManagement/grm/launchTrader/settr
	rm -Rf  libs/bspLib/Makefile.vars
	rm -Rf  libs/bspLib/bspcc.sh
	rm -Rf  libs/mpiLib/Makefile.vars
	rm -Rf  resourceProviders/lrm/Makefile.vars
	rm -Rf  resourceProviders/lrm/arsc.conf
	rm -Rf  resourceProviders/lrm/asct.conf
	rm -Rf  resourceProviders/lrm/lrm.conf
	rm -Rf  tools/asct/jacorb.properties
	rm -Rf  tools/testInteGrade/jacorb.properties
	rm -Rf  startservices.sh
