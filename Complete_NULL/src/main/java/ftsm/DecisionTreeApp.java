// DECISION TREE  APPLICATION
// Frans Coenen
// Thursday 15 August 2002
// Department of Computer Science, University of Liverpool
package ftsm;

import java.io.*;


public class DecisionTreeApp {

    /* ------------------------------- */
    /*                                 */
    /*              FIELDS             */
    /*                                 */
    /* ------------------------------- */

    static BufferedReader keyboardInput = new
                           BufferedReader(new InputStreamReader(System.in));
    static DecisionTree newTree;

    /* --------------------------------- */
    /*                                   */
    /*               METHODS             */
    /*                                   */
    /* --------------------------------- */

    /* MAIN */

    public static void main(String[] args) throws IOException {

        // Create instance of class DecisionTree
        YamlParser yaml = new YamlParser();
        newTree = yaml.parseYaml("decisionTree.yaml");

        // Output tree

        System.out.println("\nOUTPUT DECISION TREE");
        System.out.println("====================");
        newTree.outputBinTree();

        // Query tree

        queryTree();
        }

    public static String getFtecName(String decisionTreePath) throws IOException {

    	/*
        // Create instance of class DecisionTree
        YamlParser yaml = new YamlParser();
        newTree = yaml.parseYaml(decisionTreePath);

        // Output tree

        System.out.println("\nOUTPUT DECISION TREE");
        System.out.println("====================");
        newTree.outputBinTree();

        // Query tree

        return queryTree();
        */
    	return "NULL";
    }
    /* GENERATE TREE */

    static void generateTree() {
        System.out.println("\nGENERATE DECISION TREE");
        System.out.println("======================");
        /*newTree.createRoot(1,"Animal", "eat", "meat");
        newTree.addYesNode(1,2,"Animal", "have", "stripes");
        newTree.addNoNode (1,3,"Animal",  "have" , "stripes");
        newTree.addYesNode(2,4,"Animal", "is",  "a Tiger");
        newTree.addNoNode (2,5,"Animal", "is", "a Leopard");
        newTree.addYesNode(3,6,"Animal", "is", " a Zebra");
        newTree.addNoNode (3,7,"Animal", "is ", "a Horse");*/
        }

    /* QUERY TREE */
    static String queryTree() throws IOException {
        System.out.println("\nQUERY DECISION TREE");
        System.out.println("===================");
        return newTree.queryBinTree();

        // Option to exit

        //optionToExit();
        }

    /* OPTION TO EXIT PROGRAM */

    static void optionToExit() throws IOException {
        System.out.println("Exit? (enter \"Yes\" or \"No\")");
        String answer = keyboardInput.readLine();
        if (answer.equals("Yes")) return;
        else {
            if (answer.equals("No")) queryTree();
            else {
                System.out.println("ERROR: Must answer \"Yes\" or \"No\"");
                optionToExit();
                }
            }
        }
    }
