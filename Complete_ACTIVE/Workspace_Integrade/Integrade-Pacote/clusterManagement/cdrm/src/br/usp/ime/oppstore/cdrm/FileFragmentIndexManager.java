package br.usp.ime.oppstore.cdrm;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import br.usp.ime.oppstore.FileFragmentIndex;
import br.usp.ime.oppstore.message.CdrmReplicaInfoMessage;
import br.usp.ime.oppstore.message.FileFragmentIndexMessage;
import br.usp.ime.oppstore.message.CdrmReplicaInfoMessage.CdrmInfoSide;
import br.usp.ime.oppstore.message.CdrmReplicaInfoMessage.CdrmInfoType;
import br.usp.ime.oppstore.message.FileFragmentIndexMessage.RequestType;
import br.usp.ime.virtualId.VirtualNode;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.pastry.IdRange;
import rice.pastry.leafset.LeafSet;

public class FileFragmentIndexManager {
	
    /**
     * Maintains the FileFragmentIndex for a given fileId 
     * nodeId x fileInfo
     */
    private HashMap <Id, FileFragmentIndex> ffiMap;

	private VirtualNode virtualNode;
	
	private FileFragmentIndexLivenessMonitor livenessMonitor;
	
	private Logger logger;
	
	public FileFragmentIndexManager( VirtualNode virtualNode ) {
		this.ffiMap = new HashMap<Id, FileFragmentIndex>();
		this.virtualNode = virtualNode;
		this.logger = Logger.getLogger("cdrm." + virtualNode.getNode().getId().toString().substring(0, 9) + ">");
		this.livenessMonitor = new FileFragmentIndexLivenessMonitor(this, logger);
		this.livenessMonitor.start();
	}
	
	public Map<Id, FileFragmentIndex> getStoredFfiMap() {
		return ffiMap;
	}
	
	public void createDepartureReplicaInfoMessage( ) {
		
		LeafSet leafSet = virtualNode.getNode().getLeafSet();
				
		CdrmReplicaInfoMessage infoMessage = 
			new CdrmReplicaInfoMessage(virtualNode.getNode().getLocalNodeHandle(), CdrmInfoType.STORE_DEPART, CdrmInfoSide.LEFT, 0);
		
		IdRange idRange = leafSet.range(virtualNode.getNode().getLocalHandle(), 0);
		for (Id ffiId : ffiMap.keySet())
			if ( idRange.containsId( ffiId ) )
				infoMessage.ffiMap.put( ffiId, ffiMap.get( ffiId ) );
		
		NodeHandle leftNode  = leafSet.get(-1);
		if (leftNode != null) {
			logger.debug("Forwarding message from " + virtualNode.getNode().getId() + " to " + leftNode.getId());
			virtualNode.sendDirectMessage(leftNode, infoMessage);
		}
	
		NodeHandle rightNode = leafSet.get(1);
		if (rightNode != null) {
			logger.debug("Forwarding message from " + virtualNode.getNode().getId() + " to " + rightNode.getId());		
			virtualNode.sendDirectMessage(rightNode, infoMessage);
		}
					
	}
	
	public void processCdrmReplicaInfoMessage( CdrmReplicaInfoMessage message ) {
		
		if( message.isResponse == false ) {

			if ( message.cdrmInfoType == CdrmInfoType.STORE_JOIN ) {
				
				LeafSet leafSet = virtualNode.getNode().getLeafSet();
				IdRange idRange = leafSet.range((rice.pastry.NodeHandle)message.sourceHandle, 0);
				for (Id ffiId : ffiMap.keySet())
					if ( idRange.containsId( ffiId ) )
						message.ffiMap.put( ffiId, ffiMap.get( ffiId ) );

				NodeHandle forwardHandle = null;
				if (message.cdrmInfoSide == CdrmInfoSide.LEFT)
					forwardHandle = leafSet.get(-1);
				else if (message.cdrmInfoSide == CdrmInfoSide.RIGHT)
					forwardHandle = leafSet.get(1);
				if ( forwardHandle != null && forwardHandle.getId().equals( message.sourceHandle ) == false ) {
					logger.debug("Forwarding message to " + forwardHandle.getId());
					CdrmReplicaInfoMessage forwardMessage = new CdrmReplicaInfoMessage( message );
					forwardMessage.cdrmInfoType = CdrmInfoType.REMOVAL_JOIN;
					forwardMessage.isResponse = true;
					virtualNode.sendDirectMessage(forwardHandle, forwardMessage);
				}				

				NodeHandle tempHandle = message.sourceHandle;
				message.sourceHandle = virtualNode.getNode().getLocalNodeHandle();
				message.isResponse = true;
				virtualNode.sendDirectMessage(tempHandle, message);				
				
			}
			
			if( message.cdrmInfoType == CdrmInfoType.STORE_DEPART ) {
				
				logger.debug("Received message at " + virtualNode.getNode().getId() + " from " + message.sourceHandle.getId());
				for ( Entry<Id,FileFragmentIndex> ffi : message.ffiMap.entrySet() )
					ffiMap.put( ffi.getKey(), ffi.getValue() );
			}
				
		}
		else {
			if ( message.cdrmInfoType == CdrmInfoType.STORE_JOIN ) {
				//System.out.println("Adding FFIs at " + virtualNode.getNode().getId());
				for ( Entry<Id,FileFragmentIndex> ffi : message.ffiMap.entrySet() )
					ffiMap.put( ffi.getKey(), ffi.getValue() );
			}
			else if ( message.cdrmInfoType == CdrmInfoType.REMOVAL_JOIN ) {
				//System.out.println("Removing FFIs at " + virtualNode.getNode().getId());				
				for ( Entry<Id,FileFragmentIndex> ffi : message.ffiMap.entrySet() )
					ffiMap.remove( ffi.getKey() );
			}			
		}
		
	}
	
