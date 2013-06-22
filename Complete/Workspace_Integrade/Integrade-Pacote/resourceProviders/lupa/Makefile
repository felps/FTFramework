TARGET = lupaCollector
TESTTARGET = lupaTester
TARGETLIB = lib/liblupa.so

##########################################################
# Source Files                                           #
##########################################################

CPPFILES = src/LinuxSystemAnalyzer.cpp \
           src/DataCollector.cpp \
           src/ResourceData.cpp \
           src/Timestamp.cpp \
           src/DataParser.cpp \
           src/UsageData.cpp \
           src/ClusterAnalyzer.cpp \
           src/CurrentTimestampFactory.cpp \
           src/Cluster.cpp \
           src/ClusteringAlgorithm.cpp \
           src/KMeansClusteringAlgorithm.cpp \
           src/UsagePredictor.cpp \
           src/LupaConstants.cpp \
           src/Lupa.cpp \
           src/RequestLog.cpp \
           src/RequestLogger.cpp


TARGETFILE = 	src/StandaloneDataCollectorApp.cpp

TESTFILES = 	src/tests/DataCollectorTester.cpp \
		src/tests/TimestampTester.cpp\
		src/tests/ClusterTester.cpp\
		src/tests/UsageDataTester.cpp\
		src/tests/LupaTester.cpp \
		src/tests/TestCurrentTimestampFactory.cpp \
		src/tests/TestSystemAnalyzer.cpp \
		src/tests/ResourceDataTester.cpp \
		src/tests/ClusteringAlgorithmsTester.cpp \
		src/tests/ClusterAnalyzerTester.cpp \
		src/tests/RequestLogTester.cpp \
		src/tests/UsagePredictorTester.cpp
 

##########################################################
# Compiler Flags                                         #
##########################################################

CXX = g++
ifeq ($(DEBUG), true)
DEBUGFLAGS =  -g3 -O0 -fno-inline 
endif
WARN_FLAGS = -Wall -pedantic -ansi
CPPFLAGS = -O2 -fPIC
LDFLAGS = -Wl,-rpath, -lm -pthread


##########################################################
# Generating modified paths for objects and dependencies #
##########################################################

#DEPENDENCIES
FILES = $(CPPFILES) $(TARGETFILE) $(TESTFILES)
DIRLIST = $(dir $(FILES)) 
FILELIST = $(notdir $(FILES)) 
DIRDEPSLIST = $(addsuffix dependencies/, $(DIRLIST))
DEPFILESLIST = $(join $(DIRDEPSLIST), $(FILELIST))
DIRDEPS = $(sort $(DIRDEPSLIST)) #Removes duplicate dirs
CPPDEPENDS = $(DEPFILESLIST:.cpp=.d)
transfname = $(patsubst %.o, %.d, $(subst objects/,dependencies/,$(1) ) )

#OBJECTS
OBJFILESLIST = $(join $(addsuffix objects/, $(dir $(CPPFILES))), $(notdir $(CPPFILES)))
TARGETFILESLIST = $(join $(addsuffix objects/, $(dir $(TARGETFILE))), $(notdir $(TARGETFILE)))
TESTFILESLIST = $(join $(addsuffix objects/, $(dir $(TESTFILES))), $(notdir $(TESTFILES)))
OBJFILES = $(OBJFILESLIST:.cpp=.o) # do not consider test files
TARGETOBJFILE = $(TARGETFILESLIST:.cpp=.o) # consider only target file (executable)
TESTOBJFILES = $(TESTFILESLIST:.cpp=.o) # consider only test files
DIROBJS = $(sort $(dir $(OBJFILESLIST))) $(sort $(dir $(TARGETFILESLIST))) $(sort $(dir $(TESTFILESLIST))) #Removes duplicate dirs

#$(info $(CPPDEPENDS))

all: first $(TARGET) $(TARGETLIB)

-include $(CPPDEPENDS)

first:
	@mkdir -p $(DIRDEPS) $(DIROBJS) $(dir $(TARGETLIB))

tests: first $(TESTTARGET)

src/objects/%.o: src/%.cpp
	$(CXX) $(INCDIR) $(WARN_FLAGS) -c $(CPPFLAGS) $(DEBUGFLAGS)  -o $@ $<
	$(CXX) $(INCDIR) $(WARN_FLAGS) $(CPPFLAGS) -MM  $< > $(call transfname, $@)
	@mv -f $(call transfname, $@) $(call transfname, $@).tmp
	@sed -e 's|.*:|$@:|' < $(call transfname, $@).tmp > $(call transfname, $@)
	@sed -e 's/.*://' -e 's/\\$$//' < $(call transfname, $@).tmp | fmt -1 | sed -e 's/^ *//' -e 's/$$/:/' >> $(call transfname, $@)
	@rm -f $(call transfname, $@).tmp

src/tests/objects/%.o: src/tests/%.cpp
	$(CXX) $(INCDIR) $(WARN_FLAGS) -c $(CPPFLAGS) $(DEBUGFLAGS)  -o $@ $<
	$(CXX) $(INCDIR) $(WARN_FLAGS) $(CPPFLAGS) -MM  $< > $(call transfname, $@)
	@mv -f $(call transfname, $@) $(call transfname, $@).tmp
	@sed -e 's|.*:|$@:|' < $(call transfname, $@).tmp > $(call transfname, $@)
	@sed -e 's/.*://' -e 's/\\$$//' < $(call transfname, $@).tmp | fmt -1 | sed -e 's/^ *//' -e 's/$$/:/' >> $(call transfname, $@)
	@rm -f $(call transfname, $@).tmp

$(TARGET): $(OBJFILES) $(TARGETOBJFILE)
	$(CXX) $(INCDIR) $(WARN_FLAGS) $(CPPFLAGS) $(DEBUGFLAGS) -o $@ $+ $(LDFLAGS)
ifeq ($(DEBUG), false)
	strip -s $(TARGET)
endif

$(TARGETLIB): $(OBJFILES)
	$(CXX) $(CPPFLAGS) -shared -o $@ $+ $(LDFLAGS) 

lib: first $(TARGETLIB)

$(TESTTARGET): $(OBJFILES) $(TESTOBJFILES)
	$(CXX) $(INCDIR) $(WARN_FLAGS) $(CPPFLAGS) $(DEBUGFLAGS) -o $@ $+ $(LDFLAGS) -ldl -lcppunit

clean:
	rm -rf $(DIRDEPS) $(DIROBJS) $(TARGET) $(TESTTARGET) $(TARGETLIB) $(dir $(TARGETLIB)) *~ src/*~ src/tests/*~ fileAnalyzer predictionTester

#  daqui pra baixo é coisa de teste #

all_tests: tests __fileAnalyzer __predictionTester

fileAnalyzer: first __fileAnalyzer

__fileAnalyzer: $(OBJFILES) src/tests/objects/LogFileAnalyzer.o
	$(CXX) $(INCDIR) $(WARN_FLAGS) $(CPPFLAGS) $(DEBUGFLAGS) -o fileAnalyzer $+ $(LDFLAGS) 

predictionTester: first __predictionTester

__predictionTester: $(OBJFILES) src/tests/objects/PredictionTester.o
	$(CXX) $(INCDIR) $(WARN_FLAGS) $(CPPFLAGS) $(DEBUGFLAGS) -o predictionTester $+ $(LDFLAGS)



