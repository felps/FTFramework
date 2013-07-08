package br.usp.ime.virtualId.util;

import rice.p2p.commonapi.Id;
import rice.pastry.NodeIdFactory;
import rice.pastry.Id.Distance;

public class DistanceManipulator {

    public static long MAX_DISTANCE = 72057594037927936L;
    public static long DIVISOR = 72057594037927L;
    private static long[] multipliers = {1, 256, 65536, 16777216, 4294967296L, 1099511627776L, 281474976710656L};
    
    public static void testDistanceLongConverters (NodeIdFactory nodeIdFactory) {
        
        byte[] distanceArray1 = new byte[20];
        byte[] distanceArray2 = new byte[20];
        
        for (int i=0; i < 1000; i++) {
            Id id1 = nodeIdFactory.generateNodeId();
            Id id2 = nodeIdFactory.generateNodeId();            
            Distance dist1 = (Distance) id1.distanceFromId(id2);
            long longDist = convertDistanceToLong(dist1);
            Distance dist2 = convertLongtoDistance(longDist);
            dist1.blit(distanceArray1);
            dist2.blit(distanceArray2);
            for (int k=19; k >= 13; k--)
                assert (distanceArray1[k] == distanceArray2[k]);
        }
    }

    public static long convertDistanceToLong(Distance distance) {
        
        byte[] distanceArray = new byte[20]; 
        distance.blit(distanceArray);
                
        long longId = 0;
        long nextByte;
        int iOffset = distanceArray.length - multipliers.length;
        for (int i=0; i < multipliers.length; i++) {
            nextByte = distanceArray[iOffset + i];
            if (nextByte < 0) nextByte += 256;          
            longId += nextByte * multipliers[i];
        }        
        
        return longId;
    }
    
    public static Distance convertLongtoDistance (long longDistance) {
        
        byte[] distanceArray = new byte[20];
        int iOffset = distanceArray.length - multipliers.length;
        for (int i=0; i < iOffset; i++)
            distanceArray[i] = 0;
        for (int i = multipliers.length-1; i >= 0 ; i--) {
            distanceArray[iOffset + i] = (byte)(longDistance / multipliers[i]);
            longDistance = longDistance % multipliers[i];
        }
        
        return new Distance(distanceArray);
    }

}
