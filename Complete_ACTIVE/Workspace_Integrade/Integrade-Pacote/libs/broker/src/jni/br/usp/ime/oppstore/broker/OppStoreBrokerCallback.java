package br.usp.ime.oppstore.broker;  

public interface OppStoreBrokerCallback {
	
    public void finishedOperation(int requestId, int status);

}
 