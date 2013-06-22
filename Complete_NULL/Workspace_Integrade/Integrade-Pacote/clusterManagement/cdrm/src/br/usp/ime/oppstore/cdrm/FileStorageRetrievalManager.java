package br.usp.ime.oppstore.cdrm;

import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import br.usp.ime.oppstore.FileInformation;
import br.usp.ime.oppstore.FileFragmentIndex;
import br.usp.ime.oppstore.InvalidFragmentKeyException;
import br.usp.ime.oppstore.cdrm.CdrmApp.OpStoreIdProtocol;
import br.usp.ime.oppstore.corba.AccessBroker;
import br.usp.ime.oppstore.message.FileFragmentIndexMessage;
import br.usp.ime.oppstore.message.StoreFragmentListMessage;
import br.usp.ime.oppstore.message.StoreFragmentMessage;
import br.usp.ime.oppstore.message.FileFragmentIndexMessage.RequestType;
import br.usp.ime.virtualId.VirtualNode;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.IdFactory;
import rice.pastry.commonapi.PastryIdFactory;

/**
 * Allows the creation and management of file storage in the network.
 * Used when creating FFIs, and sending the FFI and fragments for storage in the network.
 * 
 * @author rcamargo
 *
 */
public class FileStorageRetrievalManager {
    
    private RequestSourceBrokerManager requestSourceBrokerManager;
    private HashMap <Integer, FileInformation> fileInformationMap;
    private VirtualNode virtualNode;
    private Logger logger;
    private IdFactory idFactory;
    
    public FileStorageRetrievalManager ( VirtualNode virtualNode ) {
    	
    	this.idFactory = new PastryIdFactory( virtualNode.getNode().getEnvironment() );
    	this.virtualNode = virtualNode;
        this.fileInformationMap = new HashMap <Integer, FileInformation>();
        this.requestSourceBrokerManager = new RequestSourceBrokerManager();
        this.logger = Logger.getLogger("request." + virtualNode.getNode().getId().toString().substring(0, 9) + ">");
    }
    
    public void storeFileInformationIndex(int requestNumber, int[] notStoredFragmentIndexList, byte[][] fragmentHashList, byte[] finalFileKey) {
        
        FileInformation fileInformation = fileInformationMap.get(requestNumber);
        fileInformation.fileId = idFactory.buildId(finalFileKey);

        // TODO: Implement what happens when some fragments are not stored
        FileFragmentIndex fileFragmentIndex = fileInformation.createFileFragmentIndex();
        if (notStoredFragmentIndexList.length > 0)
        	fileFragmentIndex.removeFragment(notStoredFragmentIndexList);
        fileFragmentIndex.updateFragmentHashes(fragmentHashList);
        fileFragmentIndex.updateFileKey(finalFileKey);
        
        // Calls the CDRM method to store the fileInformation
        if (fileFragmentIndex.fragmentSizeList.length >= fileFragmentIndex.neededFragments) {                	
        	FileFragmentIndexMessage ffiMessage = 
        		new FileFragmentIndexMessage( fileInformation.fileId, fileFragmentIndex, RequestType.STORAGE, 
        				FileInformation.numberOfReplicas, virtualNode.getNode().getLocalNodeHandle(), requestNumber); 
        	
        	logger.debug("FileStorageRetrievalManager -> Storing FileFragmentIndex for file " + fileInformation.fileId + " timeoutMinutes: " + fileFragmentIndex.timeoutMinutes + ".");
        	virtualNode.routeMessage(fileInformation.fileId, ffiMessage);        	
        }
        
        else {

        	logger.debug("FileStorageRetrievalManager -> Could not obtain enough fragments for FFI of file " + fileInformation.fileId + ".");
        	
            AccessBroker sourceAccessBroker = requestSourceBrokerManager.getRequestSource(requestNumber);
            assert(sourceAccessBroker != null);                                    
            fileInformationMap.remove(requestNumber);
            
            sourceAccessBroker.setFileStorageRequestFailed( requestNumber );
        }                
    }
    
    public int getRequestNumber(AccessBroker source) {
    	return requestSourceBrokerManager.getRequestNumber(source);
    }
    
    public void printBrokerAddress (String brokerAddress) {
    	logger.debug("brokerAdress: " + brokerAddress);
    }
    
