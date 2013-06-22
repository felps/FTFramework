package br.usp.ime.oppstore.simulation.failure;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import br.usp.ime.oppstore.simulation.cdrm.CdrmEvent.CdrmState;

//@CDRM1
//numberOfAdrs=2 events=[J:0.0|D:9553.3]

public class CdrmSimulationData {

    int cdrmNumber;
    List<AdrSimulationData> adrDataList;
    
    List<CdrmState> cdrmStateList;
    List<Double> cdrmEventTimeList;
    
    Random random;
    
    CdrmSimulationData (Random random) {
        this.random = random;
    }
    
    void createCdrmSimulationData (int cdrmNumber, int numberOfAdrs, int minFreeSpace, int maxFreeSpace) {
        
    	cdrmEventTimeList = new LinkedList<Double>();
        cdrmStateList = new LinkedList<CdrmState>();        
        adrDataList = new LinkedList<AdrSimulationData>();
        
        for (int i=0; i<numberOfAdrs; i++) {
            AdrSimulationData adrData = new AdrSimulationData();
            adrData.adrNumber = i;
            adrData.freeStorageSpace = random.nextInt(maxFreeSpace) + minFreeSpace;
            adrDataList.add(adrData);
        }                               
    }
    
    public void setFailureExperimentsData (
            double meanDayUptime, double meanDayIdleTime, 
            double meanNightUptime, double meanNightIdleTime,
            int numberOfTimeZones) {

        for (AdrSimulationData adrData : adrDataList) {
            adrData.timeOffset = random.nextInt(numberOfTimeZones);
            
            adrData.meanDayUptime = meanDayUptime;
            adrData.meanNightUptime = meanNightUptime;

            adrData.meanDayIdleTime = meanDayIdleTime;
            adrData.meanNightIdleTime = meanNightIdleTime;
        }        
    }
}
