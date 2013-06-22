package br.usp.ime.oppstore.simulation.broker;

import java.util.Collection;
import java.util.HashMap;

import rice.pastry.NodeIdFactory;

public class BrokerRequestManager {

    class BrokerFileInformation {
        byte[] fileKey;
        byte[][] fragmentKeyList;
        int fileSize;
        int[] fragmentSizeList;
        int storeRequestNumber = -1;
        int neededFragments;
    }
    
    NodeIdFactory nodeIdFactory;
    HashMap <Integer, BrokerFileInformation> fileKeyMap;
    HashMap <Integer, BrokerFileInformation> requestNumberMap;
    
    public BrokerRequestManager(NodeIdFactory nodeIdFactory) {
        this.nodeIdFactory = nodeIdFactory;
        this.fileKeyMap = new HashMap <Integer, BrokerFileInformation>();
        this.requestNumberMap = new HashMap <Integer, BrokerFileInformation>(); 
    }
    
    public byte[] createFileKey() {
        return nodeIdFactory.generateNodeId().toByteArray();
    }
    
    public byte[][] createFragmentKeyList(int nFragments) {
        byte[][] fragmentKeyList = new byte[nFragments][];
        for (int i=0; i<nFragments; i++)
            fragmentKeyList[i] = nodeIdFactory.generateNodeId().toByteArray();

        return fragmentKeyList;
    }

    // -----------------------------------------------------------------------------------
    
    public void addFileInformation(byte[] fileKey, byte[][] fragmentKeyList, int fileSize, int[] fragmentSizeList, int neededFragments) {
        
        BrokerFileInformation fileInfo = new BrokerFileInformation();
        fileInfo.fileKey = fileKey;
        fileInfo.fragmentKeyList = fragmentKeyList;
        fileInfo.fileSize = fileSize;
        fileInfo.fragmentSizeList = fragmentSizeList;
        fileInfo.neededFragments = neededFragments;
        
        fileKeyMap.put(this.getIntRepresentation(fileKey), fileInfo);                       
    }

    public Collection<BrokerFileInformation> getFileInformationCollection() {
        return fileKeyMap.values();
    }
    
    public int getNumberOfFiles() {
        return fileKeyMap.size();
    }
    
    public BrokerFileInformation getBrokerFileInformation( byte[] fileKey ) { 
    	return fileKeyMap.get( this.getIntRepresentation( fileKey ) );
    }
    
    public void setRequestNumber (int requestNumber, byte[] fileKey, boolean isStore) {
    	BrokerFileInformation fileInformation = fileKeyMap.get( this.getIntRepresentation( fileKey ) );
    	if (fileInformation != null) {
    		if (isStore) fileInformation.storeRequestNumber = requestNumber;
    		requestNumberMap.put(requestNumber, fileInformation);
    	}
    }
    
    public BrokerFileInformation getBrokerFileInformation ( int requestNumber ) {
    	return requestNumberMap.get( requestNumber );
    }

    public void setFileStored(byte[] fileKey) {
        //BrokerFileInformation fileInfo = fileKeyMap.get(this.getIntRepresentation(fileKey));
        //fileInfo.fragmentKeyList = null;
        //fileInfo.fragmentSizeList = null;
    }


    // -----------------------------------------------------------------------------------
    
    public int getIntRepresentation(byte[] key) {
        int intRepresentation = 16777216 * key[0] + 65536 * key[1] + 256 * key[2] + 1 * key[3];
        return intRepresentation;
    }
    
}
