package br.usp.ime.oppstore.simulation.failure;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import br.usp.ime.oppstore.simulation.cdrm.CdrmEvent.CdrmState;

/**
 * Creates cluster description files for using on simulations.
 * These files describe, for each cluster, the number of ADRs and free space and availability for each ADR.
 *
 * ------------------------------------------------------------------------
 * @SIMULATION
 * numberOfCdrms=5 numberOfBrokers=5 numberOfExperiments=1
 *  
 * @CDRM1 
 * numberOfAdrs=2 events=[J:0.0|D:9553.3]
 * @ADR1
 * freeSpace=3523 timeOffset=0 meanDayUptime=0.1 meanNightUptime=0.1 meanDayIdleness=0.1 meanNightIdleness=0.1
 * @ADR2
 * freeSpace=3523 timeOffset=0 meanDayUptime=0.1 meanNightUptime=0.1 meanDayIdleness=0.1 meanNightIdleness=0.1
 * ------------------------------------------------------------------------
 *
 * @author Raphael Y. de Camargo
 */
public class SimulationDataWriter {

    BufferedWriter fileWriter;
    GeneralSimulationData simData;
	    
    double dayUptime; // mean = 0.85
    double nightUptime;
    
    double dayIdleTime; // mean = 0.73 
    double nightIdleTime;

    /**
     * Creates the simulation data and writes data to file. 
     */
    public static void main(String[] args) {
       
        int numberOfCdrms = 1000;
        int numberOfBrokers = 1;
        //int seed = 123; // Used to generate original files
        int seed = new Random().nextInt();
        SimulationDataWriter simulationDataWriter = new SimulationDataWriter(numberOfCdrms, numberOfBrokers, seed);      
        
        int numberOfTimeZones = 24;
        String outputFile = "experiments/exp-1000-24.dat";
        simulationDataWriter.writeFailureTimesToFile(outputFile, numberOfTimeZones);
        
        System.out.println("Finished!");
    }

    SimulationDataWriter(int numberOfCdrms, int numberOfBrokers, int seed) {

        int numberOfExperiments = 1;        

        Random random = new Random();
        random.setSeed(seed);
        simData = new GeneralSimulationData(random);
        simData.generateSimulationData(numberOfCdrms, numberOfBrokers, numberOfExperiments);
               
    }

    void setExperiment0 () {
        dayUptime = 1.0; // mean = 1.00
        nightUptime = 1.0;
        
        dayIdleTime = 1.0; // mean = 1.00 
        nightIdleTime = 1.0;        
    }

    // Mutka + Bolowsky
    void setExperiment1 () {
        dayUptime = 0.9; // mean = 0.85
        nightUptime = 0.825;
        
        dayIdleTime = 0.6; // mean = 0.73 
        nightIdleTime = 0.8;        
    }
    
    // Domingues
    void setExperiment2 () {
        dayUptime = 0.6; // mean = 0.5
        nightUptime = 0.45;
        
        dayIdleTime = 0.25; // mean = 0.35
        nightIdleTime = 0.4;
        
    }

    // Bolowsky + Acharay
    void setExperiment3 () {
        dayUptime = 0.9; // mean = 0.85
        nightUptime = 0.825;
        
        dayIdleTime = 0.4; // mean = 0.6
        nightIdleTime = 0.7;
        
    }

    /**
     *  Write simulationData to file.
     */
    void writeFailureTimesToFile (String filename, int numberOfTimeZones) {
        
        try { 
            fileWriter = new BufferedWriter( new FileWriter(filename) );

            writeGeneralSimulationData();
                        
            for (CdrmSimulationData cdrmData : simData.cdrmDataList) {
            	
            	double randomDouble = Math.random();
            	if (randomDouble < 0.0) setExperiment0();
            	else if (randomDouble < 0.3333) setExperiment1();
            	else if (randomDouble < 0.6666) setExperiment2();
            	else if (randomDouble < 1.0000) setExperiment3();
            	
                cdrmData.setFailureExperimentsData(dayUptime, dayIdleTime, nightUptime, nightIdleTime, numberOfTimeZones);
                writeCdrmSimulationData(cdrmData);
            }
                            
            fileWriter.flush();
        } 
        catch (IOException e) { e.printStackTrace(); }
                
    }
    
    private void writeGeneralSimulationData( ) throws IOException {
        fileWriter.write("@SIMULATION");
        fileWriter.newLine();
        fileWriter.write("numberOfCdrms="       + simData.numberOfCdrms   + " ");
        fileWriter.write("numberOfBrokers="     + simData.numberOfBrokers + " ");
        fileWriter.write("numberOfExperiments=" + simData.numberOfExperiments);
        fileWriter.newLine();
    }
    
    private void writeCdrmSimulationData( CdrmSimulationData cdrmData ) throws IOException {

        fileWriter.write("@CDRM" + cdrmData.cdrmNumber);
        fileWriter.newLine();
        
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("numberOfAdrs=" + cdrmData.adrDataList.size() + " ");
        stringBuffer.append("events=[");        
        int numberOfEvents = cdrmData.cdrmStateList.size();
        for (int eventIndex = 0; eventIndex < numberOfEvents; eventIndex++) {
            
            if (cdrmData.cdrmStateList.get(eventIndex) == CdrmState.JOIN)
                stringBuffer.append("J:");
            else if (cdrmData.cdrmStateList.get(eventIndex) == CdrmState.DEPART)
                stringBuffer.append("D:");

            stringBuffer.append(cdrmData.cdrmEventTimeList.get(eventIndex).doubleValue());
            
            if (eventIndex < numberOfEvents-1)
                stringBuffer.append("|");
        }       
        stringBuffer.append("]");
        fileWriter.write(stringBuffer.toString());
        fileWriter.newLine();
        
        for (AdrSimulationData adrData : cdrmData.adrDataList)
            writeAdrSimulationData(adrData);

    }

    private void writeAdrSimulationData( AdrSimulationData adrData ) throws IOException {

        fileWriter.write("@ADR" + adrData.adrNumber);
        fileWriter.newLine();

        fileWriter.append("freeSpace="         + adrData.freeStorageSpace + " ");
        fileWriter.append("timeOffset="        + adrData.timeOffset       + " ");
        fileWriter.append("meanDayUptime="     + adrData.meanDayUptime    + " ");
        fileWriter.append("meanNightUptime="   + adrData.meanNightUptime  + " ");
        fileWriter.append("meanDayIdleness="   + adrData.meanDayIdleTime  + " ");
        fileWriter.append("meanNightIdleness=" + adrData.meanNightIdleTime);        
        fileWriter.newLine();

    }
        
}
