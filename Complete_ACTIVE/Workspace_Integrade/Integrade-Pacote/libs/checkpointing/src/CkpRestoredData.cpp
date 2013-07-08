#include "CkpRestoredData.hpp"
#include "CkpLogger.hpp"

#include <cstdlib>
#include <iostream>
#include <sstream>

using std::cout;
using std::cerr;
using std::endl;


//-------------------------------------------------------------------------
CkpRestoredData::CkpRestoredData() {
    ckpRestoredBytes = 0;
    dataConverters = NULL;
    ckpHeapData = false;
    ckpOnlyAdd = false;
    structSkipRecoverFlag = false;
    stackEndPos = 0;    
}

//-------------------------------------------------------------------------
CkpRestoredData::~CkpRestoredData() {
    delete dataConverters;
}

//-------------------------------------------------------------------------
int CkpRestoredData::ckpGetData(void *data, long nbytes, 
                                unsigned long type, void (*func)(void *)) {


  if (isBuiltIn(type)) {
    
    if (ckpHeapData == false) stackDataAddresses[ckpRestoredStackBytes] = data;
    
    int bytesRead = nbytes;
    if (dataConverters == NULL)
      memcpy(data, ckpRestoreCurrent, nbytes);
    else
      bytesRead = dataConverters->readData(data, ckpRestoreCurrent, type%CKPT_GLOBAL);      
    
    ckpRestoreCurrent     += bytesRead;
    ckpRestoredBytes      += bytesRead;
    ckpRestoredStackBytes += bytesRead;        
    
  }
  else if (type == CKPT_STRUCT) {
    (*func)(data);
    return 0;
  }
  // TODO: Reconstruct Pointer Data
  else if (isPrimitivePointer(type)) {
    
    if (ckpHeapData == false) 
      stackDataAddresses[ckpRestoredStackBytes] = data;
    
    ckp_datapos_t dataPos; // Contains the data position pointed by the pointer 
    if (dataConverters == NULL) 
      memcpy(&dataPos, ckpRestoreCurrent, sizeof(ckp_datapos_t));
    else 
      dataConverters->readData(&dataPos, ckpRestoreCurrent, CKPT_DATAPOS);      
    
    ckpRestoreCurrent      += sizeof(ckp_datapos_t);
    ckpRestoredBytes       += sizeof(ckp_datapos_t);     
    ckpRestoredStackBytes  += sizeof(ckp_datapos_t);
    
    /** NULL pointer was saved */        
    if (dataPos < 0) {
      //std::cout << "NULL pointer..." << std::endl;	
      *(void **)data = NULL; 
    }
    
    /** Pointer to a position in the execution stack 
     *  Pointer data is restored in the end of the recovering process*/
    else if (dataPos < stackEndPos) {
      //	std::cout << "------> Inside Stack..." << std::endl;
      stackDataPointersList.push_back( std::make_pair(data, dataPos) );
    }
    
    /** Pointer to a memory chunk in the the heap area */      
    else {
      DataChunksReadI dataChunksReadI = dataChunksRead.find(dataPos);
      /** Data from memory chunk needs to be restored */
      if ( dataChunksReadI == dataChunksRead.end() ) {
	
	ckp_datasize_t chunkSize; 
	if (dataConverters == NULL) 
	  memcpy(&chunkSize, (char *)ckpRestoreData + dataPos, sizeof(ckp_datasize_t));
	else 
	  dataConverters->readData(&chunkSize, (char *)ckpRestoreData + dataPos, CKPT_DATASIZE);      
	
	ckpRestoredBytes  += sizeof(ckp_datasize_t);             
	//-------------------------------------------------
	if ((type%CKPT_POINTER)/CKPT_POINTER_LEVEL == 0) { 
	  
	  if (dataConverters == NULL) {
	    char *tempData = (char *)ckp_malloc(chunkSize); // Allocates memory
	    dataChunksRead[dataPos] = tempData;  // In case this same chunk is needed by other pointer
	    
	    char *ckpRestoreTemp = ckpRestoreCurrent;
	    ckpRestoreCurrent = (char *)ckpRestoreData + dataPos + sizeof(ckp_datasize_t);
	    
	    memcpy(tempData, ckpRestoreCurrent, chunkSize);
	    ckpRestoredBytes += chunkSize;
	    ckpRestoreCurrent = ckpRestoreTemp;
	    
	    *(void **)data = (void *)tempData;
	  }
	  else {
	    
	    ckp_datasize_t sourceSize;  
	    ckp_datasize_t targetSize;            
	    
	    dataConverters->getSizes(&targetSize, &sourceSize, type % CKPT_POINTER);
	    
	    /** The new ammount of memory to be allocated */
	    ckp_datasize_t newChunkSize = targetSize*(chunkSize/sourceSize);
	    
	    char *tempData = (char *)ckp_malloc(newChunkSize); // Allocates memory
	    dataChunksRead[dataPos] = tempData;  // In case this same chunk is needed by other pointer
	    
	    char *ckpRestoreTemp = ckpRestoreCurrent;
	    ckpRestoreCurrent = (char *)ckpRestoreData + dataPos + sizeof(ckp_datasize_t);
	    
	    dataConverters->readDataVector(tempData, ckpRestoreCurrent, chunkSize, type % CKPT_POINTER);
	    
	    ckpRestoredBytes += chunkSize;
	    ckpRestoreCurrent = ckpRestoreTemp;
	    
	    *(void **)data = (void *)tempData;
	  }
	}
	else {
	  
	  ckp_datasize_t sourceSize, targetSize;
	  if (dataConverters == NULL) {
	    sourceSize = sizeof(ckp_datapos_t);
	    targetSize = sizeof(void *);
	  }
	  else {
	    dataConverters->getSizes(&targetSize, &sourceSize, CKPT_DATAPOS);
	    targetSize = sizeof(void *);
	  }
	  
	  /** The new ammount of memory to be allocated */
	  ckp_datasize_t newChunkSize = targetSize*(chunkSize/sourceSize);
	  
	  //	    cout << "Inside Pointer! newChunkSize=" << chunkSize << "|" << newChunkSize<< endl;
	  
	  void **tempData = (void **)ckp_malloc(newChunkSize); // Allocates memory
	  dataChunksRead[dataPos] = tempData;  // In case this same chunk is needed by other pointer
	  char *ckpRestoreTemp = ckpRestoreCurrent;
	  for (unsigned long p=0; newChunkSize >= targetSize; p+= 1) {
	    // cout << "-----> Iterating..." << endl;
	    ckpRestoreCurrent = (char *)ckpRestoreData + dataPos + sizeof(ckp_datasize_t) + p*sourceSize;
	    ckpRestoredStackBytes -= sizeof(ckp_datapos_t); // Will be updated later
	    ckpGetData(tempData+p, sizeof(void *), type - CKPT_POINTER_LEVEL, func);
	    newChunkSize -= targetSize;
	  }
	  ckpRestoreCurrent = ckpRestoreTemp;
	  *(void **)data = tempData;
	  
	  if (newChunkSize != 0) {
	    std::cout << "Error when converting a chunk of memory from a pointer. " 
		      << "Sizes do not match! Aborting..." << std::endl;
	    exit (-1); 
	  }
	  
	}
      }
      /** Data from memory chunk has already been restored */
      else {
	//std::cout << "Already added..." << std::endl;
	*(void **)data = (*dataChunksReadI).second;
      }
    }
  }
  // FIXME: Where is the support for vectors of structures!!!! 
  else if (type == CKPT_POINTER_STRUCT) { // In case of structs
    
    if (ckpHeapData == false) stackDataAddresses[ckpRestoredStackBytes] = data;
    
    ckp_datapos_t dataPos; 
    if (dataConverters == NULL) memcpy(&dataPos, ckpRestoreCurrent, sizeof(ckp_datapos_t));
    else dataConverters->readData(&dataPos, ckpRestoreCurrent, CKPT_DATAPOS);      
    //memcpy(&dataPos, ckpRestoreCurrent, sizeof(ckp_datapos_t));
    
    if (dataPos < 0) *(void **)data = NULL;
    else if (dataPos < stackEndPos) {
      //std::cout << "------> Inside Stack1..." << std::endl;
      stackDataPointersList.push_back( std::make_pair(data, dataPos) );
    }      
    else {
      DataChunksReadI dataChunksReadI = dataChunksRead.find(dataPos);
      
      if ( dataChunksReadI != dataChunksRead.end() )
	*(void **)data = (*dataChunksReadI).second;
      else  {
	
	// Indicates a pointer to a structure inside a structure
	if (structSkipRecoverFlag == true) {
	  ckpGetDataStruct *structData = new ckpGetDataStruct;
	  structData->data    = data;
	  structData->nbytes  = nbytes;
	  structData->type    = type;
	  structData->func    = func;
	  structData->dataPos = (char *)ckpRestoreCurrent - (char *)ckpRestoreData;
	  structPointerStack.push_back(structData);
	  return 0;
	}
	
	*(void **)data = ckp_malloc(nbytes); // Allocates memory
	dataChunksRead[dataPos] = *(void **)data;          
	char *ckpRestoreTemp = ckpRestoreCurrent;
	ckpRestoreCurrent = (char *)ckpRestoreData + dataPos;
	long ckpRestoredStackBytesTemp = ckpRestoredStackBytes; 
	ckpHeapData = true;
	structSkipRecoverFlag = true;
	
	(*func)(*(void **)data);
	
	structSkipRecoverFlag = false;
	ckpHeapData = false;
	ckpRestoreCurrent = ckpRestoreTemp;
	ckpRestoredStackBytes = ckpRestoredStackBytesTemp ;
      }
      
    }
    ckpRestoreCurrent      += sizeof(ckp_datapos_t);
    ckpRestoredBytes       += sizeof(ckp_datapos_t);              
    ckpRestoredStackBytes  += sizeof(ckp_datapos_t);
  }
  else {
    cerr << "ERROR!!!! CkpCompilerStack::ckpGetData -> Unsuported Type" << endl;
  }
  
  if (ckpOnlyAdd == true) return 0;
  
  while (structPointerStack.empty() == false) {
    
    ckpGetDataStruct *structData = structPointerStack.back();
    structPointerStack.pop_back();
    
    char *ckpRestoreTemp = ckpRestoreCurrent;
    ckpRestoreCurrent = (char *)ckpRestoreData + structData->dataPos;
    long ckpRestoredStackBytesTemp = ckpRestoredStackBytes; 
    
    ckpHeapData = true;
    ckpOnlyAdd  = true;
    ckpGetData(structData->data, structData->nbytes, structData->type, structData->func);
    ckpHeapData = false;
    ckpOnlyAdd  = false;
    
    ckpRestoreCurrent = ckpRestoreTemp;
    ckpRestoredStackBytes = ckpRestoredStackBytesTemp ;
    
    delete structData;
  }
  //    cout << endl;
  
  // Test if all data has been restored.
  if (ckpRestoredBytes == ckpRestoreNbytes) {
    
    ckpLogger.debug("Finished recovering checkpoint data. Releasing memory.");
    
    StackDataPointersList::const_iterator stackI = stackDataPointersList.begin();
    while (stackI != stackDataPointersList.end()) {
      *(void **)((*stackI).first) = stackDataAddresses[(*stackI).second];
      //assert((*stackI).first >= 0);
      stackI++;
    } 
    
    stackDataPointersList.clear();      
    free(ckpRestoreData);
  }
  
  return 0;
}

