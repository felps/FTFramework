package br.usp.ime.oppstore.simulation;

import java.io.Serializable;
import java.net.InetSocketAddress;

public class CdrmBootstrapInformation implements Serializable {
    private static final long serialVersionUID = -2077284039615033479L;

    public InetSocketAddress bootstrapNodeAddress;
    //public int nextCdrmNumber;
}
