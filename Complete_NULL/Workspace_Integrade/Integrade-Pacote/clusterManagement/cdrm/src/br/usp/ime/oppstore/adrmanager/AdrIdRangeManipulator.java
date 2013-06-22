package br.usp.ime.oppstore.adrmanager;

import java.util.Collection;

import rice.p2p.commonapi.Id;
import br.usp.ime.oppstore.adrmanager.AdrInformationStructure.AdrLiveness;
import br.usp.ime.virtualId.util.DistanceManipulator;

public class AdrIdRangeManipulator {
        
    protected final long maxLongId = 1152921504606846975L; // 2^60 - 1
    protected final long d = 1000000000000000L;

    protected Id leftmostId  = null;
    protected Id rightmostId = null;

    public AdrIdRangeManipulator() {
        
        leftmostId  = rice.pastry.Id.build (new int[]{0,0,0,0,0});
        rightmostId = rice.pastry.Id.build (new int[]{0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF});
                
    }
        
    boolean setAdrVirtualIds (Collection<AdrInformationStructure> adrInfoCollection) {
                        
    	if (adrInfoCollection.size() == 0) return false;    		

    	int numberOfAliveAdrs = 0;
		double meanCapacity = 0;
		int rightmostAdrId = 0;
		for (AdrInformationStructure adrInfo : adrInfoCollection)
			if (adrInfo.adrLiveness == AdrLiveness.ALIVE) {
				meanCapacity += adrInfo.currentCapacity;
				numberOfAliveAdrs++;
				rightmostAdrId = adrInfo.adrId;								
			}
		
		if (numberOfAliveAdrs == 0) return false;
		
		meanCapacity /= numberOfAliveAdrs;				
		long meanIdRange = DistanceManipulator.MAX_DISTANCE / numberOfAliveAdrs;

		/**
		 * Evaluates the new adaptiveIds and puts into the message.
		 * The first and last elements in the neighbors map maintains its adaptiveId.
		 */
		long longOffset = 0;
		int adrIndex = 0;
		for (AdrInformationStructure adrInfo : adrInfoCollection) {
			if (adrInfo.adrId == rightmostAdrId)
				adrInfo.virtualId = rightmostId;
			
			else if (adrInfo.adrLiveness == AdrLiveness.ALIVE) {
				longOffset += (long)( (adrInfo.currentCapacity / meanCapacity) * meanIdRange );
				adrInfo.virtualId = leftmostId.addToId( DistanceManipulator.convertLongtoDistance(longOffset) );				
			}
			adrIndex++;			
		}
		
		return true;
    }
    
}
