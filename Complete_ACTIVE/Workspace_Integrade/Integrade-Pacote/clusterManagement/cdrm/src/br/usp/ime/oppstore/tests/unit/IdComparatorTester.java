package br.usp.ime.oppstore.tests.unit;

import rice.pastry.Id;
import br.usp.ime.virtualId.util.IdComparator;

public class IdComparatorTester {

    static public void testIdComparator () {
    	
        int tenth = Integer.MAX_VALUE/5;
        Id baseNodeId = Id.build(new int[]{0, 0, 0, 0, tenth * 2});
        
    	IdComparator testIdComparator = new IdComparator( baseNodeId );
    	
    	
    	Id nodeId3  = Id.build (new int[]{0, 0, 0, 0, tenth * 3});
    	Id nodeId3a = Id.build (new int[]{0, 0, 0, 0, tenth * 3});
    	Id nodeId4  = Id.build (new int[]{0, 0, 0, 0, tenth * 4});
    	Id nodeId9  = Id.build (new int[]{0, 0, 0, 0, tenth * 9});
    	Id nodeId1  = Id.build (new int[]{0, 0, 0, 0, tenth * 1});
    	
    	System.out.println(baseNodeId + " " + nodeId3 + " " + nodeId4 + " " + nodeId9 + " " + nodeId1 );
    	assert(testIdComparator.compare(baseNodeId, nodeId3) == -1);
    	assert(testIdComparator.compare(nodeId3, nodeId3a) == 0);    	    	
    	assert(testIdComparator.compare(nodeId3, nodeId4)  == -1);
    	assert(testIdComparator.compare(nodeId3, nodeId9)  == -1);
    	assert(testIdComparator.compare(nodeId3, nodeId1)  == -1);
    	assert(testIdComparator.compare(nodeId1, nodeId3)  == 1);
    	
    }

    public static void main (String[] args) {
    	testIdComparator(); 
    }
}
