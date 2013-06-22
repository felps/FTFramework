package br.usp.ime.oppstore.broker;  

public class OppStoreBrokerTester {

    public static void main(String[] args) {
    	OppStoreBroker broker = new OppStoreBroker();
    	int status;
    	
    	status = broker.testPrint();    	
    	
    	String inFile1  = "TestBrokerReal";
    	String inFile2  = "Makefile.vars";
    	String outFile1 = "TestAdrOperations.out";
    		
    	String key1 = broker.storeFileW(inFile1, null);
    	String key2 = broker.storeFileW(inFile2, null);

    	if (key1 != null) {
    		status = broker.removeDataW(key1, null);
    		System.out.println("Removed data with status '" + status + "'.");
    	}
    	
    	if (key2 != null) {
    		status = broker.retrieveFileW(key2, outFile1);
    		System.out.println("Retrieved file with status '" + status + "'.");
    	}
    }
}
