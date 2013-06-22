package decisionTree;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;

import org.yaml.*;
import org.yaml.snakeyaml.Yaml;

import decisionTree.DecisionTree;

public class YamlParser {
	
	private DecisionTree tree;

	public DecisionTree parseYaml(String path) {
		this.tree = new DecisionTree();
		
		try
		{
			InputStream input = new FileInputStream(new File(
            path));
			loadManyDocuments(input);
    
		} 
        catch (FileNotFoundException e)
		{
			System.err.println("File Not Found");
		}
        return tree;
	}

	public void loadManyDocuments(InputStream input){
        Yaml yaml = new Yaml();
        System.out.println("LoadManyDocuments");
        for (Object data : yaml.loadAll(input)) {
        	
        	System.out.println(data);
        	System.out.println(yaml.dump(data));

        	@SuppressWarnings("unchecked")        	
        	Map map = (Map) data;
        	
        	String	nodeIdStr				= "" + map.get("nodeId");
            String	parentAnswered 			= "" + map.get("parentNodeAnswered");
            String	nodeVariable			= "" + map.get("nodeVariable");
    		String	nodeComparison			= "" + map.get("nodeComparison");
    		String	nodeValue				= "" + map.get("nodeValue");
    		String	chosenFtec				= "" + map.get("FtecChoice");
    		String	chosenFtecConfigFile	= "" + map.get("FtecConfigFile");

    		int nodeId = Integer.parseInt(nodeIdStr);
    		System.out.println(nodeId + " " + parentAnswered + " " + nodeVariable + " " + nodeComparison + " " + nodeValue);
    		
    		//If its root
    		if(parentAnswered.contentEquals("null")){
    			this.tree.createRoot(nodeId, nodeVariable, nodeComparison, nodeValue, null, null);
    		} else {
                int	parentNode = Integer.parseInt("" + map.get("parentNode"));
                
    			if (parentAnswered.equalsIgnoreCase("true")){
    				this.tree.addYesNode(parentNode, nodeId,nodeVariable, nodeComparison, nodeValue, chosenFtec, chosenFtecConfigFile);
    			}
    			else{
    				this.tree.addNoNode(parentNode,nodeId,nodeVariable, nodeComparison, nodeValue, chosenFtec, chosenFtecConfigFile);
    			}
    		}
        }
    }

}
