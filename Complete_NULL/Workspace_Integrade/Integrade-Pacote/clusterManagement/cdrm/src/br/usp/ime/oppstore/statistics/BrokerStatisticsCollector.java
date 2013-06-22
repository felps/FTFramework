package br.usp.ime.oppstore.statistics;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import br.usp.ime.oppstore.simulation.adr.AdrFailureModel;

public class BrokerStatisticsCollector {

	PrintStream outputStream;
	
    class BrokerRetrievalData {
        int nFragmentsTotal;
        Vector<int[]>   nFragmentsUpVector; 
        Vector<int[]>   nFragmentsIdleVector;
        
        BrokerRetrievalData (
                Vector<Integer> nFragmentsTotalVector,
                Vector<int[]>   nFragmentsUpVector,
                Vector<int[]>   nFragmentsIdleVector) {
            
        	this.nFragmentsTotal      = nFragmentsTotalVector.get(0);
            this.nFragmentsIdleVector = nFragmentsIdleVector;            
            this.nFragmentsUpVector   = nFragmentsUpVector;            
        } 
    }
    
    /**
     * key:   brokerNumber
     * value: the collected data for that broker 
     */
    HashMap<Integer, BrokerRetrievalData > retrievalDataMap;
    StatisticsDataWriter dataWriter;
    String filename;
    int nBrokers;
    int nFinishedBrokers;
    
    public BrokerStatisticsCollector () {
        this.retrievalDataMap = new HashMap<Integer, BrokerRetrievalData >();
        this.dataWriter = new StatisticsDataWriter();
        this.filename = dataWriter.generateFileName("broker-");
        
        this.nBrokers = 0;
        this.nFinishedBrokers = 0;
        
    	//outputStream = System.out;
    	
    	try {
    		outputStream = new PrintStream( new BufferedOutputStream( new FileOutputStream("broker.dat") ));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

    }

    public void incrementNumberOfBrokers() {
        this.nBrokers++;
    }

    public void setNumberOfBrokers( int numberOfBrokers ) {
        this.nBrokers = numberOfBrokers;
    }
        
    public void setAccessBrokerRetrievalData( int brokerNumber, 
    		Vector<Integer> nFragmentsTotalVector, Vector<int[]> nFragmentsUpVector, Vector<int[]> nFragmentsIdleVector ) {

     		retrievalDataMap.put( brokerNumber, new BrokerRetrievalData (
                nFragmentsTotalVector, nFragmentsUpVector, nFragmentsIdleVector) );
    }

    public void writeRetrievalData() {        
        // Should we write to file after each round?
        dataWriter.writeDataToFile(filename, this);
    }
    
    public void printRetrievalData() {
        
        nFinishedBrokers++;        
        if (nFinishedBrokers < nBrokers) return;
        
        // evaluate the mean and standardDeviation for each experiment,
        // with different number of fragments
        
        int numberOfRounds = AdrFailureModel.numberOfWeeks * AdrFailureModel.nCompleteWeekRounds;

        int numberOfRetrievals = retrievalDataMap.get(0).nFragmentsUpVector.size();
        int numberOfFragmentsTotal = retrievalDataMap.get(0).nFragmentsTotal;

        /**
         * Contains the number of recovered files for different number of necessary fragments
         */
        double[] meanNumberOfRecoveredIdle1 = new double[numberOfFragmentsTotal];
        double[] meanNumberOfRecoveredUp1 = new double[numberOfFragmentsTotal];

        double[] meanNumberOfRecoveredIdle2 = new double[numberOfFragmentsTotal];
        double[] meanNumberOfRecoveredUp2 = new double[numberOfFragmentsTotal];

        double[] meanNumberOfRecoveredIdle3 = new double[numberOfFragmentsTotal];
        double[] meanNumberOfRecoveredUp3 = new double[numberOfFragmentsTotal];

        /**
         * Evaluates the mean number of succesfull file retrievals for different values of necessary fragments
         */
        for ( BrokerRetrievalData retrievalData : retrievalDataMap.values() ) {            

            for ( int file = 0; file < numberOfRetrievals; file++ ) {
                                 
                int[] nFragmentsIdle = retrievalData.nFragmentsIdleVector.get(file);
                int[] nFragmentsUp   = retrievalData.nFragmentsUpVector.get(file);

                /**
                 * For each round, evaluates the number of succesful retrievals
                 */
                int round = 0;
                for (; round < numberOfRounds; round++)                                        
                    for ( int nFragments=0; nFragments < numberOfFragmentsTotal; nFragments++ ) {                  
                        if (nFragmentsIdle[round] > nFragments )
                            meanNumberOfRecoveredIdle1[nFragments]++;
                        if (nFragmentsUp[round] > nFragments )
                            meanNumberOfRecoveredUp1[nFragments]++;                    
                }

                for (; round < 2*numberOfRounds; round++)                                        
                    for ( int nFragments=0; nFragments < numberOfFragmentsTotal; nFragments++ ) {                  
                        if (nFragmentsIdle[round] > nFragments )
                            meanNumberOfRecoveredIdle2[nFragments]++;
                        if (nFragmentsUp[round] > nFragments )
                            meanNumberOfRecoveredUp2[nFragments]++;                    
                }

                for (; round < 3*numberOfRounds; round++)                                        
                    for ( int nFragments=0; nFragments < numberOfFragmentsTotal; nFragments++ ) {                  
                        if (nFragmentsIdle[round] > nFragments )
                            meanNumberOfRecoveredIdle3[nFragments]++;
                        if (nFragmentsUp[round] > nFragments )
                            meanNumberOfRecoveredUp3[nFragments]++;                    
                }

            }
            
        }

        // divide by numberOfRounds and numberOfFiles and numberOfBrokers
        for ( int nFragments=0; nFragments < numberOfFragmentsTotal; nFragments++ ){
        	double meanIdle = meanNumberOfRecoveredIdle1[nFragments]/numberOfRounds/numberOfRetrievals/nBrokers;
        	double meanUp = meanNumberOfRecoveredUp1[nFragments]/numberOfRounds/numberOfRetrievals/nBrokers;
        	outputStream.printf("%.6f|%.6f ", meanIdle, meanUp);
        }
        outputStream.println();
        for ( int nFragments=0; nFragments < numberOfFragmentsTotal; nFragments++ ){   
        	double meanIdle = meanNumberOfRecoveredIdle2[nFragments]/numberOfRounds/numberOfRetrievals/nBrokers;
        	double meanUp = meanNumberOfRecoveredUp2[nFragments]/numberOfRounds/numberOfRetrievals/nBrokers;
        	outputStream.printf("%.6f|%.6f ", meanIdle, meanUp);
        }
        outputStream.println();
        for ( int nFragments=0; nFragments < numberOfFragmentsTotal; nFragments++ ){   
        	double meanIdle = meanNumberOfRecoveredIdle3[nFragments]/numberOfRounds/numberOfRetrievals/nBrokers;
        	double meanUp = meanNumberOfRecoveredUp3[nFragments]/numberOfRounds/numberOfRetrievals/nBrokers;
        	outputStream.printf("%.6f|%.6f ", meanIdle, meanUp);
        } 
        outputStream.println();

        outputStream.flush();
    }
    
}
