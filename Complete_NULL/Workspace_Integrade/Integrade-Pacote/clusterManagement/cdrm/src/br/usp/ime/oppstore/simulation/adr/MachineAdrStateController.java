package br.usp.ime.oppstore.simulation.adr;

import java.util.LinkedList;
import java.util.Vector;

import br.usp.ime.oppstore.simulation.adr.ClusterAdrSimulator.AdrState;

public class MachineAdrStateController {
    
//    double currentTime;
//    
//    /**
//     * key: experiment number
//     */
//    Vector< LinkedList<AdrEvent> > adrExperimentEventsList;
//    
//    public MachineAdrStateController () {
//        this.adrExperimentEventsList = new Vector< LinkedList<AdrEvent> >();
//        this.currentTime = 0.0;
//    }
//           
//    public void readAdrFailureTimes(Adr adr, int experimentNumber, String events) {
//        String[] adrEventStringList = events.split("[:|]");
//        LinkedList<AdrEvent> adrEventLinkedList = new LinkedList<AdrEvent>(); 
//        
//        for (int i=0; i<adrEventStringList.length; i+=2) {
//            
//            AdrState newState = null;
//            if (adrEventStringList[i].compareTo("I") == 0)
//                newState = AdrState.IDLE;
//            else if (adrEventStringList[i].compareTo("O") == 0)
//                newState = AdrState.OCCUPIED;
//            else if (adrEventStringList[i].compareTo("U") == 0)
//                newState = AdrState.UNAVAILABLE;
//            else
//                System.out.println("readAdrFailureTimes: Wrong file format.");
//            
//            double eventTime = 0;
//            try { eventTime = Double.valueOf(adrEventStringList[i+1]); }
//            catch (NumberFormatException e) {
//                System.out.println("readAdrFailureTimes: Wrong file format.");
//            }
//
//            adrEventLinkedList.add( new AdrEvent(eventTime, adr, newState) );
//        }
//        
//        if (adrExperimentEventsList.size() <= experimentNumber)
//            adrExperimentEventsList.setSize(experimentNumber+1);
//        
//        adrExperimentEventsList.set(experimentNumber, adrEventLinkedList);        
//    }
//    
//    public void changeMachineStates(double timeStep) {
//        double maxTime = currentTime + timeStep;
//        for (int experiment=0; experiment < adrExperimentEventsList.size(); experiment++ ) {
//            LinkedList<AdrEvent> adrEventList = adrExperimentEventsList.get(experiment);                         
//            while (adrEventList.peek().eventTime <= maxTime) {
//                AdrEvent adrEvent = adrEventList.poll();
//                adrEvent.adr.setAdrState(experiment, adrEvent.newAdrState);
//            }
//        }        
//    }     
    
}
