package br.usp.ime.oppstore.simulation.adr;

import java.util.HashMap;

import br.usp.ime.oppstore.AdrAddress;
import br.usp.ime.oppstore.simulation.adr.ClusterAdrSimulator.AdrState;

public class Adr implements Comparable<Adr> {
    
    class HashKey {
        byte[] key;
        int hashCode;
        
        HashKey (byte[] key) {
            this.key = key;
            this.hashCode = 0;
            for (byte value : key)
                this.hashCode += (value + 127);
        }
        
        public int hashCode() { return hashCode; }
        
        public boolean equals(Object obj) {
            if (obj instanceof HashKey) {
                HashKey obj2 = (HashKey) obj;
                for (int i = 0; i < key.length; i++ )
                    if (this.key[i] != obj2.key[i])
                        return false;                
                return true;                 
            } 
            return false;
        }
        
    }
    
    HashMap <HashKey, byte[]> keyToDataMap;
    AdrAddress adrAddress;    
    int adrLongId;
    
    AdrFailureModel adrFailureModel;
        
    int totalStorageSpace;
    int freeStorageSpace;
    
    double meanUptime;
    double meanIdleness;
    
    Adr(AdrAddress adrAddress) {
        keyToDataMap = new HashMap <HashKey, byte[]>();
        this.adrAddress = adrAddress;
        
        this.freeStorageSpace  = 10000;
        this.totalStorageSpace = 10000;
        this.meanUptime       = 1;
        this.meanIdleness     = 1;        
    }
    
    public void setAdrParameters(int freeStorageSpace, int nExperiments, int timeOffSet, 
            double meanDayUptime, double meanNightUptime, double meanDayIdleness, double meanNightIdleness) {
        
        this.freeStorageSpace  = freeStorageSpace;
        this.totalStorageSpace = freeStorageSpace;
        adrFailureModel = new AdrFailureModel(timeOffSet, 
                meanDayUptime, meanDayIdleness, meanNightUptime, meanNightIdleness);
        
        this.meanIdleness = adrFailureModel.meanIdleTime;
        this.meanUptime   = adrFailureModel.meanUptime;
    }
    
    public long getFreeStorageSpace() {
        return freeStorageSpace;
    }
    
    public double getMeanIdleness() {
		return meanIdleness;
	}
    
    public double getMeanUptime() {
		return meanUptime;
	}
    
    int getNumberOfStoredFragments() {
        return keyToDataMap.size();
    }
    
    long storeData (byte[] dataId, byte[] data, long dataSize) {
        if (freeStorageSpace - dataSize < 0) return -1;
            
        freeStorageSpace -= dataSize;
        keyToDataMap.put(new HashKey(dataId), data);
        return freeStorageSpace;
    }

    byte[] getData (byte[] dataId) {
        return keyToDataMap.get(new HashKey(dataId));        
    }
    
    void updateAdrStateList() {
        this.adrFailureModel.updateAdrStateList();
    }
    
    AdrState[] getAdrStateList() {
        return adrFailureModel.getAdrStateList();
    }

    public int compareTo(Adr adr) {
        if (this.adrLongId < adr.adrLongId)
            return -1;
        else if (this.adrLongId > adr.adrLongId)
            return 1;
        else
            return 0;
    }

}
