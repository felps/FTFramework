package br.usp.ime.oppstore.adrmanager;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import rice.p2p.commonapi.Id;

import br.usp.ime.oppstore.AdrAddress;

class AdrInformationStructureCapacityComparator implements Comparator<AdrInformationStructure> {

    public int compare(AdrInformationStructure adr1, AdrInformationStructure adr2) {
        if (adr1.currentCapacity <= adr2.currentCapacity )
            return -1;
        else
            return 1;
    }        
}

public class AdrInformationStructure {

	public enum AdrLiveness {ALIVE, UNRESPONSIVE, DEPARTED};
	
	AdrLiveness adrLiveness = AdrLiveness.ALIVE;
    String adrAddress;
    Id virtualId;
    int adrId;
    
    List<Id> fragmentRemovalList = new LinkedList<Id>();
    
    long lastUpdateTime;
    
    long initialAvailableSpace;
    long freeStorageSpace;
    long freeUnreservedSpace;
    double meanUptime;
    double meanIdleness;    
    
    double lastUpdatedCapacity;      
    double currentCapacity;
        
//    public int compareTo(AdrInformationStructure adrInfo) {
//        if (this.adrLongId < adrInfo.adrLongId)
//            return -1;
//        else if (this.adrLongId > adrInfo.adrLongId)
//            return 1;
//        else
//            return 0;
//    }

//    public int hashCode() {
//        return (int)adrLongId;
//    }
}
