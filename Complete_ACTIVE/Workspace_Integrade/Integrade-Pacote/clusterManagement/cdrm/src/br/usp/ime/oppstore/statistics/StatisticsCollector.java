package br.usp.ime.oppstore.statistics;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.pastry.Id.Distance;
import br.usp.ime.oppstore.cdrm.CdrmApp;
import br.usp.ime.oppstore.cdrm.CdrmApp.OpStoreIdProtocol;
import br.usp.ime.oppstore.message.FileFragmentIndexMessage;
import br.usp.ime.oppstore.message.StoreFragmentMessage;
import br.usp.ime.oppstore.message.FileFragmentIndexMessage.RequestType;
import br.usp.ime.oppstore.tests.adaptive.CdrmTestInformation;
import br.usp.ime.virtualId.NodeInformation;
import br.usp.ime.virtualId.VirtualNode;
import br.usp.ime.virtualId.VirtualSpace;
import br.usp.ime.virtualId.util.DistanceManipulator;
import br.usp.ime.virtualId.util.IdManipulator;

public class StatisticsCollector {
    
	PrintStream outputStream;
	
    /**
     * Mantains the number of FIS stored on each CDRM
     */
    ConcurrentHashMap<CdrmApp, AtomicInteger> numberStoredFFIMap;
    /**
     * Mantains the number of fragments stored on each CDRM
     */    
    ConcurrentHashMap<CdrmApp, AtomicInteger> numberStoredFragmentsMap;

    ConcurrentHashMap<CdrmApp, AtomicLong> cdrmUsedSpaceMap;

    long fragmentToFileMeanDistance = 0;
    long totalNumberOfForwardedFragments = 0;
    
    /**
     * Mantains the number of hops necessary for fragments to reach the target CDRM
     */
    ConcurrentHashMap<Id, AtomicInteger> fragmentStoreHopCountMap;


    /**
     * Mantains the number of hops necessary for a FIS to reach the target CDRM
     */
    ConcurrentHashMap<Id, AtomicInteger> fisStoreHopCountMap;

    /**
     * Mantains the number of hops necessary for a FIS to reach the target CDRM
     */
    ConcurrentHashMap<Id, AtomicInteger> fisRetrievalHopCountMap;
    
    AtomicInteger numberOfFragmentsNotStored = new AtomicInteger(0);
    AtomicInteger numberOfFragmentsNotStoredAfter = new AtomicInteger(0);

    int numberOfDiscardRequests = 80;
    
    boolean enableFragmentStoreHopListMap = false;
    ConcurrentHashMap<Id, Vector<Id>> fragmentStoreHopListMap;
       
    public StatisticsCollector() {
    	
    	//outputStream = System.out;    	
    	try { outputStream = new PrintStream( new BufferedOutputStream( new FileOutputStream("results.dat") )); } 
    	catch (FileNotFoundException e) { e.printStackTrace(); }		
    	
        numberStoredFFIMap       = new ConcurrentHashMap<CdrmApp, AtomicInteger>();
        numberStoredFragmentsMap = new ConcurrentHashMap<CdrmApp, AtomicInteger>();
        cdrmUsedSpaceMap         = new ConcurrentHashMap<CdrmApp, AtomicLong>();
        
        fragmentStoreHopCountMap = new ConcurrentHashMap<Id, AtomicInteger>();
        fisStoreHopCountMap      = new ConcurrentHashMap<Id, AtomicInteger>();
        fisRetrievalHopCountMap  = new ConcurrentHashMap<Id, AtomicInteger>();
        
        fragmentStoreHopListMap  = new ConcurrentHashMap<Id, Vector<Id>>();
    }    

    
    public void addFragmentNotStored (int requestNumber) {
    	numberOfFragmentsNotStored.incrementAndGet();
    	if (requestNumber > numberOfDiscardRequests)
    		numberOfFragmentsNotStoredAfter.incrementAndGet();
    }
    
    //---------------------------------------------------------------------------------------
    // StoreFragment Message
    //---------------------------------------------------------------------------------------    

    public void setStoreFragmentMessageForwarded(CdrmApp cdrmApp, StoreFragmentMessage message, NodeHandle nextHop) {        

        /**
         *  We use "cdrmApp.getNode().getId() != nextHop.getId()" because the last 
         *  forward message is delivered in the same node.                    
         **/           
        if (message.isResponse == false && cdrmApp.getVirtualNode().getNode().getId() != nextHop.getId()) {
            incrementHopCount(message.fragmentId, nextHop.getId(), fragmentStoreHopCountMap);
        }
    }
    
