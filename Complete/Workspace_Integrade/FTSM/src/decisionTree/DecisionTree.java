package decisionTree;
// DECISION TREE
// Frans Coenen
// Thursday 15 August 2002
// Department of Computer Science, University of Liverpool

import java.io.*;

class DecisionTree {

    /* ------------------------------- */
    /*                                 */
    /*              FIELDS             */
    /*                                 */
    /* ------------------------------- */

    /* NESTED CLASS */

    private class BinTree {
    	
	/* FIELDS */
	
	private int     nodeID;
    	private String  answer = null;
    	private BinTree yesBranch  = null;
    	private BinTree noBranch   = null;
    	private String variable = null;
    	private String comparison = null;
    	private String value = null;
    	private String ftec = null;
    	private String ftecConfigFile = null;
	
	/* CONSTRUCTOR */
	
	public BinTree(int newNodeID, String newQuestAns, String nodeVariable, String nodeComparison, String nodeValue, String chosenFtec, String chosenFtecConfigFile) {
	    nodeID = newNodeID;
	    answer = newQuestAns;
	    variable = nodeVariable;
	    value = nodeValue;
	    ftec = chosenFtec;
	    ftecConfigFile = chosenFtecConfigFile;
		}
	}

    /* OTHER FIELDS */

    static BufferedReader    keyboardInput = new
                           BufferedReader(new InputStreamReader(System.in));
    BinTree rootNode = null;

    /* ------------------------------------ */
    /*                                      */
    /*              CONSTRUCTORS            */
    /*                                      */
    /* ------------------------------------ */

    /* Default Constructor */

    public DecisionTree() {
	}

    /* ----------------------------------------------- */
    /*                                                 */
    /*               TREE BUILDING METHODS             */
    /*                                                 */
    /* ----------------------------------------------- */

    /* CREATE ROOT NODE */

    public void createRoot(int newNodeID, String nodeVariable, 
    						String nodeComparison, String nodeValue, String chosenFtec, String chosenFtecConfigFile) {
    String newQuestAns = "" + nodeVariable + " " + nodeComparison + " " + nodeValue;
	rootNode = new BinTree(newNodeID,newQuestAns,  nodeVariable, nodeComparison, nodeValue, chosenFtec, chosenFtecConfigFile);	
	System.out.println("Created root node " + newNodeID);	
	}
			
    /* ADD YES NODE */

    public void addYesNode(int existingNodeID, int newNodeID,
    					   	String nodeVariable, String nodeComparison, String nodeValue, String chosenFtec, String chosenFtecConfigFile) {
    	String newQuestAns = "" + nodeVariable + " " + nodeComparison + " " + nodeValue;
	    // If no root node do nothing
		
		if (rootNode == null) {
		    System.out.println("ERROR: No root node!");
		    return;
		    }
		
		// Search tree
		
		if (searchTreeAndAddYesNode(rootNode,existingNodeID,newNodeID,newQuestAns, 
				nodeVariable, nodeComparison, nodeValue, chosenFtec, chosenFtecConfigFile)) {
		    	System.out.println("Added node " + newNodeID +
		    		" onto \"yes\" branch of node " + existingNodeID);
		    }
		else System.out.println("Node " + existingNodeID + " not found");
	}

    /* SEARCH TREE AND ADD YES NODE */

    private boolean searchTreeAndAddYesNode(BinTree currentNode,
    			int existingNodeID, int newNodeID, String newQuestAns, String nodeVariable, String nodeComparison, String nodeValue, String chosenFtec, String chosenFtecConfigFile) {
    	
    	if (currentNode.nodeID == existingNodeID) {
		    // Found node
		    if (currentNode.yesBranch == null) currentNode.yesBranch = new
		    		BinTree(newNodeID,newQuestAns, nodeVariable, nodeComparison, nodeValue, chosenFtec, chosenFtecConfigFile);
		    else {
		        System.out.println("WARNING: Overwriting previous node " +
				"(id = " + currentNode.yesBranch.nodeID +
				") linked to yes branch of node " +
				existingNodeID);
			currentNode.yesBranch = new BinTree(newNodeID,newQuestAns, nodeVariable, nodeComparison, nodeValue, chosenFtec, chosenFtecConfigFile);
			}		
	    	    return(true);
	    }
		else {
		    // Try yes branch if it exists
		    if (currentNode.yesBranch != null) { 	
		        if (searchTreeAndAddYesNode(currentNode.yesBranch,
			        	existingNodeID,newNodeID,newQuestAns, nodeVariable, nodeComparison, nodeValue, chosenFtec, chosenFtecConfigFile)) {    	
		            return(true);
			    }	
			else {
	    	        // Try no branch if it exists
		    	    if (currentNode.noBranch != null) {
	    	    		return(searchTreeAndAddYesNode(currentNode.noBranch,
					existingNodeID,newNodeID,newQuestAns, nodeVariable, nodeComparison, nodeValue, chosenFtec, chosenFtecConfigFile));
		    	    }
		    	    else return(false);	// Not found here
			    }
    		}
		    return(false);		// Not found here
	    }
   	} 	
    		