	public int requestFragmentStorageLocations(
			int requestNumber, int fileSize, int[] fragmentSizeArray, Id fileId, Vector<Id> fragmentIdList, int neededFragments, int timeoutMinutes, boolean storeGlobal, String brokerAddress) {
		        
        fileInformationMap.put(new Integer(requestNumber), new FileInformation(fileId, fragmentIdList, fileSize, fragmentSizeArray, neededFragments, timeoutMinutes));

        StringBuffer fragmentList = new StringBuffer();
        for ( Id id : fragmentIdList) {
        	fragmentList.append(id);
        	fragmentList.append(" ");
        }
        logger.debug("FileStorageRetrievalManager -> Requesting fragment storage locations for file " + fileId + " timeoutMinutes: " + timeoutMinutes + ".\n" + fragmentList);
        
        if ( storeGlobal == true ) {

        	for (int fragment=0; fragment < fragmentIdList.size(); fragment++) {       
                StoreFragmentMessage storeMessage = new StoreFragmentMessage (
                        fileId, fragmentIdList.get(fragment), fragmentSizeArray[fragment], virtualNode.getNode().getLocalNodeHandle(), requestNumber, timeoutMinutes, brokerAddress);
                
                if (CdrmApp.idProtocol == OpStoreIdProtocol.PASTRY)
                	storeMessage.routingVirtualSpaceNumber = 0;

                if (fragment == 0 && fragmentSizeArray[0] == fileSize) {
                	storeMessage.isCacheRequest = true;
                	virtualNode.sendDirectMessage(virtualNode.getNode().getLocalHandle(), storeMessage);
                }
                else { 
                	storeMessage.isCacheRequest = false;
                	virtualNode.routeMessage(fragmentIdList.get(fragment), storeMessage);
                }
            }
        }

        else { // storeGlobal == false

        	Vector<Long> fragmentSizeList = new Vector<Long>();
            for (long fragmentSize : fragmentSizeArray)
                fragmentSizeList.add(fragmentSize);
            
            StoreFragmentListMessage storeFragmentListMessage = new StoreFragmentListMessage (
                    fileId, fragmentIdList, fragmentSizeList, virtualNode.getNode().getLocalNodeHandle(), requestNumber, timeoutMinutes, brokerAddress);

            virtualNode.sendDirectMessage(virtualNode.getNode().getLocalHandle(), storeFragmentListMessage);

        }
		return requestNumber;
	}
	
    public int retrieveFileInformationIndex(Id fileId, AccessBroker sourceAccessBroker, RequestType requestType) {
        
        int requestNumber = requestSourceBrokerManager.getRequestNumber(sourceAccessBroker);        

        FileFragmentIndexMessage fileInformationMessage = new FileFragmentIndexMessage (
                fileId, null, requestType, FileInformation.numberOfReplicas, virtualNode.getNode().getLocalNodeHandle(), requestNumber);
        
        logger.debug("FileStorageRetrievalManager -> Retrieving FileFragmentIndex for file " + fileId + ".");
        
        // routes the message in the Network                
        virtualNode.routeMessage(fileId, fileInformationMessage);
        
        return requestNumber;
    }

    public int renewFileLease(Id fileId, AccessBroker sourceAccessBroker, int timeout) {
        
        int requestNumber = requestSourceBrokerManager.getRequestNumber(sourceAccessBroker);        

        FileFragmentIndexMessage fileInformationMessage = new FileFragmentIndexMessage (
                fileId, null, RequestType.RENEWAL, FileInformation.numberOfReplicas, virtualNode.getNode().getLocalNodeHandle(), requestNumber);
        fileInformationMessage.newTimeout = timeout;
        
        logger.debug("FileStorageRetrievalManager -> Renewing lease for file " + fileId + ".");
        
        // routes the message in the Network                
        virtualNode.routeMessage(fileId, fileInformationMessage);
        
        return requestNumber;
    }


