package br.usp.ime.oppstore.cdrm;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import br.usp.ime.oppstore.corba.AccessBroker;

/**
 * Maintains a mapping from request numbers to AccessBrokers.
 * @author rcamargo
 *
 */
public class RequestSourceBrokerManager {
   
    private AtomicInteger nextRequestNumber;

    private HashMap <Integer, AccessBroker> requestSourceMap;
    
    public RequestSourceBrokerManager () {
        nextRequestNumber = new AtomicInteger(1);        
        requestSourceMap  = new HashMap <Integer, AccessBroker>();
    }
        
    public int getRequestNumber(AccessBroker source) {
        int requestNumber = this.nextRequestNumber.getAndIncrement();
        
        requestSourceMap.put(requestNumber, source);
        return requestNumber;
    }

    public AccessBroker getRequestSource (int requestNumber) {
        return requestSourceMap.get(requestNumber);
    }    
}
