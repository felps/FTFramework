package br.usp.ime.oppstore.tests.adaptive;

import java.util.List;

import rice.p2p.commonapi.Id;
import br.usp.ime.oppstore.FileFragmentIndex;
import br.usp.ime.oppstore.cdrm.CdrmApp.OpStoreIdProtocol;

public class CdrmTestInformation {

    public long longIdRange;
    //public Id previousAdaptiveNodeId;    
    public Id adaptiveNodeId;
    //public Id nextAdaptiveNodeId;
    public Id originalNodeId;
    public double capacity;
    public double lastUpdatedCapacity;
    public OpStoreIdProtocol protocol;
    
    public List <FileFragmentIndex> fileInformationList;
    
}