    public void setFragmentStored(CdrmApp cdrmApp, StoreFragmentMessage message) {
    	
    	AtomicInteger counter = numberStoredFragmentsMap.get(cdrmApp);
    	if (counter == null)
    		counter = numberStoredFragmentsMap.putIfAbsent(cdrmApp, new AtomicInteger(1));
    	if (counter != null)
    		counter.incrementAndGet();

    	AtomicLong usedSpace = cdrmUsedSpaceMap.get(cdrmApp);
    	if (usedSpace == null)
    		usedSpace = cdrmUsedSpaceMap.putIfAbsent(cdrmApp, new AtomicLong(message.fragmentSize));
    	if (usedSpace != null)
    		usedSpace.addAndGet(message.fragmentSize);

    	Distance distance = (Distance) message.fileId.distanceFromId(cdrmApp.getVirtualNode().getNode().getId());

    	fragmentToFileMeanDistance += 
    		DistanceManipulator.convertDistanceToLong(distance) / DistanceManipulator.DIVISOR;
    	totalNumberOfForwardedFragments++;

    }

    //---------------------------------------------------------------------------------------
    // FileInformation Message
    //---------------------------------------------------------------------------------------    

    public void setFileInformationMessageForwarded(CdrmApp cdrmApp, FileFragmentIndexMessage message, NodeHandle nextHop) {

        /**
         *  We use "cdrmApp.getNode().getId() != nextHop.getId()" because the last 
         *  forward message is delivered in the same node.                    
         **/           
        if (message.isResponse == false && cdrmApp.getVirtualNode().getNode().getId() != nextHop.getId()) {
            if (message.requestType == RequestType.STORAGE)
                incrementHopCount(message.fileId, nextHop.getId(), fisStoreHopCountMap);
            else
                incrementHopCount(message.fileId, nextHop.getId(), fisRetrievalHopCountMap);            
        }

    }

    //--------------------------------------------------------------------------------------
    
    public void setFileInformationMessageDelivered(CdrmApp cdrmApp, FileFragmentIndexMessage message) {
        
        if (message.requestType == RequestType.STORAGE && message.isResponse == false && message.isReplicaRequest == false) {
            AtomicInteger counter = numberStoredFFIMap.get(cdrmApp);
            if (counter == null) {
                counter = numberStoredFFIMap.putIfAbsent(cdrmApp, new AtomicInteger(1));
                numberStoredFragmentsMap.putIfAbsent(cdrmApp, new AtomicInteger(0));
            }
            if (counter != null)
                counter.incrementAndGet();
        }        
    }
    
    //--------------------------------------------------------------------------------------
    
    private void incrementHopCount(Id fileId, Id nodeId, ConcurrentHashMap<Id, AtomicInteger> hopCountMap) {

        AtomicInteger counter = hopCountMap.get(fileId);
        if (counter == null) 
            counter = hopCountMap.putIfAbsent(fileId, new AtomicInteger(1));            
        if (counter != null)
            counter.incrementAndGet();
    }
    
    //====================================================================================================
    
