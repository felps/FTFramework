TARGET1 = testCompiler
TARGET2 = testScope
TARGET3 = TestStore

BSPLIB_DIR=../bspLib
CONVERTER_DIR=../dataConverters
UTILSPATH=../../shared/utils/c++
BROKERPATH=../broker

CPPFILES = src/CheckpointingLib.cpp \
           src/CheckpointTimer.cpp \
           src/CkpCompilerStack.cpp \
           src/CkpRestoredData.cpp \
           src/StackMemoryPositions.cpp \
           src/HeapController.cpp \
           src/CkpStore.cpp \
           src/CkpLogger.cpp \
           src/CkpUtils.cpp \
           src/repository/CkpRepositoryStore.cpp \
           src/repository/CkpIdaRepositoryStore.cpp \
           src/repository/CkpReplicatedRepositoryStore.cpp \
           src/repository/CkpParityRepositoryStore.cpp \
           src/repository/CkpRepositoryStub.cpp \
           src/repository/CkpOppStore.cpp \
           $(UTILSPATH)/CkpReposManagerStub.cpp \
           $(UTILSPATH)/Config.cpp \
           $(UTILSPATH)/StringTokenizer.cpp \
           $(UTILSPATH)/LuaUtils.cpp \
           $(UTILSPATH)/OrbUtils.cpp \
           $(UTILSPATH)/NameServiceStub.cpp           

CPPFILES_BSP = src/BspLibCkp.cpp

include Makefile.vars

##########################################################
# Compiler Flags                                         #
##########################################################

INCDIR = -I../../shared -Iinclude -I$(BSPLIB_DIR)/include -I$(CONVERTER_DIR)/include -I$(BROKERPATH)/src -I$(LUAINCDIR)
CXX = g++
#ifeq ($(DEBUG), true)
DEBUGFLAGS =  -g3 -O0 -fno-inline
#endif
WARN_FLAGS = -Wall -ansi -pedantic
CPPFLAGS   = -pthread -fPIC #-O2
LDFLAGS_LUA = -L$(LUALIBDIR) -Wl,-rpath,$(LUALIBDIR) -llua -loilall -lluasocket -ldl -L$(BROKERPATH)/lib -Wl,-rpath,$(BROKERPATH)/lib -lbroker
LDFLAGS_BSP = -L$(BSPLIB_DIR)/lib -Wl,-rpath,$(BSPLIB_DIR)/lib -lbsp

##########################################################
# Generating modified paths for objects and dependencies #
##########################################################

DIRLIST_BSP      = $(dir $(CPPFILES_BSP))
DIRDEPSLIST_BSP  = $(addsuffix dependencies/, $(DIRLIST_BSP))
DIROBJSLIST_BSP  = $(addsuffix objects/, $(DIRLIST_BSP))
FILELIST_BSP     = $(notdir $(CPPFILES_BSP))
OBJFILESLIST_BSP = $(join $(DIROBJSLIST_BSP), $(FILELIST_BSP))
DEPFILESLIST_BSP = $(join $(DIRDEPSLIST_BSP), $(FILELIST_BSP))
DIRDEPS_BSP      = $(sort $(DIRDEPSLIST_BSP)) #Removes duplicate dirs
DIROBJS_BSP      = $(sort $(DIROBJSLIST_BSP)) #Removes duplicate dirs
OBJFILES_BSP     = $(OBJFILESLIST_BSP:.cpp=.o)
CPPDEPENDS_BSP   = $(DEPFILESLIST_BSP:.cpp=.d)

DIRLIST      = $(dir $(CPPFILES))
DIRDEPSLIST  = $(addsuffix dependencies/, $(DIRLIST))
DIROBJSLIST  = $(addsuffix objects/, $(DIRLIST))
FILELIST     = $(notdir $(CPPFILES))
OBJFILESLIST = $(join $(DIROBJSLIST), $(FILELIST))
DEPFILESLIST = $(join $(DIRDEPSLIST), $(FILELIST))
DIRDEPS      = $(sort $(DIRDEPSLIST)) #Removes duplicate dirs
DIROBJS      = $(sort $(DIROBJSLIST)) #Removes duplicate dirs
OBJFILES_CKP   = $(OBJFILESLIST:.cpp=.o)
CPPDEPENDS_CKP = $(DEPFILESLIST:.cpp=.d)

transfname = $(patsubst %.o, %.d, $(subst objects/,dependencies/,$(1) ) )

all: first libs testCkpTmp #testCkpTmp TestStore testCkpGraph

libs: libckp.so libckpbsp.so

-include $(CPPDEPENDS_CKP) $(CPPDEPENDS_BSP)

