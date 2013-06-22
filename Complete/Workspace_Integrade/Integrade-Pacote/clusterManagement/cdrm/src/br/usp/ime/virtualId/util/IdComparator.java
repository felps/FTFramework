package br.usp.ime.virtualId.util;

import java.io.Serializable;
import java.util.Comparator;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Id.Distance;

public class IdComparator implements Comparator<Id>, Serializable {
    
    private static final long serialVersionUID = -83269521032831437L;
    
    private Id baseId = null;
    
    public IdComparator ( Id baseId ) {
        this.baseId = baseId;
    }
    
    public void setBaseId( Id baseId ) {
        this.baseId = baseId;
    }
    
    public Id getBaseId(  ) {
        return this.baseId;
    }

    
    public int compare(Id id1, Id id2) {       

    	Distance dist1;	
    	if (id1.clockwise(baseId) == false) dist1 = baseId.distanceFromId(id1);
    	else dist1 = baseId.longDistanceFromId(id1);
    	
    	Distance dist2;
    	if (id2.clockwise(baseId) == false) dist2 = baseId.distanceFromId(id2);
    	else dist2 = baseId.longDistanceFromId(id2);
        
        return dist1.compareTo(dist2);           
    }
    
    public static int compare(Id baseId, Id id1, Id id2) {       

    	Distance dist1;	
    	if (id1.clockwise(baseId) == false) dist1 = baseId.distanceFromId(id1);
    	else dist1 = baseId.longDistanceFromId(id1);
    	
    	Distance dist2;
    	if (id2.clockwise(baseId) == false) dist2 = baseId.distanceFromId(id2);
    	else dist2 = baseId.longDistanceFromId(id2);
        
        return dist1.compareTo(dist2);           
    }

    /**
     * Returns true if leftId <= centerId <= rightId
     */
    public static boolean isBetween(Id leftId, Id centerId, Id rightId) {
        if ( IdComparator.compare(leftId, centerId, rightId) <= 0 )
            return true;
        else
            return false;
    }
    
    /**
     * Returns true if leftId <= centerId < rightId
     */
    public static boolean isBetweenExcludeRight(Id leftId, Id centerId, Id rightId) {
        if ( IdComparator.compare(leftId, centerId, rightId) < 0 )
            return true;
        else
            return false;
    }
    
    /**
     * Returns true if leftId < centerId <= rightId
     */
    public static boolean isBetweenExcludeLeft(Id leftId, Id centerId, Id rightId) {
        if ( IdComparator.compare(leftId, centerId, rightId) <= 0 && leftId.equals( centerId ) == false )
            return true;
        else
            return false;
    }
    
    /**
     * Returns true if leftId < centerId <= rightId
     */
    public static boolean isBetweenExcludeLeftRight(Id leftId, Id centerId, Id rightId) {
        if ( IdComparator.compare(leftId, centerId, rightId) < 0 && leftId.equals( centerId ) == false )
            return true;
        else
            return false;
    }

}
