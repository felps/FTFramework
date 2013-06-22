################################################################################
# Automatically-generated file. Do not edit!
################################################################################

CFILES = src/LuaUtils.c       \
         src/OilUtils.c        \
         src/OilOrb.c        \
         src/NameServiceStub.c 

include Makefile.vars

##########################################################
# Compiler Flags                                         #
##########################################################

INCDIR = -I$(LUAINCDIR) 
CC = gcc
ifeq ($(DEBUG), true)
DEBUGFLAGS =  -g3 -O0 -fno-inline
endif
WARN_FLAGS = -Wall -ansi -pedantic
CFLAGS = -pthread -O2 -fPIC
LDFLAGS = -L$(LUALIBDIR) -Wl,-rpath,$(LUALIBDIR) -llua -lm -loilall -lluasocket -lpthread 

##########################################################
# Generating modified paths for objects and dependencies #
##########################################################

DIRLIST = $(dir $(CFILES))
FILELIST = $(notdir $(CFILES))
DIROBJSLIST = $(addsuffix objects/, $(DIRLIST))
OBJFILESLIST = $(join $(DIROBJSLIST), $(FILELIST))
DIROBJS = $(sort $(DIROBJSLIST)) #Removes duplicate dirs

OBJFILES = $(OBJFILESLIST:.c=.o)

all: first libConnOil.so libConnOil.a

first:
	@mkdir -p lib $(DIROBJS)

src/objects/%.o: src/%.c
	$(CC) $(INCDIR) $(WARN_FLAGS) -c $(CFLAGS) $(DEBUGFLAGS)  -o $@ $<

libConnOil.so: $(OBJFILES)
	$(CC) $(INCDIR) $(WARN_FLAGS) $(CFLAGS) $(DEBUGFLAGS) -shared -o lib/$@ $+ $(LDFLAGS)
ifeq ($(DEBUG), false)
	strip -s lib/$@
endif

libConnOil.a: $(OBJFILES)
	ar rcs lib/$@ $+

clean:
	rm -rf lib $(DIROBJS)
