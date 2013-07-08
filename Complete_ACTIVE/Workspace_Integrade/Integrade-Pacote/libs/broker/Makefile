TARGET1 = TestBrokerReal
TARGET2 = TestAdrOperations

##########################################################
# Source Files                                           #
##########################################################

SHAREDPATH=../../shared
UTILSPATH=$(SHAREDPATH)/utils/c++
JNIDIR = src/jni/
JAVABROKERDIR = br/usp/ime/oppstore/broker/

CPPFILES = src/AccessBrokerSkeleton.cpp      \
           src/AdrDataTransferStub.cpp       \
           src/DataStorageManager.cpp        \
           src/DataRetrievalManager.cpp      \
           src/DataRemovalRenewalManager.cpp \
           src/OppStoreUtils.cpp             \
           src/OppStoreUtilsSSL.cpp          \
           src/Benchmark.cpp                 \
           src/OppStoreBroker.cpp            \
           src/StorageRequestIda.cpp         \
           src/StorageRequestReplicated.cpp  \
           src/FragmentUploadThread.cpp      \
           src/FragmentOperationThread.cpp   \
           src/FragmentDownloadThread.cpp    \
           src/DataReaderThread.cpp          \
           src/DataWriterThread.cpp          \
           src/RetrievalRequestIda.cpp       \
           src/RemovalRenewalRequest.cpp     \
           src/BrokerServerManager.cpp       \
           src/BrokerLogger.cpp              \
           $(UTILSPATH)/CdrmRequestsStub.cpp \
           $(UTILSPATH)/StringTokenizer.cpp  \
           $(UTILSPATH)/Config.cpp           \
           $(UTILSPATH)/LuaUtils.cpp         \
           $(UTILSPATH)/FileUtils.cpp        \
           $(UTILSPATH)/OrbUtils.cpp         \
           $(UTILSPATH)/NameServiceStub.cpp  \
           src/ida/IDAAuxVectors.cpp         \
           src/ida/IDADecoderRabin.cpp       \
           src/ida/IDADecoderIdentity.cpp    \
           src/ida/IDAEncoderRabin.cpp       \
           src/ida/IDAEncoderIdentity.cpp    \
           src/ida/IDAEncodingThread.cpp     \
           src/ida/LocalParityEncoder.cpp    \
           src/ida/IDAImpl.cpp               \
           src/jni/OppStoreBrokerJava.cpp

JNIFILES = $(JNIDIR)/$(JAVABROKERDIR)/OppStoreBroker.java  \
           $(JNIDIR)/$(JAVABROKERDIR)/OppStoreBrokerCallback.java \
           $(JNIDIR)/$(JAVABROKERDIR)/OppStoreBrokerTester.java

#           src/CkpLogger.cpp \
#           src/repository/CkpRepositoryStore.cpp \
#           src/repository/CkpIdaRepositoryStore.cpp \
#           src/repository/CkpReplicatedRepositoryStore.cpp \
#           src/repository/CkpParityRepositoryStore.cpp \
#           src/repository/CkpRepositoryStub.cpp \

include Makefile.vars

##########################################################
# Compiler Flags                                         #
##########################################################

CXX = g++
INCDIR = -I$(SHAREDPATH) -I$(LUAINCDIR)
#ifeq ($(DEBUG), true)
DEBUGFLAGS =  -g3 -O0 -fno-inline
#endif
WARN_FLAGS = #-Wall -pedantic -ansi
CPPFLAGS = -pthread -O2 -fPIC
LDFLAGS = -L$(LUALIBDIR) -Wl,-rpath,$(LUALIBDIR) -lm -llua -loilall -lluasocket  -lpthread -ldl -lssl
JNIINCDIR = -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux/

##########################################################
# Generating modified paths for objects and dependencies #
##########################################################

DIRLIST = $(dir $(CPPFILES))
FILELIST = $(notdir $(CPPFILES))
DIRDEPSLIST = $(addsuffix dependencies/, $(DIRLIST))
DIROBJSLIST = $(addsuffix objects/, $(DIRLIST))
OBJFILESLIST = $(join $(DIROBJSLIST), $(FILELIST))
DEPFILESLIST = $(join $(DIRDEPSLIST), $(FILELIST))
DIRDEPS = $(sort $(DIRDEPSLIST)) #Removes duplicate dirs
DIROBJS = $(sort $(DIROBJSLIST)) #Removes duplicate dirs

OBJFILES = $(OBJFILESLIST:.cpp=.o)
CPPDEPENDS = $(DEPFILESLIST:.cpp=.d)
transfname = $(patsubst %.o, %.d, $(subst objects/,dependencies/,$(1) ) )

all: first lib/broker.jar $(TARGET1) $(TARGET2) lib/libbroker.so

jar: first lib/broker.jar

-include $(CPPDEPENDS)