    public void setFileIndexResponseReceived( FileFragmentIndexMessage message ) {
    	
    	if ( message.requestType == RequestType.STORAGE ) {

    		AccessBroker sourceBroker = 
    			requestSourceBrokerManager.getRequestSource(message.messageNumber);

    		FileInformation fileInformation = fileInformationMap.get(message.messageNumber);                
    		fileInformation.numberOfStoredReplicas++;

    		logger.debug("FileStorageRetrievalManager -> Received FileInformationIndex storage confirmation for file " + fileInformation.fileId 
    				+ " (" + fileInformation.numberOfStoredReplicas + " out of " + FileInformation.requiredReplicas + ")");
    		
    		/**
    		 * TODO: Need to treat the case where some servers do not respond
    		 */
    		if (fileInformation.numberOfStoredReplicas == FileInformation.requiredReplicas)
    			sourceBroker.setFileStorageRequestCompleted(message.messageNumber);                   
    		
    		if (fileInformation.numberOfStoredReplicas == FileInformation.numberOfReplicas)
    			fileInformationMap.remove(message.messageNumber);
    	}

    	else if ( message.requestType == RequestType.RETRIEVAL ){
            AccessBroker sourceBroker = 
                requestSourceBrokerManager.getRequestSource(message.messageNumber);
            
            if ( message.fileFragmentIndex != null ) {            	
        		logger.debug("FileStorageRetrievalManager -> Successfully retrieved FileInformationIndex for file " + message.fileId + ".");      
        		FileFragmentIndex ffi = message.fileFragmentIndex;
                sourceBroker.downloadFragments( message.messageNumber, ffi.adrAddressList, ffi.fragmentKeyList, ffi.fileSize, ffi.fragmentSizeList, ffi.neededFragments );
            }
            else {
        		logger.debug("FileStorageRetrievalManager -> Failed to retrieve FileInformationIndex for file " + message.fileId + ".");
                sourceBroker.setFileRetrievalRequestFailed(message.messageNumber);
            }
    	}
    	
    	else if ( message.requestType == RequestType.REMOVAL ){
            AccessBroker sourceBroker = 
                requestSourceBrokerManager.getRequestSource(message.messageNumber);
            
            if (message.isReplicaRequest == false) {
            	if ( message.fileFragmentIndex != null ) {            	
            		logger.debug("FileStorageRetrievalManager -> Successfully retrieved FileInformationIndex for file " + message.fileId + ".");      
            		FileFragmentIndex ffi = message.fileFragmentIndex;
            		sourceBroker.removeFragments( message.messageNumber, ffi.adrAddressList, ffi.fragmentKeyList );
            	}
            	else {
            		logger.debug("FileStorageRetrievalManager -> Failed to retrieve FileInformationIndex for file " + message.fileId + ".");
            		sourceBroker.removeFragments( message.messageNumber, new String[] {}, new byte[][]{} );                
            	}
            }
    	}
    	
    	else if ( message.requestType == RequestType.RENEWAL ){
            AccessBroker sourceBroker = 
                requestSourceBrokerManager.getRequestSource(message.messageNumber);
            
            if (message.isReplicaRequest == false) {
            	if ( message.fileFragmentIndex != null ) {            	
            		logger.debug("FileStorageRetrievalManager -> Successfully retrieved FileInformationIndex for file " + message.fileId + ".");      
            		FileFragmentIndex ffi = message.fileFragmentIndex;
            		sourceBroker.renewFragmentLeases( message.messageNumber, ffi.adrAddressList, ffi.fragmentKeyList, ffi.timeoutMinutes );
            	}
            	else {
            		logger.debug("FileStorageRetrievalManager -> Failed to retrieve FileInformationIndex for file " + message.fileId + ".");
            		sourceBroker.renewFragmentLeases( message.messageNumber, new String[] {}, new byte[][]{}, -1 );                
            	}
            }
    	}    		


    }
        
    public void addFragmentStorageLocation(int requestNumber, Id fragmentId, String adrAddress) {

        FileInformation fileInformation = fileInformationMap.get(new Integer(requestNumber));        
        if (fileInformation == null) return;
    
        try { fileInformation.setFragmentStorageLocation(fragmentId, adrAddress); } 
        catch (InvalidFragmentKeyException e) { e.printStackTrace(); }

        logger.debug("FileStorageRetrievalManager -> Received fragment storage location for file " + fileInformation.fileId 
        		+ " (" + fileInformation.nStorageAddresses + " out of " + fileInformation.adrAddressMap.size() + ")");

        if (fileInformation.hasAllStorageLocations() == true) {

        	logger.info("FileStorageRetrievalManager -> Received storage location of all fragments from file " + fileInformation.fileId + ".");

            FileFragmentIndex fileFragmentIndex = fileInformation.createFileFragmentIndex();

            AccessBroker sourceAccessBroker = requestSourceBrokerManager.getRequestSource(requestNumber);
            assert(sourceAccessBroker != null);
            assert(fileFragmentIndex != null);
            assert(fileFragmentIndex.adrAddressList != null);

        	StringBuffer addresses2 = new StringBuffer();
        	for (String address : fileFragmentIndex.adrAddressList) {
        		assert (address != null);
        		addresses2.append(address + ",");
        	}
        	logger.info( "Returning addresses: " + addresses2 );

            sourceAccessBroker.uploadFragments( requestNumber, fileFragmentIndex.adrAddressList );
        }
    }
}