first:
	@mkdir -p lib $(DIRDEPS) $(DIROBJS)

../../shared/utils/c++/objects/%.o: ../../shared/utils/c++/%.cpp
	$(CXX) $(INCDIR) $(WARN_FLAGS) -c $(CPPFLAGS) $(DEBUGFLAGS)  -o $@ $<
	$(CXX) $(INCDIR) $(WARN_FLAGS) $(CPPFLAGS) -MM  $< > $(call transfname, $@)
	@mv -f $(call transfname, $@) $(call transfname, $@).tmp
	@sed -e 's|.*:|$@:|' < $(call transfname, $@).tmp > $(call transfname, $@)
	@sed -e 's/.*://' -e 's/\\$$//' < $(call transfname, $@).tmp | fmt -1 | sed -e 's/^ *//' -e 's/$$/:/' >> $(call transfname, $@)
	@rm -f $(call transfname, $@).tmp

src/objects/%.o: src/%.cpp
	$(CXX) $(INCDIR) $(WARN_FLAGS) -c $(CPPFLAGS) $(DEBUGFLAGS)  -o $@ $<
	$(CXX) $(INCDIR) $(WARN_FLAGS) $(CPPFLAGS) -MM  $< > $(call transfname, $@)
	@mv -f $(call transfname, $@) $(call transfname, $@).tmp
	@sed -e 's|.*:|$@:|' < $(call transfname, $@).tmp > $(call transfname, $@)
	@sed -e 's/.*://' -e 's/\\$$//' < $(call transfname, $@).tmp | fmt -1 | sed -e 's/^ *//' -e 's/$$/:/' >> $(call transfname, $@)
	@rm -f $(call transfname, $@).tmp

src/repository/objects/%.o: src/repository/%.cpp
	$(CXX) $(INCDIR) $(WARN_FLAGS) -c $(CPPFLAGS) $(DEBUGFLAGS)  -o $@ $<
	$(CXX) $(INCDIR) $(WARN_FLAGS) $(CPPFLAGS) -MM  $< > $(call transfname, $@)
	@mv -f $(call transfname, $@) $(call transfname, $@).tmp
	@sed -e 's|.*:|$@:|' < $(call transfname, $@).tmp > $(call transfname, $@)
	@sed -e 's/.*://' -e 's/\\$$//' < $(call transfname, $@).tmp | fmt -1 | sed -e 's/^ *//' -e 's/$$/:/' >> $(call transfname, $@)
	@rm -f $(call transfname, $@).tmp

testCkpTmp: libs src/testCkpTmp.cpp
	$(CXX) $(INCDIR) $(WARN_FLAGS) $(CPPFLAGS) $(DEBUGFLAGS) -o $@ src/testCkpTmp.cpp -Llib -Wl,-rpath,lib -lckp $(LDFLAGS_LUA)

testCkpGraph: libs src/testCkpGraph.cpp
	$(CXX) $(INCDIR) $(WARN_FLAGS) $(CPPFLAGS) $(DEBUGFLAGS) -o $@ src/testCkpGraph.cpp -Llib -Wl,-rpath,lib -lckp

testIda2: $(OBJFILES_CKP) src/ida/testIda2.cpp
	$(CXX) $(INCDIR) $(WARN_FLAGS) $(CPPFLAGS) $(DEBUGFLAGS) -o $@ src/ida/testIda2.cpp  src/ida/objects/IDAAuxVectors.o src/ida/objects/IDADecoderRabin.o src/ida/objects/IDADecoderIdentity.o src/ida/objects/IDAEncoderRabin.o src/ida/objects/IDAEncoderIdentity.o src/ida/objects/LocalParityEncoder.o src/ida/objects/IDAImpl.o

# Tests the CkpRepositoryStore
TestStore: src/TestStore.cpp libckp.so 
	$(CXX) $(INCDIR) $(WARN_FLAGS) $(CPPFLAGS) $(DEBUGFLAGS) -o $@ $+ -Llib -Wl,-rpath,lib -lckp

libckp.so: $(OBJFILES_CKP)
	$(CXX) -shared -fPIC -o lib/$@ $+ $(CONVERTER_DIR)/lib/libconverter.a $(LDFLAGS_LUA)

libckpbsp.so: $(OBJFILES_CKP) $(OBJFILES_BSP)
	$(CXX) -shared -fPIC -o lib/$@ $+ $(CONVERTER_DIR)/lib/libconverter.a $(LDFLAGS_BSP) $(LDFLAGS_LUA)

clean:
	rm -rf lib $(DIRDEPS) $(DIROBJS)

