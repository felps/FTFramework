BINS=regular mpi
TEST_DIR=/tmp/integrade_test

all:
	@echo "Don't run all, run for each target, like: make regular mpi"
	@echo "make 64: configure apps for 64bits"
	@echo "make 32: undo changes for 64bits"

# Targets to create dirs 
create:
	mkdir -p ${TEST_DIR}

# Targets to clean dirs
clean_directory:
	rm -Rf ${TEST_DIR}

clean: clean_directory
	rm -rf *~ ${BINS}

# ???
junit: clean create

# Regular Application
regular: create regular.cpp
	g++ regular.cpp -o regular
	cp regular regular.conf regular.expected ${TEST_DIR}

# Mpi Application
mpi: create mpi.c
	mpicc mpi.c -o mpi.bin
	cp mpi mpi.conf mpi.expected ${TEST_DIR}

# Parametric Application
parametric: create parametric.c
	gcc parametric.c -o parametric
	cp parametric parametric.conf parametric.expected parametric_file ${TEST_DIR}

64:
	sed -i 's/Linux_i686/Linux_x86_64/' *.conf
	@echo Done

32:
	sed -i 's/Linux_x86_64/Linux_i686/' *.conf
	@echo Done
