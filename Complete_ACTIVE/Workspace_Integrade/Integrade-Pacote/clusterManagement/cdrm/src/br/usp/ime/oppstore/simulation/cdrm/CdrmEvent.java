package br.usp.ime.oppstore.simulation.cdrm;

import br.usp.ime.oppstore.simulation.simnode.MachineCdrmAdrManager;


public class CdrmEvent {

    public enum CdrmState {JOIN, DEPART};
    
    private MachineCdrmAdrManager cdrmManager;
    private int cdrmIndex;
    private CdrmState newState;
    
    public CdrmEvent(double eventTime, MachineCdrmAdrManager cdrmManager, int cdrmIndex, CdrmState newState) {
        
        this.cdrmManager = cdrmManager;
        this.cdrmIndex = cdrmIndex;
        this.newState = newState;
    }

    public void dispatchEvent() {
        if (newState == CdrmState.DEPART)
            cdrmManager.destroyLocalCdrm(cdrmIndex);
    }
}
