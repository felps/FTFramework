UTILSPATH=../../shared/utils/c++
LIBSRCDIR=SystemOperations
POSIXLIBDIR=$(LIBSRCDIR)/POSIX

LIBDIR=lib
HEADERDIR=include
BINDIR=bin

##########################################################
# Source Files                                                                                                                         #
##########################################################

LIBCPPFILES = $(LIBSRCDIR)/Directory.cpp                \
              $(LIBSRCDIR)/SystemOperationException.cpp \
              $(POSIXLIBDIR)/POSIXDirectory.cpp         \
              $(POSIXLIBDIR)/POSIXProcess.cpp           \
              $(POSIXLIBDIR)/POSIXUtils.cpp             \
              $(UTILSPATH)/CharArrayArrayBeautifier.cpp \
              $(UTILSPATH)/StringTokenizer.cpp          

HEADERFILES = $(LIBSRCDIR)/Process.hpp                  \
              $(LIBSRCDIR)/Thread.hpp                   \
              $(LIBSRCDIR)/ConditionVariable.hpp        \
              $(LIBSRCDIR)/Directory.hpp                \
              $(LIBSRCDIR)/Mutex.hpp                    \
              $(LIBSRCDIR)/ProcessDataTypes.hpp         \
              $(LIBSRCDIR)/SystemOperationException.hpp \
              $(LIBSRCDIR)/ProcessFactory.hpp           \
              $(LIBSRCDIR)/ThreadFactory.hpp            \
              $(LIBSRCDIR)/ConditionVariableFactory.hpp \
              $(LIBSRCDIR)/MutexFactory.hpp            

##########################################################
# Compiler Flags                                                                                                                       #
##########################################################

CXX = g++
INCDIR = -I$(UTILSPATH) -I$(LIBSRCDIR) -I$(POSIXLIBDIR)
DEBUG = true
ifeq ($(DEBUG), true)
DEBUGFLAGS = -g3 -O0 -fno-inline
endif
WARN_FLAGS = -Wall -pedantic -ansi
CPPFLAGS = -DPOSIX 
#-pthread -fPIC
LDFLAGS= -L$(LIBDIR) -Wl,-rpath,. -lSystemOperations -lpthread

##########################################################
# Generating modified paths for objects and dependencies                                                              #
##########################################################

DIRLIST = $(dir $(LIBCPPFILES))
FILELIST = $(notdir $(LIBCPPFILES))
DIRDEPSLIST = $(addsuffix dependencies/, $(DIRLIST))
DIROBJSLIST = $(addsuffix objects/, $(DIRLIST))
OBJFILESLIST = $(join $(DIROBJSLIST), $(FILELIST))
DEPFILESLIST = $(join $(DIRDEPSLIST), $(FILELIST))
DIRDEPS = $(sort $(DIRDEPSLIST)) #Removes duplicate dirs
DIROBJS = $(sort $(DIROBJSLIST)) #Removes duplicate dirs

OBJFILES = $(OBJFILESLIST:.cpp=.o)
CPPDEPENDS = $(DEPFILESLIST:.cpp=.d)
transfname = $(patsubst %.o, %.d, $(subst objects/,dependencies/,$(1) ) )

-include $(CPPDEPENDS)

all: first \
	 $(LIBDIR)/libSystemOperations.so    \
	 $(LIBDIR)/libSystemOperations.a     \
     $(BINDIR)/SampleCorrectApp          \
     $(BINDIR)/SampleCrashingApplication \
	 $(BINDIR)/SampleLongRunningApp      \
	 $(BINDIR)/TestSystemOperations

first:
	@mkdir -p $(DIRDEPS) $(DIROBJS) $(LIBDIR) $(HEADERDIR) $(BINDIR)
	@cp $(HEADERFILES) $(HEADERDIR)
	

$(LIBSRCDIR)/objects/%.o: SystemOperations/%.cpp
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

$(LIBDIR)/libSystemOperations.so: $(LIBCPPFILES)
	$(CXX) $(INCDIR) $(WARN_FLAGS) $(CPPFLAGS) $(DEBUGFLAGS) -shared -o $@ $+ 
ifeq ($(DEBUG), false)
	strip -s $(TARGET)
endif

$(LIBDIR)/libSystemOperations.a: $(LIBCPPFILES)
	ar rcs $@ $+

$(BINDIR)/SampleCorrectApp: SampleCorrectApp/SampleCorrectApp.cpp
	$(CXX) -o $@ $+
	
$(BINDIR)/SampleCrashingApplication: SampleCrashingApplication/SampleCrashingApplication.cpp
	$(CXX) -o $@ $+ 
	
$(BINDIR)/SampleLongRunningApp: SampleLongRunningApp/SampleLongRunningApp.cpp
	$(CXX) -o $@ $+ 

$(BINDIR)/TestSystemOperations: $(LIBDIR)/libSystemOperations.so
	cp $+ $(BINDIR)
	$(CXX) -ISystemOperations $(WARN_FLAGS) $(CPPFLAGS) $(DEBUGFLAGS) TestSystemOperations/TestSystemOperations.cpp -o $@ $(LDFLAGS)
ifeq ($(DEBUG), false)
	strip -s TestSystemOperations
endif

clean:
	rm -rf $(DIRDEPS) $(DIROBJS) $(TARGET) $(LIBDIR) $(HEADERDIR) $(BINDIR)
