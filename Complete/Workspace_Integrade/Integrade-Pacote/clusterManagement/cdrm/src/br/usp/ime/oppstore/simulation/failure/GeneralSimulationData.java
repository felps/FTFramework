package br.usp.ime.oppstore.simulation.failure;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class GeneralSimulationData {

    int numberOfCdrms;
    int numberOfBrokers;
    int numberOfExperiments;

    List<CdrmSimulationData> cdrmDataList;
    
    Random random;
    
    GeneralSimulationData( Random random ){
        this.random = random;
    }
    
    /**
     * Creates a 'numberOfCdrms' CdrmSimulationData classes
     * 
     * @param numberOfCdrms	The number of CDRMs in the simulation
     * @param numberOfBrokers The number of brokers which will submit data (ex: the same as number of CDRMs)
     * @param numberOfExperiments Should always use 1
     */
    void generateSimulationData ( int numberOfCdrms, int numberOfBrokers, int numberOfExperiments ) {
        this.numberOfBrokers = numberOfBrokers;
        this.numberOfCdrms = numberOfCdrms;
        this.numberOfExperiments = numberOfExperiments;

        // Number of ADRs on each cluster (10, 20, 50, 100, or 200 machines)
        int[] numberOfAdrsList = {10, 20, 50, 100, 200};
        int[][] freeSpaceList = { {1000000, 10000000}, {20000000, 50000000} };
        
        //int[] numberOfAdrsList = {10};
        //int[][] freeSpaceList = { {20000000, 50000000} };
        
        cdrmDataList = new LinkedList<CdrmSimulationData>();
        for (int i=0; i<numberOfCdrms; i++) {
            CdrmSimulationData cdrmData = new CdrmSimulationData(random);
            
            int numberOfAdrs = numberOfAdrsList[random.nextInt(numberOfAdrsList.length)];
            
            int freeSpaceRandom = random.nextInt(freeSpaceList.length);
            int minFreeSpace = freeSpaceList[freeSpaceRandom][0];
            int maxFreeSpace = freeSpaceList[freeSpaceRandom][0];            
            
            cdrmData.createCdrmSimulationData(i, numberOfAdrs, minFreeSpace, maxFreeSpace);
            
            cdrmDataList.add(cdrmData);
        }
    }        
}
