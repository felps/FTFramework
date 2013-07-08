package br.usp.ime.virtualId;

import java.io.Serializable;
import java.util.Vector;

import br.usp.ime.virtualId.util.IdComparator;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;

public class VirtualIdUpdates implements Serializable {
    
    private static final long serialVersionUID = -263316514162785934L;
 
    private Vector<Id> oldIdList = new Vector<Id>();
    private Vector<Id> newIdList = new Vector<Id>();
    private Vector<NodeHandle> nodeHandleList = new Vector<NodeHandle>();
    
    private boolean wrappingUpdates = false;
    
    public void setWrappingUpdates(boolean value) { wrappingUpdates = value;}
    public boolean getWrappingUpdates() {return wrappingUpdates;};
    
    public void addUpdate(Id oldId, Id newId, NodeHandle nodeHandle) { 
        oldIdList.add(oldId);
        newIdList.add(newId);
        nodeHandleList.add(nodeHandle);
    }
    
    public int numberOfUpdates() {
        return oldIdList.size();
    }
    
    public Id getOldId(int index) { return oldIdList.get(index % numberOfUpdates()); }
    public Id getNewId(int index) { return newIdList.get(index % numberOfUpdates()); }
    public NodeHandle getNodeHandle(int index) { return nodeHandleList.get(index % numberOfUpdates()); }

    public int getLeftLeafSetIndex (Id leafId) {
    	
        for (int i=0; i < newIdList.size()-1; i++)
            if ( IdComparator.isBetweenExcludeRight( newIdList.get(i), leafId, newIdList.get(i+1) ) )
                return i;
        
        if ( wrappingUpdates )
            if ( IdComparator.isBetweenExcludeRight( newIdList.get( newIdList.size()-1 ), leafId, newIdList.get( 0 ) ) )
                return newIdList.size()-1;
        	
        return -1;
    	
    }

    public int getRightLeafSetIndex (Id leafId) {

//        if ( wrappingUpdates ) {
//        	int rightIndex = getRightLeafSetIndex(leafId);
//            if ( rightIndex >= 0 )
//                return rightIndex + numberOfUpdates();
//            else
//            	return -1;
//        }

        for (int i=0; i < newIdList.size()-1; i++)
            if ( IdComparator.isBetweenExcludeLeft( newIdList.get(i), leafId, newIdList.get(i+1) ) )
                return i+1;
        
        if ( wrappingUpdates )
            if ( IdComparator.isBetweenExcludeLeft( newIdList.get( newIdList.size()-1 ), leafId, newIdList.get( 0 ) ) )
                return 0;
        	
        return -1;
    	
    }

    public int getIndexNewId(Id newId) {
        
        for (int i=0; i < newIdList.size(); i++) {
            if ( newIdList.get(i).equals(newId) )
                return i;
        }
        return -1;
    }

    public int getIndexOldId(Id oldId) {
        
        for (int i=0; i < oldIdList.size(); i++) {
            if ( oldIdList.get(i).equals(oldId) )
                return i;
        }
        return -1;
    }

    public int getIndexHandle(NodeHandle nodeHandle) {
        
        for (int i=0; i < nodeHandleList.size(); i++) {
            if ( nodeHandleList.get(i).getId().equals(nodeHandle.getId()) )
                return i;
        }
        return -1;
    }
    
    public String toString() {
     
        StringBuffer output = new StringBuffer();
        output.append("VirtualIdUpdates:\n");
        output.append("newIds  ->");
        for (int i=0; i < numberOfUpdates(); i++)
            output.append( i + ": " + getNewId(i).toString() + " ");
        output.append("\noldIds  ->");
        for (int i=0; i < numberOfUpdates(); i++)
            output.append( i + ": " + getOldId(i).toString() + " ");
        output.append("\nhandles ->");
        for (int i=0; i < numberOfUpdates(); i++)
            output.append( i + ": " + getNodeHandle(i).getId().toString() + " ");
        
        return output.toString();
    }
}