    /* ADD NO NODE */

    public void addNoNode(int existingNodeID, int newNodeID, String nodeVariable, String nodeComparison, String nodeValue, String chosenFtec, String chosenFtecConfigFile) {
	// If no root node do nothing
	if (rootNode == null) {
	    System.out.println("ERROR: No root node!");
	    return;
	    }
	
	// Search tree
	String newQuestAns = "" + nodeVariable + " " + nodeComparison + " " + nodeValue;
	if (searchTreeAndAddNoNode(rootNode,existingNodeID,newNodeID,newQuestAns, nodeVariable, nodeComparison, nodeValue, chosenFtec, chosenFtecConfigFile)) {
	    System.out.println("Added node " + newNodeID +
	    		" onto \"no\" branch of node " + existingNodeID);
	    }
	else System.out.println("Node " + existingNodeID + " not found");
	}
	
    /* SEARCH TREE AND ADD NO NODE */

    private boolean searchTreeAndAddNoNode(BinTree currentNode,
    			int existingNodeID, int newNodeID, String newQuestAns, String nodeVariable, String nodeComparison, String nodeValue, String chosenFtec, String chosenFtecConfigFile) {
    	if (currentNode.nodeID == existingNodeID) {
	    // Found node
	    if (currentNode.noBranch == null) currentNode.noBranch = new
	    		BinTree(newNodeID,newQuestAns, nodeVariable, nodeComparison, nodeValue, chosenFtec, chosenFtecConfigFile);
	    else {
	        System.out.println("WARNING: Overwriting previous node " +
			"(id = " + currentNode.noBranch.nodeID +
			") linked to yes branch of node " +
			existingNodeID);
		currentNode.noBranch = new BinTree(newNodeID,newQuestAns, nodeVariable, nodeComparison, nodeValue, chosenFtec, chosenFtecConfigFile);
		}		
    	    return(true);
	    }
	else {
	    // Try yes branch if it exists
	    if (currentNode.yesBranch != null) { 	
	        if (searchTreeAndAddNoNode(currentNode.yesBranch,
		        	existingNodeID,newNodeID,newQuestAns, nodeVariable, nodeComparison, nodeValue, chosenFtec, chosenFtecConfigFile)) {    	
	            return(true);
		    }	
		else {
    	        // Try no branch if it exists
	    	    if (currentNode.noBranch != null) {
    	    		return(searchTreeAndAddNoNode(currentNode.noBranch,
				existingNodeID,newNodeID,newQuestAns, nodeVariable, nodeComparison, nodeValue, chosenFtec, chosenFtecConfigFile));
			}
		    else return(false);	// Not found here
		    }
		 }
	    else return(false);	// Not found here
	    }
   	} 	

    /* --------------------------------------------- */
    /*                                               */
    /*               TREE QUERY METHODS             */
    /*                                               */
    /* --------------------------------------------- */

    public void queryBinTree() throws IOException {
        queryBinTree(rootNode);
        }

    private void queryBinTree(BinTree currentNode) throws IOException {

        // Test for leaf node (answer) and missing branches

        if (currentNode.yesBranch==null) {
            if (currentNode.noBranch==null) System.out.println("FTEC Escolhido: " + currentNode.ftec + "\nConfigFile: " + currentNode.ftecConfigFile);
            else System.out.println("Error: Missing \"Yes\" branch at \"" +
            		currentNode.answer + "\" question");
            return;
            }
        if (currentNode.noBranch==null) {
            System.out.println("Error: Missing \"No\" branch at \"" +
            		currentNode.answer + "\" question");
            return;
            }

        // Question

        askQuestion(currentNode);
        }

    private void askQuestion(BinTree currentNode) throws IOException {
        System.out.println(currentNode.answer + " (enter \"Yes\" or \"No\")");
        String answer = keyboardInput.readLine();
        if (answer.equals("Yes")) queryBinTree(currentNode.yesBranch);
        else {
            if (answer.equals("No")) queryBinTree(currentNode.noBranch);
            else {
                System.out.println("ERROR: Must answer \"Yes\" or \"No\"");
                askQuestion(currentNode);
                }
            }
        }

    /* ----------------------------------------------- */
    /*                                                 */
    /*               TREE OUTPUT METHODS               */
    /*                                                 */
    /* ----------------------------------------------- */

    /* OUTPUT BIN TREE */

    public void outputBinTree() {

        outputBinTree("1",rootNode);
        }

    private void outputBinTree(String tag, BinTree currentNode) {

        // Check for empty node

        if (currentNode == null) return;

        // Output
        System.out.println("[" + tag + "] nodeID = " + currentNode.nodeID +
        		", question/answer = " + currentNode.answer);
        		
        // Go down yes branch

        outputBinTree(tag + ".1",currentNode.yesBranch);

        // Go down no branch

        outputBinTree(tag + ".2",currentNode.noBranch);
	}      		
    }

