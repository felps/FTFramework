package br.usp.ime.oppstore.simulation.failure;

public class AdrSimulationData {

    int adrNumber;
    long freeStorageSpace;
    
    int timeOffset;
    
    // Parameters used to determine the next state change
    double meanDayUptime; 
    double meanNightUptime;
    
    double meanDayIdleTime; 
    double meanNightIdleTime;


}

