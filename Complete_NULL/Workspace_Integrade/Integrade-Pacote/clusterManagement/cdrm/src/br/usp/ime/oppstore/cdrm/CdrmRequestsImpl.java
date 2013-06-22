package br.usp.ime.oppstore.cdrm;

import java.util.Vector;

import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.iiop.IIOPAddress;
import org.jacorb.orb.iiop.IIOPProfile;
import org.omg.CORBA.ORB;

import rice.environment.Environment;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.IdFactory;
import rice.pastry.commonapi.PastryIdFactory;
import br.usp.ime.oppstore.corba.AccessBroker;
import br.usp.ime.oppstore.corba.AccessBrokerHelper;
import br.usp.ime.oppstore.corba.CdrmRequestsPOA;
import br.usp.ime.oppstore.message.FileFragmentIndexMessage.RequestType;

public class CdrmRequestsImpl extends CdrmRequestsPOA {
    
	private ORB orb;
    private FileStorageRetrievalManager fileStorageManager;
    protected IdFactory idFactory;
    //private Logger logger;
       
    public CdrmRequestsImpl (FileStorageRetrievalManager fileStorageManager, Environment environment, ORB orb) {
        this.fileStorageManager = fileStorageManager;   
        this.idFactory = new PastryIdFactory(environment);
        this.orb = orb;
        //this.logger = Logger.getLogger("request.CdrmRequestImpl");
    }        
    
    /**
     * From CDRM IDL interface 
     * Used to notify the CDRM that the fragments from file 'fileId' have been stored.
     * 
     * @param fileKey A byte sequence representing the file key
     * @param fragmentKeyList List of keys from fragments that couldn't be stored. 
     */
    public void setFragmentStorageFinished(int requestNumber, int[] notStoredFragmentIndexList, byte[][] fragmentHashList, byte[] finalFileKey) {
                               
    	fileStorageManager.storeFileInformationIndex(requestNumber, notStoredFragmentIndexList, fragmentHashList, finalFileKey);
    }
    
    /**
     * From CDRM IDL interface 
     * 
     * @param fragmentKeyArray A list of byte sequences representing keys 
     */
    public int requestFileStorage(byte[] fileKey, byte[][] fragmentKeyArray, int fileSize, int[] fragmentSizeArray, int neededFragments,
    		String accessBrokerIor, int timeoutMinutes, boolean storeGlobal) {

    	AccessBroker accessBroker = AccessBrokerHelper.narrow( orb.string_to_object( accessBrokerIor ) );    	    	
    	org.apache.avalon.framework.logger.Logger iorLogger = 
    		((org.jacorb.orb.ORB)orb).getConfiguration().getNamedLogger("jacorb.print_ior");    	
    	ParsedIOR pior = new ParsedIOR( accessBrokerIor, orb, iorLogger );
    	IIOPProfile profile = (IIOPProfile)pior.getProfiles().get(0);
    	IIOPAddress iiopAddress = (IIOPAddress)profile.getAddress(); 
    	String brokerIpAddress = iiopAddress.getIP();
    	//String brokerHostName  = iiopAddress.getOriginalHost();    	    	
    	    	
        // generates Ids from the byte sequences
        Id fileId = idFactory.buildId(fileKey);
        Vector<Id> fragmentIdList = new Vector<Id>();
        for (byte[] key : fragmentKeyArray)
            fragmentIdList.add(idFactory.buildId(key));        
        
        int requestId = fileStorageManager.getRequestNumber(accessBroker);
        fileStorageManager.requestFragmentStorageLocations(
        		requestId, fileSize, fragmentSizeArray, fileId, fragmentIdList, neededFragments, timeoutMinutes, storeGlobal, brokerIpAddress);
                
        return requestId;
    }
    
    /**
     * From CDRM IDL interface 
     * 
     * @param fragmentKeyList A list of byte sequences representing keys 
     */
    public int requestFileRetrieval(byte[] fileKey, String accessBrokerIor) {

    	AccessBroker source = AccessBrokerHelper.narrow( orb.string_to_object( accessBrokerIor ) );

        Id fileId = idFactory.buildId(fileKey);
        
        return fileStorageManager.retrieveFileInformationIndex(fileId, source, RequestType.RETRIEVAL);
    }

    /**
     * From CDRM IDL interface 
     * 
     * @param fileKey The key of the file that will be removed 
     */
    public int requestFileRemoval(byte[] fileKey, String accessBrokerIor) {

    	AccessBroker source = AccessBrokerHelper.narrow( orb.string_to_object( accessBrokerIor ) );
        Id fileId = idFactory.buildId(fileKey);
        
        return fileStorageManager.retrieveFileInformationIndex(fileId, source, RequestType.REMOVAL);
    }
    
    /**
     * From CDRM IDL interface 
     * 
     * @param fileKey The key of the file that will be removed 
     */
    public int requestFileLeaseRenewal(byte[] fileKey, String accessBrokerIor, int timeout) {

    	AccessBroker source = AccessBrokerHelper.narrow( orb.string_to_object( accessBrokerIor ) );
        Id fileId = idFactory.buildId(fileKey);
        
        return fileStorageManager.renewFileLease(fileId, source, timeout);
    }


//    public void setFragmentsRemoved(int requestNumber, byte[] fileKey, int[] notRemovedFragmentIndexList) {
//        
//    	fileStorageManager.removeFileInformationIndex(requestNumber, idFactory.buildId(fileKey), notRemovedFragmentIndexList);
//    	
//    	/**
//    	 * 	- When all fragments are removed, can successfully remove the FFI
//    	 * 		- FFI is marked as removed, but kept in the CDRM with a lease for later removal (later)
//    	 * 			- Used to later remove fragments that could not be previously removed  (later)
//    	 */
//    }

}