//-------------------------------------------------------------------------
int CkpRestoredData::ckpRestoreCkpData(CkpStore * ckpStore, int ckpNumber) {
  
  /* Recover the checkpointing data to 'ckpRestoreData' members and  *
   * the number of restored bytes to 'ckpRestoreNbytes' member.      */
  ckpStore->recoverCkpData(ckpRestoreData, ckpRestoreNbytes, ckpNumber);
  
  //ckpRestoreData = *data;
  ckpRestoreCurrent = (char *)ckpRestoreData;    
  
  ckp_arch_t dstArch = DataConverters::getProcessorArchitecture();
  ckp_arch_t srcArch = X86;
  ckpGetData(&srcArch, sizeof(ckp_arch_t), CKPT_ARCH, 0);
  
  if (srcArch != dstArch) {
    dataConverters = new DataConverters(dstArch);
    dataConverters->setDataConverter(srcArch);
  }
  
  //    stackDataPointersList.clear();
  
  {
      ostringstream logStr;
      logStr << "Recovering data from checkpoint. ckpSrcArch=" << (int)srcArch << " | ckpDstArch=" << (int)dstArch; 
      ckpLogger.debug( logStr.str() );        
  }
  
  ckpGetData(&stackEndPos, sizeof(ckp_datapos_t), CKPT_DATAPOS, 0);
  
  return ckpRestoreNbytes;
}

//-------------------------------------------------------------------------