    /**
     * @param ffiMessage
     */
    public void processFileIndexMessage(FileFragmentIndexMessage ffiMessage) {

    	/**
    	 * FileFragmentIndex storage request
    	 */
        if (ffiMessage.requestType == RequestType.STORAGE) {

        	forwardRequestToReplicas(ffiMessage);

        	/**
        	 * stores data on local repository
        	 */
        	ffiMessage.fileFragmentIndex.isReplica = ffiMessage.isReplicaRequest;
        	ffiMap.put(ffiMessage.fileId, ffiMessage.fileFragmentIndex);
        	livenessMonitor.addLivenessTimeout(ffiMessage.fileFragmentIndex.timeoutMinutes, ffiMessage.fileId);

        	/**
        	 * returns FileFragmentIndex to sourceCdrm
        	 */
        	ffiMessage.isResponse = true;
        	virtualNode.sendDirectMessage(ffiMessage.sourceHandle, ffiMessage);                      
        }

    	/**
    	 * FileFragmentIndex retrieval request
    	 */
        else if (ffiMessage.requestType == RequestType.RETRIEVAL){            
        	
        	ffiMessage.fileFragmentIndex = ffiMap.get(ffiMessage.fileId);
        	ffiMessage.isResponse = true;
        	
        	/**
        	 * TODO: forward the message to the replicas
        	 */
        	
        	logger.debug("Returning FFI for requested file " + ffiMessage.fileId + " for ffiSet of size " + ffiMap.size() );
        	if (ffiMessage.fileFragmentIndex == null)
        		logger.debug("FFI for requested file " + ffiMessage.fileId + " at " + virtualNode.getNode().getId() + " not found!");
        	
        	// Sends the address of the ADR to the source CDRM
        	virtualNode.sendDirectMessage(ffiMessage.sourceHandle, ffiMessage);           
        }

    	/**
    	 * FileFragmentIndex retrieval request
    	 */
        else if (ffiMessage.requestType == RequestType.RENEWAL){            
        	
        	ffiMessage.fileFragmentIndex = ffiMap.get(ffiMessage.fileId);        
        	livenessMonitor.renewLivenessTimeout(ffiMessage.newTimeout, ffiMessage.fileId);
        	forwardRequestToReplicas(ffiMessage);
        	      	        	
        	logger.debug("Renewing FFI for requested file " + ffiMessage.fileId + " for ffiSet of size " + ffiMap.size() );
        	ffiMessage.isResponse = true;
        	if (ffiMessage.fileFragmentIndex == null)
        		logger.debug("FFI for requested file " + ffiMessage.fileId + " at " + virtualNode.getNode().getId() + " not found!");
        	
        	// Sends the address of the ADR to the source CDRM
        	if (ffiMessage.isReplicaRequest == false)
        		virtualNode.sendDirectMessage(ffiMessage.sourceHandle, ffiMessage);           
        }

    	/**
    	 * FileFragmentIndex removal request
    	 */
        else if (ffiMessage.requestType == RequestType.REMOVAL){         
        	
        	ffiMessage.fileFragmentIndex = ffiMap.remove(ffiMessage.fileId);
        	livenessMonitor.removeLivenessTimeout(ffiMessage.fileId);
        	ffiMessage.isResponse = true;
        	        	
        	// Forwards the message to the CDRM containing replicas of the FFI
        	forwardRequestToReplicas(ffiMessage);        
        	
        	logger.debug("Returning FFI for removed file " + ffiMessage.fileId + " for ffiSet of size " + ffiMap.size() );
        	if (ffiMessage.fileFragmentIndex == null)
        		logger.debug("FFI for removed file " + ffiMessage.fileId + " at " + virtualNode.getNode().getId() + " not found!");
        	
        	//Sends the address of the ADR to the source CDRM
        	virtualNode.sendDirectMessage(ffiMessage.sourceHandle, ffiMessage);
        	
        }

    }

	private void forwardRequestToReplicas(FileFragmentIndexMessage ffiMessage) {
		
		/**
		 * Forwards replica storage request to the closest neighbor nodes
		 */
		if (ffiMessage.isReplicaRequest == false) {   

			LeafSet leafSet = virtualNode.getNode().getLeafSet();
			for (int handleIndex = 1; handleIndex <= ffiMessage.numberOfReplicas/2; handleIndex++) {

				{
					FileFragmentIndexMessage replicaFFIMessage = new FileFragmentIndexMessage(ffiMessage);
					replicaFFIMessage.isReplicaRequest = true;

					NodeHandle targetHandle = leafSet.get(-handleIndex);
					if (targetHandle != null)
						virtualNode.sendDirectMessage( targetHandle, replicaFFIMessage );
					else {
						replicaFFIMessage.isResponse = true;
						virtualNode.sendDirectMessage(replicaFFIMessage.sourceHandle, replicaFFIMessage);
					}
				}
				
				
				{
					FileFragmentIndexMessage replicaFFIMessage = new FileFragmentIndexMessage(ffiMessage);
					replicaFFIMessage.isReplicaRequest = true;

					NodeHandle targetHandle = leafSet.get(handleIndex);
					if (targetHandle != null)
						virtualNode.sendDirectMessage( targetHandle, replicaFFIMessage );
					else {
						replicaFFIMessage.isResponse = true;
						virtualNode.sendDirectMessage(replicaFFIMessage.sourceHandle, replicaFFIMessage);
					}
				}

			}

		}
	}

}
