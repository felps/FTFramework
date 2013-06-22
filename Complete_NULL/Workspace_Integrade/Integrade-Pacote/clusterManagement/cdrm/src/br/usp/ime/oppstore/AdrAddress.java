package br.usp.ime.oppstore;

import java.io.Serializable;

import br.usp.ime.oppstore.simulation.adr.ClusterAdrs;

public class AdrAddress implements Serializable {
   private static final long serialVersionUID = 2232277784047053400L;
   
   public ClusterAdrs clusterAdrStub;
   public String address;    
}
