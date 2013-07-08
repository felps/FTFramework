package br.usp.ime.oppstore.simulation.adr;

import br.usp.ime.oppstore.simulation.adr.ClusterAdrSimulator.AdrState;

/**
 * The simulation is divided as a one week time spam
 * Simulation step are equivalent to a 2 hour period, with a total of 84 periods for a week
 * 
 * @author rcamargo
 *
 */
public class AdrFailureModel {
        
    private AdrState[] adrStateList;
    private int currentPos = 0;

    // Parameters used to determine the next state change
    double meanUptime;
    double meanIdleTime;
    
    private double meanDayUptime; 
    private double meanNightUptime;
    
    private double meanDayIdleTime; 
    private double meanNightIdleTime;
    
    private int timeOffset;
    

    public static final int nExperiments = 3;
    public static final int numberOfWeeks = 4;
    
    public static final int nCompleteWeekRounds = 84; // single week
    public static final int nWeekdaysRounds = 60; // single week    
    public static final int nDayRounds = 12; // single week

    public AdrFailureModel ( int timeOffset,
                double meanDayUptime, double meanDayIdleTime, 
                double meanNightUptime, double meanNightIdleTime) {

        //System.out.println(nExperiments);
        
        this.currentPos = 0;
        this.adrStateList = new AdrState[nExperiments * numberOfWeeks * nCompleteWeekRounds];
        
        this.meanDayIdleTime = meanDayIdleTime;
        this.meanDayUptime = meanDayUptime;

        this.meanNightIdleTime = meanNightIdleTime;
        this.meanNightUptime = meanNightUptime;

        this.timeOffset = timeOffset;
        
        this.meanUptime = (meanDayUptime*60 + meanNightUptime*108)/168;
        this.meanIdleTime = (meanDayIdleTime*60 + meanNightIdleTime*108)/168;
        
        updateAdrStateList();
        
    }
    
    public AdrState[] getAdrStateList() {
        //System.out.println("getting adrStateList...");
        return adrStateList;
    }
    
    public void updateAdrStateList() {
        
        //System.out.println("updating adrStateList...");
        
        generateIndependentFailureExperiment();
        generateCorrelatedFailureExperiment(0);
        generateCorrelatedFailureExperiment(timeOffset);
    }
        
    private void generateIndependentFailureExperiment () {
        
        for (int currentTime = 0; currentTime < numberOfWeeks*nCompleteWeekRounds; currentTime++)
            updateState(this.meanIdleTime, this.meanUptime);
        
    }

    
    private void generateCorrelatedFailureExperiment ( int timeOffset ) {
          
        double localMeanIdleTime, localMeanUptime;                
        
        for (int currentTime = timeOffset; currentTime < (numberOfWeeks*nCompleteWeekRounds) + timeOffset; currentTime++) {
            // daily weekdays
            if ( ( currentTime % nCompleteWeekRounds < nWeekdaysRounds ) && 
                 ( currentTime % nDayRounds  < nDayRounds/2 )  ) {
                
                localMeanIdleTime = meanDayIdleTime;
                localMeanUptime = meanDayUptime;
            }
            else {
            	
                localMeanIdleTime = meanNightIdleTime;
                localMeanUptime = meanNightUptime;                    
            }
            
            updateState(localMeanIdleTime, localMeanUptime);
        }
                
    }

    private void updateState (double localMeanIdleTime, double localMeanUptime) {
        
        double randomValue = Math.random();
        //System.out.print(randomValue + " ");
        if (randomValue < localMeanIdleTime)
            adrStateList[currentPos++] = AdrState.IDLE;
        else if (randomValue < localMeanUptime)
            adrStateList[currentPos++] = AdrState.OCCUPIED;
        else
            adrStateList[currentPos++] = AdrState.UNAVAILABLE;
    }
}
