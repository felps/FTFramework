package br.usp.ime.oppstore;

import java.util.HashMap;
import java.util.Vector;

import rice.p2p.commonapi.Id;

/**
 * Used to collect data and generate a FileInformationStructure
 * 
 * @author Raphael Y. de Camargo
 */

// TODO: FileInformation should not be kept forever
public class FileInformation {

    static public final int numberOfReplicas = 3;
    static public final int requiredReplicas = 3;

    public Id fileId;
    //public Id tempFileId;
    public HashMap<Id, String> adrAddressMap;
    public Vector<Id> fragmentIdList;
    public int fileSize;
    public int[] fragmentSizeList;
    int neededFragments;
    public int timeoutMinutes;
    
    public  int nStorageAddresses;
    public FileFragmentIndex fileInformationStructure;
    public int numberOfStoredReplicas;
    
    public FileInformation (Id fileId, Vector<Id> fragmentIdList, int fileSize, int[] fragmentSizeList, int neededFragments, int timeoutMinutes) {
        this.nStorageAddresses = 0;
        this.numberOfStoredReplicas = 0;
        this.fileId = fileId;
        this.fileInformationStructure = null;
        this.fragmentIdList = fragmentIdList;
        this.fileSize = fileSize;
        this.fragmentSizeList = fragmentSizeList;
        this.neededFragments = neededFragments;
        this.timeoutMinutes = timeoutMinutes;
        
        // Starts the HashMap with null values to later check fragment key values
        adrAddressMap = new HashMap<Id, String>();
        for (Id key : fragmentIdList)
            this.adrAddressMap.put(key, null);
    }
    
    public void setFragmentStorageLocation(Id fragmentId, String adrAddress)
    throws InvalidFragmentKeyException {

        if (adrAddressMap.containsKey(fragmentId) == false)
            throw new InvalidFragmentKeyException();
            
        adrAddressMap.put(fragmentId, adrAddress);
        nStorageAddresses++;
    }    
    
    public FileFragmentIndex createFileFragmentIndex() {

        if (this.fileInformationStructure == null) {
            
            byte[][] fragmentKeyList = new byte[adrAddressMap.size()][];
            String[] adrAddressList = new String[adrAddressMap.size()];            
            
            for (int i = 0; i < adrAddressMap.size(); i++) {
                Id fragmentId = fragmentIdList.get(i);
                fragmentKeyList[i] = fragmentId.toByteArray();
                adrAddressList[i]  = adrAddressMap.get(fragmentId);
            }
            
            this.fileInformationStructure = 
                new FileFragmentIndex(fileId.toByteArray(), fragmentKeyList, adrAddressList, fileSize, fragmentSizeList, neededFragments, timeoutMinutes);
        }
        
        return this.fileInformationStructure;
    }
    
    public void removeFragmentLocation(Id fragmentId) {
        adrAddressMap.remove(fragmentId);
    }

    public boolean hasAllStorageLocations() {
        if (nStorageAddresses == adrAddressMap.size())
            return true;
        else
            return false;
    }
}