    public void printVirtualIdStatistics(Collection<CdrmApp> cdrmApps) {
    	
		if (CdrmApp.idProtocol == OpStoreIdProtocol.PASTRY) return;
		
    	int numberOfCdrms = cdrmApps.size();
    	double meanDistance = 1.0 * DistanceManipulator.MAX_DISTANCE / DistanceManipulator.DIVISOR / numberOfCdrms ;
    	
    	double[] virtualIdDriftList = new double[numberOfCdrms];
    	
    	double[] virtualLeafSetRangeList     = new double[numberOfCdrms];
    	double[] pastryLeafSetRangeList      = new double[numberOfCdrms];
    	double[] virtualLeafSetSizeList      = new double[numberOfCdrms];
    	double[] virtualPastryRangeRatioList = new double[numberOfCdrms];
    	       
        double[] capacityList              = new double[numberOfCdrms];
        double[] virtualIdRangeList		   = new double[numberOfCdrms];
        double[] pastryIdRangeList		   = new double[numberOfCdrms];
        double[] expectedIdRangeList       = new double[numberOfCdrms];
        
        double[] virtualExpectedRatioList = new double[numberOfCdrms];
        double[] pastryExpectedRatioList  = new double[numberOfCdrms];

    	int pos=0;
    	for ( CdrmApp cdrmApp : cdrmApps ) {

    		/**
    		 * Gets information about the CDRM
    		 */
            VirtualSpace virtualSpace = cdrmApp.getVirtualNode().getVirtualSpace(1);            
            
            /**
             * Evaluates the distance of the virtual Id to the Pastry Id
             */
    		Distance distance = (Distance) cdrmApp.getVirtualNode().getNode().getId().distanceFromId( virtualSpace.getVirtualId() );    		
    		virtualIdDriftList[pos] = 1.0 * DistanceManipulator.convertDistanceToLong(distance) / DistanceManipulator.DIVISOR;

    		/**
    		 * Sets the capacity, pastryIdRange, virtualIdRange 
    		 */
            capacityList[pos] = virtualSpace.getCapacity();   
            if (virtualSpace.getCcwVirtualId().clockwise( virtualSpace.getVirtualId() ) )
            	distance = (Distance)virtualSpace.getVirtualId().distanceFromId( virtualSpace.getCcwVirtualId());                    			                
            else
           		distance = (Distance)virtualSpace.getVirtualId().longDistanceFromId( virtualSpace.getCcwVirtualId());
            virtualIdRangeList[pos]  = 1.0 * DistanceManipulator.convertDistanceToLong( distance ) / IdManipulator.DIVISOR;            

            Id leftId = cdrmApp.getVirtualNode().getNode().getLeafSet().get(-1).getId();
            Id rightId = cdrmApp.getVirtualNode().getNode().getId();
            if (leftId.clockwise( rightId ) )
            	distance = (Distance)rightId.distanceFromId( leftId );                    			                
            else
           		distance = (Distance)rightId.longDistanceFromId( leftId );
            pastryIdRangeList[pos]  = 1.0 * DistanceManipulator.convertDistanceToLong( distance ) / IdManipulator.DIVISOR;            

            /**
             * Evaluate the leafset properties: size, virtual range, Pastry range, virtual/Pastry range
             */    		
    		NodeInformation[] nodeInformationArray = virtualSpace.getVirtualLeafSet().getNodeInformationArray();
    		if (nodeInformationArray.length == 0) return;
    		virtualLeafSetSizeList[pos] = virtualSpace.getVirtualLeafSet().size();
    		
            Id leftVirtualId  = nodeInformationArray[0].getVirtualId();
            Id rightVirtualId = nodeInformationArray[nodeInformationArray.length-1].getVirtualId();
            Distance virtualLeafSetRange = null;
            if (leftVirtualId.clockwise( rightVirtualId ) )
    			 virtualLeafSetRange = (Distance) rightVirtualId.distanceFromId( leftVirtualId );                    			                
            else
            	virtualLeafSetRange = (Distance) rightVirtualId.longDistanceFromId( leftVirtualId );
            virtualLeafSetRangeList[pos] = 1.0 * DistanceManipulator.convertDistanceToLong(virtualLeafSetRange) / DistanceManipulator.DIVISOR;

            Id leftPastryId  = virtualSpace.getVirtualLeafSet().getLeftmostLeaf().getId();
            Id rightPastryId = virtualSpace.getVirtualLeafSet().getRightmostLeaf().getId();
            Distance pastryLeafSetRange = null;
            if (leftPastryId.clockwise( rightPastryId ) )
    			 pastryLeafSetRange = (Distance) rightPastryId.distanceFromId( leftPastryId );                    			                
            else
            	pastryLeafSetRange = (Distance) rightPastryId.longDistanceFromId( leftPastryId );
            pastryLeafSetRangeList[pos] = 1.0 * DistanceManipulator.convertDistanceToLong(pastryLeafSetRange) / DistanceManipulator.DIVISOR;

            virtualPastryRangeRatioList[pos] = virtualLeafSetRangeList[pos] / pastryLeafSetRangeList[pos];
            
            /**
             * Updates the cdrmApp index
             */
    		pos++;    	
    	}

    	double meanIdDrift = StatisticsEvaluator.evaluateMean(virtualIdDriftList);
    	double meanVirtualLeafSetRange = StatisticsEvaluator.evaluateMean(virtualLeafSetRangeList);
    	double meanPastryLeafSetRange  = StatisticsEvaluator.evaluateMean(pastryLeafSetRangeList);
    	double meanVirtualLeafSetSize  = StatisticsEvaluator.evaluateMean(virtualLeafSetSizeList);
        double meanCapacity = StatisticsEvaluator.evaluateMean(capacityList);
        for (int i=0; i < numberOfCdrms; i++) {
        	expectedIdRangeList[i] = ( capacityList[i] / meanCapacity ) * meanDistance;
        	virtualExpectedRatioList[i] = 100 * Math.abs(virtualIdRangeList[i] - expectedIdRangeList[i]) / expectedIdRangeList[i];
        	pastryExpectedRatioList[i]  = 100 * Math.abs(pastryIdRangeList[i]  - expectedIdRangeList[i]) / expectedIdRangeList[i];
        }
        double meanVirtualIdRange = StatisticsEvaluator.evaluateMean(virtualIdRangeList);
        double meanPastryIdRange  = StatisticsEvaluator.evaluateMean(pastryIdRangeList);
        double meanExpectedIdRange = StatisticsEvaluator.evaluateMean(expectedIdRangeList);        
        double meanVirtualExpectedRatio = StatisticsEvaluator.evaluateMean(virtualExpectedRatioList);
        double meanPastryExpectedRatio  = StatisticsEvaluator.evaluateMean(pastryExpectedRatioList);
        double meanVirtualPastryRangeRatio = StatisticsEvaluator.evaluateMean(virtualPastryRangeRatioList);
        
    	double stdDevIdDrift         = StatisticsEvaluator.evaluateStdDev(virtualIdDriftList, meanIdDrift);
    	double stdDevVirtualLeafSetRange = StatisticsEvaluator.evaluateStdDev(virtualLeafSetRangeList, meanVirtualLeafSetRange);
    	double stdDevPastryLeafSetRange  = StatisticsEvaluator.evaluateStdDev(pastryLeafSetRangeList, meanPastryLeafSetRange);
    	double stdDevVirtualLeafSetSize  = StatisticsEvaluator.evaluateStdDev(virtualLeafSetSizeList, meanVirtualLeafSetSize);
        double stdDevCapacity        = StatisticsEvaluator.evaluateStdDev(capacityList, meanCapacity);        
        double stdDevVirtualIdRange  = StatisticsEvaluator.evaluateStdDev(virtualIdRangeList, meanVirtualIdRange);
        double stdDevPastryIdRange   = StatisticsEvaluator.evaluateStdDev(pastryIdRangeList, meanPastryIdRange);
        double stdDevExpectedIdRange = StatisticsEvaluator.evaluateStdDev(expectedIdRangeList, meanExpectedIdRange);
        double stdDevVirtualExpectedRatio    = StatisticsEvaluator.evaluateStdDev(virtualExpectedRatioList, meanVirtualExpectedRatio);
        double stdDevPastryExpectedRatio     = StatisticsEvaluator.evaluateStdDev(pastryExpectedRatioList, meanPastryExpectedRatio);
        double stdDevVirtualPastryRangeRatio = StatisticsEvaluator.evaluateStdDev(virtualPastryRangeRatioList, meanVirtualPastryRangeRatio);

        outputStream.printf("Number of CDRMS: %d\n", numberOfCdrms);
        outputStream.printf("meanIdDrift:            %20f | %20f\n", meanIdDrift, stdDevIdDrift);
        outputStream.printf("meanVirtualLeafRange:   %20f | %20f\n", meanVirtualLeafSetRange, stdDevVirtualLeafSetRange);
        outputStream.printf("meanPastryLeafRange:    %20f | %20f\n", meanPastryLeafSetRange, stdDevPastryLeafSetRange);
        outputStream.printf("meanVirtualLeafSize:    %20f | %20f\n", meanVirtualLeafSetSize, stdDevVirtualLeafSetSize);
        outputStream.printf("virtualPastryLeafRatio: %20f | %20f\n", meanVirtualPastryRangeRatio, stdDevVirtualPastryRangeRatio);
        
        outputStream.printf("meanCapacity:           %20f | %20f\n", meanCapacity, stdDevCapacity);        
        outputStream.printf("meanVirtualIdRange:     %20f | %20f\n", meanVirtualIdRange, stdDevVirtualIdRange);                
        outputStream.printf("meanPastryIdRange:      %20f | %20f\n", meanPastryIdRange, stdDevPastryIdRange);
        outputStream.printf("meanExpectedIdRange:    %20f | %20f\n", meanExpectedIdRange, stdDevExpectedIdRange);
        outputStream.printf("virtualExpectedRatio:   %20f | %20f\n", meanVirtualExpectedRatio, stdDevVirtualExpectedRatio);
        outputStream.printf("pastryExpectedRatio:    %20f | %20f\n", meanPastryExpectedRatio, stdDevPastryExpectedRatio);
        outputStream.printf("\n");
        
        if (false) {
        	for (int i=0; i<virtualLeafSetRangeList.length; i++)
        		outputStream.printf("pastryRange: %15f virtualLeaf: %15f virtaulPastryRatio: %15f %d\n", pastryLeafSetRangeList[i], virtualLeafSetRangeList[i], virtualPastryRangeRatioList[i], (int)virtualLeafSetSizeList[i]);
            outputStream.printf("\n");
        }
        
        flushOutput();        
    }

