package br.usp.ime.virtualId.test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.log4j.PropertyConfigurator;

import rice.environment.Environment;
import rice.pastry.NodeIdFactory;
import rice.pastry.standard.RandomNodeIdFactory;

public class VirtualSpaceTesterLauncher {

    /**
     * Main method.
     */
    public static void main(String[] args) {
                
        int localBindPort = 9001; // 9006
        //String bootstrapNodeIp = "143.107.45.64";
        //String bootstrapNodeIp = "192.168.155.2";
        String bootstrapNodeIp = "127.0.0.1";
        int bootstrapPort = 9001;
        
        int numberOfNodes = 20;
        int numberOfMessages = 100;
        int numberOfVirtualSpaces = 1;        
        
        /** 
         * Configures logging
         */
        //BasicConfigurator.configure();
        PropertyConfigurator.configure(args[0]);                

        /**
         * Configures pastry environment
         */
        Environment env = new Environment();
        //if (numberOfNodes < env.getParameters().getInt("pastry_lSetSize"))
        env.getParameters().setInt("pastry_lSetSize", 8);
        //System.out.println(env.getParameters().getInt("pastry_lSetSize"));

        InetSocketAddress bootstrapNodeAddress = null;
        try { 
            bootstrapNodeAddress =  new InetSocketAddress(InetAddress.getByName(bootstrapNodeIp), bootstrapPort); 
        } 
        catch (UnknownHostException e1) { e1.printStackTrace(); }
        
        NodeIdFactory nodeIdFactory = new RandomNodeIdFactory(env);
        
        TestNodeManager testNodeManager = TestNodeManager.createInstance(env, localBindPort, nodeIdFactory);        
        testNodeManager.createTestNodes(bootstrapNodeAddress, numberOfNodes, numberOfVirtualSpaces);
        //testNodeManager.createTestNodes(bootstrapNodeAddress, numberOfNodes, numberOfVirtualSpaces);
        testNodeManager.launchRoutingTests(numberOfMessages);        
    }
}
