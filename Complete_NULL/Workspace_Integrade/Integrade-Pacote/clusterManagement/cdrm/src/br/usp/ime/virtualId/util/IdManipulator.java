package br.usp.ime.virtualId.util;

import rice.environment.Environment;
import rice.p2p.commonapi.Id;
import rice.pastry.NodeIdFactory;
import rice.pastry.commonapi.PastryIdFactory;

public class IdManipulator {

    public static long MAX_ID     = 72057594037927936L;
    public static long DIVISOR = 72057594037927L;
    
    private long[] multipliers = {1, 256, 65536, 16777216, 4294967296L, 1099511627776L, 281474976710656L};    
    private PastryIdFactory idFactory;
    
    public IdManipulator(Environment env) {
        idFactory = new PastryIdFactory(env);
    }
    
    public void testIdLongConverters (NodeIdFactory nodeIdFactory) {
        
        byte[] idArray1 = new byte[20];
        byte[] idArray2 = new byte[20];
        
        System.out.println("Testing IdManiplator...");
        
        for (int i=0; i < 1000; i++) {
            Id id1 = nodeIdFactory.generateNodeId();
            long longDist = convertIdToLong(id1);
            Id id2 = convertLongToId(longDist);

            id1.toByteArray(idArray1, 0);
            id2.toByteArray(idArray2, 0);            
            for (int k=19; k >= 13; k--) 
                assert (idArray1[k] == idArray2[k]);
        }
        
    }

    public long convertIdToLong(Id id) {
        
        byte[] idArray = id.toByteArray();
                
        long longId = 0;
        long nextByte;
        int iOffset = idArray.length - this.multipliers.length;
        for (int i=0; i < this.multipliers.length; i++) {
            nextByte = idArray[iOffset + i];
            if (nextByte < 0) nextByte += 256;          
            longId += nextByte * multipliers[i];
        }        
        
        return longId;
    }
    
    public Id convertLongToId (long longId) {
        
        byte[] idArray = new byte[20];
        int iOffset = idArray.length - this.multipliers.length;
        for (int i=0; i < iOffset; i++)
            idArray[i] = 0;
        for (int i = multipliers.length-1; i >= 0 ; i--) {
            idArray[iOffset + i] = (byte)(longId / multipliers[i]);
            longId = longId % multipliers[i];
        }
        
        return idFactory.buildId(idArray);
    }

}