    public void flushOutput() { 
		outputStream.flush();
    }
	
    //====================================================================================================
    
    public void printCdrmStorageCount() {
        
    	int numberOfCdrms = numberStoredFragmentsMap.size(); 
    	
    	if (numberOfCdrms == 0) return;
                
        double[] idRangeCapacityList  = new double[numberOfCdrms];        
        double[] fisCapacityRatioList = new double[numberOfCdrms];
        
        double[] totalSpaceList = new double[numberOfCdrms];        
        double[] capacityList   = new double[numberOfCdrms];
        double[] fragmentList   = new double[numberOfCdrms];
        double[] usedSpaceList  = new double[numberOfCdrms];

        double[] fragmentTotalRatioList    = new double[numberOfCdrms];
        double[] fragmentCapacityRatioList = new double[numberOfCdrms];

        double[] usedTotalRatioList    = new double[numberOfCdrms];
        double[] usedCapacityRatioList = new double[numberOfCdrms];
        
        
        int totalFragmentCount = 0;
        
        double maximumCapacity = 0;
        double maximumTotalStorage = 0;
        
        double totalStorageSpace = 0;
        double totalUsedSpace = 0;

        double meanCdrmCapacity = 0;
        HashMap<CdrmApp, CdrmTestInformation> cdrmInfoMap = new HashMap<CdrmApp, CdrmTestInformation>();
        for (CdrmApp cdrmApp : numberStoredFragmentsMap.keySet()) {
        	CdrmTestInformation cdrmInfo = cdrmApp.getCdrmInformation();
        	cdrmInfoMap.put( cdrmApp, cdrmInfo );
        	meanCdrmCapacity += cdrmInfo.capacity;
        }
        meanCdrmCapacity /= cdrmInfoMap.size();
        
        int pos = 0;        
        for (CdrmApp cdrmApp : numberStoredFragmentsMap.keySet()) {
            
            CdrmTestInformation cdrmInfo = cdrmInfoMap.get( cdrmApp );

            long cdrmUsedSpace = 0;
            if (cdrmUsedSpaceMap.get( cdrmApp ) != null)
            	cdrmUsedSpace = cdrmUsedSpaceMap.get( cdrmApp ).longValue();
            long totalSpace = cdrmApp.getAdrManager().getTotalAdrSpace();
            
            long numberOfStoredFragments = 0;            
            if ( numberStoredFragmentsMap.get( cdrmApp ) != null)
            	numberOfStoredFragments = numberStoredFragmentsMap.get( cdrmApp ).intValue();

            long numberOfStoredFFI = 0;            
            if ( numberStoredFFIMap.get( cdrmApp ) != null)
            	numberOfStoredFFI = numberStoredFFIMap.get( cdrmApp ).intValue();

            totalStorageSpace += totalSpace;
            totalUsedSpace += cdrmUsedSpace;
                        
            outputStream.print( cdrmApp );
            outputStream.printf(
            		" -> FIS:%3d fragments:%4d capacity:%5.2f lastCapacity:%5.2f idRange:%5d usedSpace:%7d totalSpace:%5.1f",
            		numberOfStoredFFI, 
            		numberOfStoredFragments,  
            		cdrmInfo.capacity / meanCdrmCapacity, 
            		cdrmInfo.lastUpdatedCapacity / meanCdrmCapacity,
            		cdrmInfo.longIdRange / DistanceManipulator.DIVISOR,
            		cdrmUsedSpace,
            		((double)cdrmUsedSpace) / totalSpace
            );
            outputStream.println();

            if (cdrmInfo.capacity > maximumCapacity)
            	maximumCapacity = cdrmInfo.capacity;
            if (totalSpace > maximumTotalStorage)
            	maximumTotalStorage = totalSpace;            
            
            fisCapacityRatioList[pos]      = numberOfStoredFFI;
            idRangeCapacityList[pos]       = cdrmInfo.longIdRange/IdManipulator.DIVISOR;
            fragmentList[pos]  = numberOfStoredFragments;
            usedSpaceList[pos] = cdrmUsedSpace;

            fragmentTotalRatioList[pos]    = numberOfStoredFragments;
            fragmentCapacityRatioList[pos] = numberOfStoredFragments;

            usedTotalRatioList[pos]    = cdrmUsedSpace;
            usedCapacityRatioList[pos] = cdrmUsedSpace;
            
            capacityList[pos]      = cdrmInfo.capacity;
            totalSpaceList[pos]    = totalSpace;
            
            totalFragmentCount += fragmentTotalRatioList[pos];
            
            pos++;
        }

        for (pos=0; pos < capacityList.length; pos++) {
        	//capacityList[pos]   /= maximumCapacity;
            //totalSpaceList[pos] /= maximumTotalStorage;
            
            fragmentTotalRatioList[pos]    /= totalSpaceList[pos];
            fragmentCapacityRatioList[pos] /= capacityList[pos];

            usedTotalRatioList[pos]    /= totalSpaceList[pos];
            usedCapacityRatioList[pos] /= capacityList[pos];
            
            fisCapacityRatioList[pos]  /= capacityList[pos];
            idRangeCapacityList[pos]   /= capacityList[pos];
         }
        
        double meanCapacity   = StatisticsEvaluator.evaluateMean(capacityList);
        double meanTotalSpace = StatisticsEvaluator.evaluateMean(totalSpaceList);        
        double meanFragment   = StatisticsEvaluator.evaluateMean(fragmentList);
        double meanUsedSpace  = StatisticsEvaluator.evaluateMean(usedSpaceList);        
        
        double meanFisCapacityRatio      = StatisticsEvaluator.evaluateMean(fisCapacityRatioList);
        double meanIdRangeCapacityRatio  = StatisticsEvaluator.evaluateMean(idRangeCapacityList);
        
        double meanFragmentTotalRatio    = StatisticsEvaluator.evaluateMean(fragmentTotalRatioList);
        double meanFragmentCapacityRatio = StatisticsEvaluator.evaluateMean(fragmentCapacityRatioList);
        
        double meanUsedTotalRatio    = StatisticsEvaluator.evaluateMean(usedTotalRatioList);
        double meanUsedCapacityRatio = StatisticsEvaluator.evaluateMean(usedCapacityRatioList);

        //--
        
        double stdDevCapacity   = StatisticsEvaluator.evaluateStdDev(capacityList, meanCapacity);
        double stdDevTotalSpace = StatisticsEvaluator.evaluateStdDev(totalSpaceList, meanTotalSpace);
        double stdDevFragment   = StatisticsEvaluator.evaluateStdDev(fragmentList, meanFragment);
        double stdDevUsedSpace  = StatisticsEvaluator.evaluateStdDev(usedSpaceList, meanUsedSpace);        
        
        double stdDevFisCapacity     = StatisticsEvaluator.evaluateStdDev(fisCapacityRatioList, meanFisCapacityRatio); 
        double stdDevIdRangeCapacity = StatisticsEvaluator.evaluateStdDev(idRangeCapacityList,  meanIdRangeCapacityRatio);
        
        double stdDevFragmentTotal    = StatisticsEvaluator.evaluateStdDev(fragmentTotalRatioList, meanFragmentTotalRatio);
        double stdDevFragmentCapacity = StatisticsEvaluator.evaluateStdDev(fragmentCapacityRatioList, meanFragmentCapacityRatio);

        double stdDevUsedTotal    = StatisticsEvaluator.evaluateStdDev(usedTotalRatioList, meanUsedTotalRatio);
        double stdDevUsedCapacity = StatisticsEvaluator.evaluateStdDev(usedCapacityRatioList, meanUsedCapacityRatio);

        outputStream.println();
        outputStream.printf("maxCapacity:   %20.8f\n", maximumCapacity);
        outputStream.printf("maxTotalSpace: %20.8f\n", maximumTotalStorage);        
        outputStream.printf("systemUsage:   %20.8f\n", (totalUsedSpace/totalStorageSpace));
        outputStream.printf("capacity:   %20.8f | %20.8f\n", meanCapacity, stdDevCapacity);
        outputStream.printf("totalSpace: %20.8f | %20.8f\n", meanTotalSpace, stdDevTotalSpace);
        outputStream.printf("fragment:   %20.8f | %20.8f\n", meanFragment, stdDevFragment);
        outputStream.printf("usedSpace:  %20.8f | %20.8f\n", meanUsedSpace, stdDevUsedSpace);
        
        outputStream.printf("FisCapacity:      %20.12f | %20.12f\n", meanFisCapacityRatio, stdDevFisCapacity);
        outputStream.printf("IdRangeCapacity:  %20.12f | %20.12f\n", meanIdRangeCapacityRatio, stdDevIdRangeCapacity);
        
        outputStream.printf("meanFragmentCapacity: %20.10f | %20.10f\n", meanFragmentCapacityRatio, stdDevFragmentCapacity);
        outputStream.printf("meanFragmentTotal:    %20.10f | %20.10f\n", meanFragmentTotalRatio, stdDevFragmentTotal);        

        outputStream.printf("meanUsedTotal:     %20.8f | %20.8f\n", meanUsedTotalRatio, stdDevUsedTotal);
        outputStream.printf("meanUsageCapacity: %20.8f | %20.8f\n", meanUsedCapacityRatio, stdDevUsedCapacity);            
        
        outputStream.printf("numberOfForwardedFragments: %d of %d\n", numberOfFragmentsNotStored.intValue(), totalNumberOfForwardedFragments);
        outputStream.printf("numberOfForwardedFragmentsAfter: %d\n", numberOfFragmentsNotStoredAfter.intValue());
        
        if (totalNumberOfForwardedFragments > 0)
            outputStream.println("fragmentToFileMeanDistance:" + 
                    fragmentToFileMeanDistance/totalNumberOfForwardedFragments);
        
        outputStream.println();        
    }
    
