TARGET = LrmLauncher

UTILSPATH=../../shared/utils/c++
ARSC_PATH=../../shared/arsc/C++/src/objects
BROKER_PATH=../../libs/broker/
LUPALIBPATH=../lupa/lib
DEBUG=true
##########################################################
# Source Files                                           #
##########################################################

CPPFILES = src/NodeStaticInformation.cpp  \
           src/DynamicInformationMonitor.cpp \
           src/CpuUsage.cpp       \
           src/LrmImpl.cpp        \
           src/LrmLauncher.cpp    \
           src/LrmSkeleton.cpp    \
           src/LinuxProcess.cpp   \
           src/LinuxSpecifics.cpp \
           src/GrmStub.cpp        \
           src/AsctStub.cpp       \
           src/ExecutionManagerStub.cpp      \
           src/ApplicationRepositoryStub.cpp \
           $(CPPARSCFILE) \
           $(UTILSPATH)/CharArrayArrayBeautifier.cpp   \
           $(UTILSPATH)/StringTokenizer.cpp \
           $(UTILSPATH)/Config.cpp          \
           $(UTILSPATH)/LuaUtils.cpp        \
           $(UTILSPATH)/OrbUtils.cpp         \
           $(UTILSPATH)/NetInfo.cpp         \
           $(UTILSPATH)/Condition.cpp \
           $(UTILSPATH)/NameServiceStub.cpp

include Makefile.vars

##########################################################
# Compiler Flags                                         #
##########################################################

CXX = g++
INCDIR = -I../../shared -I$(LUAINCDIR) -I$(BROKER_PATH)/src/
DEBUG = true
#ifeq ($(DEBUG), true)
DEBUGFLAGS = -g3 -O0 -fno-inline
#endif
WARN_FLAGS = -Wall -pedantic -ansi
#CPPFLAGS = -pthread -O2 -fPIC $(CPPARSCGFLAGS)
CPPFLAGS = -pthread -fPIC $(CPPARSCGFLAGS)
SECURITYFLAGS = $(CRYPTO++LIBDIR) $(KERBEROSLIBDIR) 
LDFLAGS = $(SECURITYFLAGS) -L$(BROKER_PATH)/lib/ -Wl,-rpath,$(BROKER_PATH)/lib/ -L$(LUPALIBPATH) -Wl,-rpath,$(LUPALIBPATH) -L$(LUALIBDIR) -Wl,-rpath,$(LUALIBDIR) -lm -llua -loilall -lluasocket  -lpthread -ldl -lbroker -llupa

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

all: first $(TARGET)

-include $(CPPDEPENDS)

first:
	@mkdir -p $(DIRDEPS) $(DIROBJS)

src/objects/%.o: src/%.cpp
	$(CXX) $(INCDIR) $(WARN_FLAGS) -c $(CPPFLAGS) $(DEBUGFLAGS)  -o $@ $<
	$(CXX) $(INCDIR) $(WARN_FLAGS) $(CPPFLAGS) -MM  $< > $(call transfname, $@)
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

$(TARGET): $(OBJFILES) $(ARSC_OBJECTS)
	$(CXX) $(INCDIR) $(WARN_FLAGS) $(CPPFLAGS) $(DEBUGFLAGS) -o $@ $+ $(LDFLAGS)
ifeq ($(DEBUG), false)
	strip -s $(TARGET)
endif

clean:
	rm -rf $(DIRDEPS) $(DIROBJS) $(TARGET) [0-9]*
