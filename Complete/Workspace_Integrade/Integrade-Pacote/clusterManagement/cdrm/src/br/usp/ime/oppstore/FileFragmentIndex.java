package br.usp.ime.oppstore;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Contains data that will be stored in the Grid.
 * 
 * @author Raphael Y. de Camargo
 */
public class FileFragmentIndex implements Serializable {
    
    private static final long serialVersionUID = 2630054572891740773L;

    /**
     * When the number of needed fragments is 1, replication is used.
     */
    public int neededFragments; 
    
    public boolean isReplica;
    public byte[] fileKey;
    public byte[][] fragmentKeyList;
    public String[] adrAddressList;
    public int fileSize;
    public int[] fragmentSizeList;
    public int timeoutMinutes;
    
    public void removeFragment(int[] fragmentIndexList) {
        
        int newLenght = fragmentKeyList.length - fragmentIndexList.length;
        
        byte[][] newFragmentKeyList = new byte[newLenght][];
        String[] newAdrAddressList  = new String[newLenght];
        int[] newFragmentSizeList  = new int[newLenght];
        
        HashSet<Integer> removeIndexSet = new HashSet<Integer>();
        for (int removeIndex : fragmentIndexList)
            removeIndexSet.add(removeIndex);
        
        for(int iOld=0, iNew=0; iNew < newLenght; iOld++, iNew++) {
            if (removeIndexSet.contains(iOld)) iOld++;
            newFragmentKeyList[iNew]  = fragmentKeyList[iOld];            
            newAdrAddressList[iNew]   = adrAddressList[iOld];
            newFragmentSizeList[iNew] = fragmentSizeList[iOld];
        }

        fragmentKeyList  = newFragmentKeyList;
        adrAddressList   = newAdrAddressList;
        fragmentSizeList = newFragmentSizeList;

        assert (adrAddressList.length == fragmentSizeList.length);
        assert (adrAddressList.length == fragmentKeyList.length);
        //System.out.println("Removed " + fragmentIndexList.length + " null fragments. Current size is " + fragmentKeyList.length + ".");
    }
    
    /**
     * Update the fragment keys with new hash values.
     * Checks if the number of fragments is the same as the original ones and if the key sizes are the same.
     * 
     * @param fragmentHashList
     * @return 0 if update was succesful and -1 otherwise
     */
    public int updateFragmentHashes(byte[][] fragmentHashList) {
    	
    	if (fragmentHashList.length == this.fragmentKeyList.length) {
    		
    		for (int fragment=0; fragment < fragmentHashList.length; fragment++) {
    			if ( fragmentHashList[fragment].length == this.fragmentKeyList[fragment].length )
    				this.fragmentKeyList[fragment] = fragmentHashList[fragment];
    			else 
    				return -1;
    		}
    	}
    	else
    		return -1;
    	
    	return 0;
    }

    /**
     * Update the fragment keys with new hash values.
     * Checks if the number of fragments is the same as the original ones and if the key sizes are the same.
     * 
     * @param fileKey
     * @return 0 if update was succesful and -1 otherwise
     */
    public void updateFileKey(byte[] fileKey) {
    	   		
    	this.fileKey = fileKey;    	
    }

    public FileFragmentIndex(byte[] fileKey, byte[][] fragmentKeyList, String[] adrAddressList, int fileSize, int[] fragmentSizeList, int neededFragments, int timeoutMinutes) {
        this.fileKey = fileKey;
        this.fragmentKeyList = fragmentKeyList;
        this.adrAddressList  = adrAddressList;
        this.fileSize = fileSize;
        this.fragmentSizeList = fragmentSizeList;
        this.neededFragments = neededFragments;
        this.timeoutMinutes = timeoutMinutes;
        
        assert (adrAddressList.length == fragmentSizeList.length);
        assert (adrAddressList.length == fragmentKeyList.length);
    }
}
