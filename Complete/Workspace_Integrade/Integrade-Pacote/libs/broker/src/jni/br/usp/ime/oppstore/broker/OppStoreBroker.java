package br.usp.ime.oppstore.broker;

public class OppStoreBroker {

    /**
     * Removes the file with key 'fileKey' from OppStore.
     * WARNING: The parameter 'callback' is currently ignored
     */
    public int removeDataW(String fileKey, OppStoreBrokerCallback callback) {
    	return this.removeDataW_(fileKey);
    }

    public String storeFileW(String filePath, OppStoreBrokerCallback callback) {
    	return this.storeFileW_(filePath);
    }

    // returns the file size or -1 if retrieval is unsuccessfull    
    public int retrieveFileW(String fileKey, String filePath) {
    	return this.retrieveFileW_(fileKey, filePath);
    }

    /**
     * 
     */
    public int testPrint () {
    	this.print();
    	return 0;
    }

	static {System.loadLibrary("broker");}	
	
    private native void print();

    private native int removeDataW_(String fileKey);

    // returns the stored data key or NULL if storage is unsuccessfull
    private native String storeFileW_(String filePath); 

    // returns the file size or -1 if retrieval is unsuccessfull    
    private native int retrieveFileW_(String fileKey, String filePath);
}
