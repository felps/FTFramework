package br.usp.ime.oppstore.tests.unit;

import java.util.HashMap;
import java.util.Map.Entry;

import br.usp.ime.oppstore.AdrAddress;
import br.usp.ime.oppstore.adrmanager.AdrManagerImpl;
import rice.environment.Environment;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.IdFactory;
import rice.pastry.NodeIdFactory;
import rice.pastry.commonapi.PastryIdFactory;
import rice.pastry.standard.RandomNodeIdFactory;

public class AdrManagerTester {
        
    
    public static void main (String[] args) {

        Environment env = new Environment();
        NodeIdFactory nodeIdFactory = new RandomNodeIdFactory(env);
        IdFactory idFactory = new PastryIdFactory(env);
        AdrManagerImpl adrManager = null; //new AdrManager(new AdrIdRangeManipulator(env));
        HashMap <Id, String> adrAddressMap = new HashMap <Id, String>();
        
        for (byte i=10; i <= 100; i += 10) {
            Id tempId = nodeIdFactory.generateNodeId();            
            byte[] tempKey = tempId.toByteArray();            
            tempKey[19] = i;
            
            Id nodeId = idFactory.buildId(tempKey);            
            
            adrAddressMap.put(nodeId, String.valueOf(i));
        }
                
        // Add some nodeIds
        System.out.println("Registering nodes");
        for (Entry<Id, String> entry: adrAddressMap.entrySet())
            adrManager.registerAdr(entry.getValue(), 100, 0.7, 0.5);
        
        // Try to recover some nodes
        System.out.println("Recovering nodeIds");        
        for (Entry<Id, String> entry: adrAddressMap.entrySet()) {
            String adrAddress = adrManager.getAdrAddress(10, nodeIdFactory.generateNodeId(), 10, null);            
            assert adrAddress.compareTo(entry.getValue()) == 0;
        }

        // Try to recover some near nodes
        System.out.println("Recovering neighbor nodeIds");
        byte[] fileIds     = {  1 , 17 , 30 , 37 , 52 , 63 , 71 , 77 , 90 , 122 ,-120 };
        String[] addresses = {"10","20","30","40","50","60","70","80","90","100","100"};
        for (byte i=0; i < fileIds.length; i++ ) {
            Id tempId = nodeIdFactory.generateNodeId();            
            byte[] tempKey = tempId.toByteArray();                        
            tempKey[19] = fileIds[i];
            
            Id fileId = idFactory.buildId(tempKey);            
            String adrAddress = adrManager.getAdrAddress(10, nodeIdFactory.generateNodeId(), 10, null);
            assert adrAddress.compareTo(addresses[i]) == 0; 
        }
        
        // Register repeated Adrs
        for (Entry<Id, String> entry: adrAddressMap.entrySet()) {
            adrManager.registerAdr(entry.getValue(), 100, 0.7, 0.5);
            String adrAddress = adrManager.getAdrAddress(10, nodeIdFactory.generateNodeId(), 10, null);            
            assert adrAddress.compareTo(entry.getValue()) == 0;            
        }
        
        // Register again with a different adrAddress
        //for (Entry<Id, AdrAddress> entry: adrAddressMap.entrySet()) {
        //    AdrAddress tempAdrAddress = new AdrAddress();
        //    tempAdrAddress.address = "1";
        //    adrManager.registerAdr(entry.getKey(), tempAdrAddress);
        //    AdrAddress adrAddress = adrManager.getAdrAddress(entry.getKey());
        //    assert adrAddress.address.compareTo(entry.getValue().address) == 0;            
        //}
        
        env.destroy();
        
        // Tests if assertions are enabled  
        boolean assertsEnabled = false;
        assert assertsEnabled = true;  // Intentional side-effect!!!
        if (assertsEnabled == true)
            System.out.println("AdrManager tests finished succesfully!");
        else
            System.out.println("Couldn't perform tests. Assertions not enabled...");

    }

}