    public void printMeanHopCount() {
        
        outputStream.println("-------------------------------------------------------");
        outputStream.println("fragmentStoreHopCountMap");
        printMeanHopCount(fragmentStoreHopCountMap);
        outputStream.println("fisStoreHopCountMap");
        printMeanHopCount(fisStoreHopCountMap);
        outputStream.println("fisRetrievalHopCountMap");
        printMeanHopCount(fisRetrievalHopCountMap);
        outputStream.println("-------------------------------------------------------");        
    }
    
    private void printMeanHopCount(Map<Id, AtomicInteger> hopCountMap) {
        
        double meanHops = 0.0;
        int maxHops = 0;
        for (AtomicInteger numberOfHops : hopCountMap.values()) {
            if (numberOfHops.get() > maxHops) maxHops = numberOfHops.get();
            meanHops += numberOfHops.get();
        }       
        meanHops /= hopCountMap.size();
        
        int[] numberofHopsList = new int[maxHops+1];        
        for (AtomicInteger numberOfHops : hopCountMap.values())
            numberofHopsList[numberOfHops.get()] += 1;
            
        outputStream.println("meanHops:" + meanHops + " maxHops:" + maxHops);
        for (int i=0; i <= maxHops ; i++)
            outputStream.print(i + ":" + numberofHopsList[i] + " ");
        outputStream.println();
    }

    
    public void printClusterAdrsInfo( Vector<ClusterAdrsInformation> clusterAdrsInfoVector) {
        
        int totalNumberOfAdrs = 0;

        /**
         * Finds the maximun storage space and capacity from all ADRs
         */
        double maxStorageSpace = 0;
        double maxCapacity = 0;        
        for (ClusterAdrsInformation clusterAdrsInformation : clusterAdrsInfoVector ) {
        	totalNumberOfAdrs += clusterAdrsInformation.capacity.length;
            for (int i=0; i < clusterAdrsInformation.capacity.length; i++) {
                if (clusterAdrsInformation.capacity[i] > maxCapacity)
                	maxCapacity = clusterAdrsInformation.capacity[i];
                if (clusterAdrsInformation.totalSpace[i] > maxStorageSpace)
                	maxStorageSpace = clusterAdrsInformation.totalSpace[i];                        	
            }
        }

        /**
         * Puts the information from all ADRs in the system in single vectors.
         */
        double[] capacityList   = new double[totalNumberOfAdrs];
        double[] totalSpaceList = new double[totalNumberOfAdrs];                
        double[] fragmentList   = new double[totalNumberOfAdrs];
        double[] usedSpaceList  = new double[totalNumberOfAdrs];

        double[] usedCapacityList = new double[totalNumberOfAdrs];
        double[] usedTotalList    = new double[totalNumberOfAdrs];
        double[] usedIdleList     = new double[totalNumberOfAdrs];

        double[] fragmentCapacityList = new double[totalNumberOfAdrs];
        double[] fragmentTotalList    = new double[totalNumberOfAdrs];
        double[] fragmentIdleList     = new double[totalNumberOfAdrs];
        
        int pos=0;
        for (ClusterAdrsInformation clusterAdrsInformation : clusterAdrsInfoVector ) {

        	int numberOfClusterAdrs = clusterAdrsInformation.capacity.length;
        	for (int i=0; i < numberOfClusterAdrs; i++) {
            	double adrCapacity = clusterAdrsInformation.capacity[i];
            	double usedSpace = clusterAdrsInformation.totalSpace[i] - clusterAdrsInformation.freeSpace[i];
            	int numberOfFragments = clusterAdrsInformation.storedFragments[i]; 
            	
            	capacityList[pos]   = adrCapacity;// / maxCapacity;
                totalSpaceList[pos] = clusterAdrsInformation.totalSpace[i];// / maxStorageSpace;
                fragmentList[pos]   = numberOfFragments;
                usedSpaceList[pos]  = usedSpace;

                usedCapacityList[pos]  = usedSpace / capacityList[pos];
                usedTotalList[pos]     = usedSpace / totalSpaceList[pos];
                usedIdleList[pos]      = usedSpace / clusterAdrsInformation.meanIdletime[i];                

                fragmentCapacityList[pos] = numberOfFragments / capacityList[pos];
                fragmentTotalList[pos]    = numberOfFragments / totalSpaceList[pos];
                fragmentIdleList[pos]     = numberOfFragments / clusterAdrsInformation.meanIdletime[i];                

                pos++;
            }
        }

        /**
         * Evaluates statistical information about ADRs in the system
         */
        double meanCapacity   = StatisticsEvaluator.evaluateMean(capacityList);
        double meanTotalSpace = StatisticsEvaluator.evaluateMean(totalSpaceList);
        double meanFragments  = StatisticsEvaluator.evaluateMean(fragmentList);
        double meanUsedSpace = StatisticsEvaluator.evaluateMean(totalSpaceList);        

        double meanUsedCapacity = StatisticsEvaluator.evaluateMean(usedCapacityList);
        double meanUsedTotal    = StatisticsEvaluator.evaluateMean(usedTotalList);        
        double meanUsedIdle     = StatisticsEvaluator.evaluateMean(usedIdleList);

        double meanFragmentCapacity = StatisticsEvaluator.evaluateMean(fragmentCapacityList);
        double meanFragmentTotal    = StatisticsEvaluator.evaluateMean(fragmentTotalList);        
        double meanFragmentIdle     = StatisticsEvaluator.evaluateMean(fragmentIdleList);

        //-
        
        double stdDevCapacity   = StatisticsEvaluator.evaluateStdDev(capacityList, meanCapacity);
        double stdDevTotalSpace = StatisticsEvaluator.evaluateStdDev(totalSpaceList, meanTotalSpace);
        double stdDevFragments  = StatisticsEvaluator.evaluateStdDev(fragmentList, meanFragments);
        double stdDevUsedSpace = StatisticsEvaluator.evaluateStdDev(totalSpaceList, meanTotalSpace);        

        double stdDevUsedCapacity = StatisticsEvaluator.evaluateStdDev(usedCapacityList, meanUsedCapacity);
        double stdDevUsedTotal    = StatisticsEvaluator.evaluateStdDev(usedTotalList, meanUsedTotal);        
        double stdDevUsedIdle     = StatisticsEvaluator.evaluateStdDev(usedIdleList, meanUsedIdle);

        double stdDevFragmentCapacity = StatisticsEvaluator.evaluateStdDev(fragmentCapacityList, meanFragmentCapacity);
        double stdDevFragmentTotal    = StatisticsEvaluator.evaluateStdDev(fragmentTotalList, meanFragmentTotal);        
        double stdDevFragmentIdle     = StatisticsEvaluator.evaluateStdDev(fragmentIdleList, meanFragmentIdle);

        /**
         * Writes statistical information to output stream.
         */        
        outputStream.printf("ADR Info:\n");       
        outputStream.printf("maxCapacity:   %20.8f\n", maxCapacity);
        outputStream.printf("maxTotalSpace: %20.8f\n", maxStorageSpace);        

        outputStream.printf("meanCapacity:   %20.8f | %20.8f\n", meanCapacity,   stdDevCapacity);
        outputStream.printf("meanFragments:  %20.8f | %20.8f\n", meanFragments,  stdDevFragments);
        outputStream.printf("meanUsedSpace:  %20.8f | %20.8f\n", meanUsedSpace,  stdDevUsedSpace);
        outputStream.printf("meanTotalSpace: %20.8f | %20.8f\n", meanTotalSpace, stdDevTotalSpace);
        
        outputStream.printf("meanUsedSpace: %20.8f | %20.8f\n", meanUsedCapacity, stdDevUsedCapacity);
        outputStream.printf("meanUsedIdle:  %20.8f | %20.8f\n", meanUsedIdle,  stdDevUsedIdle);
        outputStream.printf("meanUsedTotal: %20.8f | %20.8f\n", meanUsedTotal, stdDevUsedTotal);

        outputStream.printf("meanFragmentCapacity: %20.10f | %20.10f\n", meanFragmentCapacity, stdDevFragmentCapacity);
        outputStream.printf("meanFragmentIdle:     %20.8f | %20.8f\n", meanFragmentIdle,  stdDevFragmentIdle);
        outputStream.printf("meanFragmentTotal:    %20.10f | %20.10f\n", meanFragmentTotal, stdDevFragmentTotal);
    }    
}
