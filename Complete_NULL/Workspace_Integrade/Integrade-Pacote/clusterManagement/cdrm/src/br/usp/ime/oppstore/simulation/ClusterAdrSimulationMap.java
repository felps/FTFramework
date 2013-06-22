package br.usp.ime.oppstore.simulation;

import java.util.HashMap;

import br.usp.ime.oppstore.simulation.adr.ClusterAdrs;

public class ClusterAdrSimulationMap {

	private static HashMap< String, ClusterAdrs> adrMap = new HashMap<String, ClusterAdrs>();
		
	public static void addClusterAdr (String address, ClusterAdrs clusterAdrs) {
		adrMap.put(address, clusterAdrs);
	}
	
	public static ClusterAdrs getClusterAdr (String address) {
		return adrMap.get( address );
	}
}
