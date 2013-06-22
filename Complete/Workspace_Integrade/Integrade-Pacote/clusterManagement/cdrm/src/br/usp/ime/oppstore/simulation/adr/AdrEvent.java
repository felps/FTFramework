package br.usp.ime.oppstore.simulation.adr;

import br.usp.ime.oppstore.simulation.adr.ClusterAdrSimulator.AdrState;

public class AdrEvent {
    
    public double eventTime;
    public Adr adr;
    public AdrState newAdrState;
    
    public AdrEvent(double eventTime, Adr adr, AdrState newAdrState) {
        this.adr         = adr;
        this.newAdrState = newAdrState;
        this.eventTime   = eventTime;
    }    
}
