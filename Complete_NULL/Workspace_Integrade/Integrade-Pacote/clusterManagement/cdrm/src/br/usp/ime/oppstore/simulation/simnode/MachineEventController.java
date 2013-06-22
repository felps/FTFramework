package br.usp.ime.oppstore.simulation.simnode;

import br.usp.ime.oppstore.simulation.adr.MachineAdrStateController;
import br.usp.ime.oppstore.simulation.cdrm.CdrmEvent;
import br.usp.ime.oppstore.simulation.cdrm.MachineCdrmStateController;
import br.usp.ime.oppstore.simulation.cdrm.CdrmEvent.CdrmState;

public class MachineEventController implements MachineEventControllerRemote {

    MachineAdrStateController adrStateController;
    MachineCdrmStateController cdrmStateController;

    public MachineEventController(MachineAdrStateController adrStateController,
            MachineCdrmStateController cdrmStateController) {
        this.adrStateController  = adrStateController;
        this.cdrmStateController = cdrmStateController;
    }

    public void dispatchNextEvents(double timeStep) {
        //adrStateController.changeMachineStates(timeStep);
        //cdrmStateController.processNextCdrmEvents(timeStep);
    }

    public void addCdrmEventList(String events, MachineCdrmAdrManager cdrmManager, int cdrmIndex) {
        
        String[] eventStringList = events.split("[ :]");
        for (int i=0; i<eventStringList.length; i+=2) {
            CdrmState newState = null;
            double eventTime   = 0;
            
            if (eventStringList[i].compareTo("d") == 0)
                newState = CdrmState.DEPART;
            else
                System.out.println("LocalCdrmEventController: Wrong file format.");

            try {
                eventTime = Double.valueOf(eventStringList[i+1]);
            }
            catch (NumberFormatException e) {
                System.out.println("LocalCdrmEventController: Wrong file format.");
            }
            
            CdrmEvent cdrmEvent = new CdrmEvent(eventTime, cdrmManager, cdrmIndex, newState);            
            //eventController.addEvent(cdrmEvent);            
        }
    }

}