first:
	@mkdir -p $(DIRDEPS) $(DIROBJS)
	@mkdir -p $(JNIDIR)/classes

src/objects/%.o: src/%.cpp
	$(CXX) $(INCDIR) $(WARN_FLAGS) -c $(CPPFLAGS) $(DEBUGFLAGS)  -o $@ $<
	$(CXX) $(INCDIR) $(WARN_FLAGS) $(CPPFLAGS) -MM  $< > $(call transfname, $@)
	@mv -f $(call transfname, $@) $(call transfname, $@).tmp
	@sed -e 's|.*:|$@:|' < $(call transfname, $@).tmp > $(call transfname, $@)
	@sed -e 's/.*://' -e 's/\\$$//' < $(call transfname, $@).tmp | fmt -1 | sed -e 's/^ *//' -e 's/$$/:/' >> $(call transfname, $@)
	@rm -f $(call transfname, $@).tmp

src/ida/objects/%.o: src/ida/%.cpp
	$(CXX) $(INCDIR) $(WARN_FLAGS) -c $(CPPFLAGS) $(DEBUGFLAGS)  -o $@ $<
	$(CXX) $(INCDIR) $(WARN_FLAGS) $(CPPFLAGS) -MM  $< > $(call transfname, $@)
	@mv -f $(call transfname, $@) $(call transfname, $@).tmp
	@sed -e 's|.*:|$@:|' < $(call transfname, $@).tmp > $(call transfname, $@)
	@sed -e 's/.*://' -e 's/\\$$//' < $(call transfname, $@).tmp | fmt -1 | sed -e 's/^ *//' -e 's/$$/:/' >> $(call transfname, $@)
	@rm -f $(call transfname, $@).tmp

lib/broker.jar: $(JNIFILES)
	javac $+ -d $(JNIDIR)/classes
	javah -jni -o $(JNIDIR)/OppStoreBrokerJava.h -classpath $(JNIDIR)/classes br.usp.ime.oppstore.broker.OppStoreBroker

#jar cf lib/broker.jar -C $(JNIDIR)/classes .	

src/jni/objects/%.o: src/jni/%.cpp
	$(CXX) $(INCDIR) $(JNIINCDIR) $(WARN_FLAGS) -c $(CPPFLAGS) $(DEBUGFLAGS)  -o $@ $<
	$(CXX) $(INCDIR) $(JNIINCDIR) $(WARN_FLAGS) $(CPPFLAGS) -MM $< > $(call transfname, $@)
	@mv -f $(call transfname, $@) $(call transfname, $@).tmp
	@sed -e 's|.*:|$@:|' < $(call transfname, $@).tmp > $(call transfname, $@)
	@sed -e 's/.*://' -e 's/\\$$//' < $(call transfname, $@).tmp | fmt -1 | sed -e 's/^ *//' -e 's/$$/:/' >> $(call transfname, $@)
	@rm -f $(call transfname, $@).tmp

$(UTILSPATH)/objects/%.o: $(UTILSPATH)/%.cpp
	$(CXX) $(INCDIR) $(WARN_FLAGS) -c $(CPPFLAGS) $(DEBUGFLAGS)  -o $@ $<
	$(CXX) $(INCDIR) $(WARN_FLAGS) $(CPPFLAGS) -MM  $< > $(call transfname, $@)
	@mv -f $(call transfname, $@) $(call transfname, $@).tmp
	@sed -e 's|.*:|$@:|' < $(call transfname, $@).tmp > $(call transfname, $@)
	@sed -e 's/.*://' -e 's/\\$$//' < $(call transfname, $@).tmp | fmt -1 | sed -e 's/^ *//' -e 's/$$/:/' >> $(call transfname, $@)
	@rm -f $(call transfname, $@).tmp

lib/libbroker.so: $(OBJFILES)
	$(CXX) -shared -fPIC -o $@ $+ $(LDFLAGS)

TestRC4: src/TestRC4.cpp src/OppStoreUtils.cpp
	$(CXX) $(INCDIR) $(WARN_FLAGS) $(CPPFLAGS) $(DEBUGFLAGS) -o $@ $+ -lssl

$(TARGET1): src/$(TARGET1).cpp $(OBJFILES) 
	$(CXX) $(INCDIR) $(WARN_FLAGS) $(CPPFLAGS) $(DEBUGFLAGS) -o $@ $+ $(LDFLAGS)

$(TARGET2): src/$(TARGET2).cpp $(OBJFILES)
	$(CXX) $(INCDIR) $(WARN_FLAGS) $(CPPFLAGS) $(DEBUGFLAGS) -o $@ $+ $(LDFLAGS)

clean:
	rm -rf $(DIRDEPS) $(DIROBJS) $(TARGET) lib/libbroker.so $(JNIDIR)/classes $(JNIDIR)/OppStoreJava.h
