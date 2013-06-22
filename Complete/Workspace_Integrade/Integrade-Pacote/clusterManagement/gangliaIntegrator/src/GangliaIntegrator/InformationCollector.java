package GangliaIntegrator;




import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;






public class InformationCollector {
	
	private static final int KBYTE = 1024;
	
	
	private static int ConvertToBytes(double data, String unit){
		
		if(unit == "B")
			return (int)data;
		else if(unit == "KB")
			return (int)(data * KBYTE);
		else if(unit == "MB")
			return (int)(data * KBYTE * KBYTE);
		else if(unit == "GB")
			return (int)(data * KBYTE * KBYTE * KBYTE);
		else//TODO: don't know unit, return original value
			return (int)data;
	}
	
	
	public static HostInformation [] Collect(byte [] data) throws Exception{
		
		//Open socket and get data
		//Load data into XML document and XPATH it
		//Prepare info to insert into trader
		
		ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
			
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(byteStream);
		
		 XPathFactory xPathFactory = XPathFactory.newInstance();
		    XPath xpath = xPathFactory.newXPath();
		    XPathExpression clusterQuery 
		     = xpath.compile("/GANGLIA_XML/GRID/CLUSTER[@NAME='" + 
		    		 GangliaIntegratorLauncher.GetProperty(GangliaIntegratorLauncher.CLUSTER_NAME) + "']");
		    
		    XPathExpression nodeQuery =
		    	xpath.compile("HOST");
		    
		    XPathExpression metricQuery =
		    	xpath.compile("METRIC");
		    	
		    
 
		   NodeList clusterNodeList  = 
			   (NodeList)clusterQuery.evaluate(doc, XPathConstants.NODESET);
		   
		   //TODO: can queries return null node sets?
		   
		   ArrayList<HostInformation> nodeInformationArray = new ArrayList<HostInformation>(); 
		   
		   
		  for(int i = 0; i < clusterNodeList.getLength(); i++){
			  
			  NodeList hostNodeList = 
				  (NodeList) nodeQuery.evaluate(clusterNodeList.item(i), XPathConstants.NODESET);
			  
			  for(int j = 0; j < hostNodeList.getLength(); j++){
				  
				  HostInformation hostInformation =
					  new HostInformation();
				  
				  hostInformation.hostName(hostNodeList.item(j).getAttributes().getNamedItem("NAME").getNodeValue());
				  hostInformation.ipAddress(hostNodeList.item(j).getAttributes().getNamedItem("IP").getNodeValue());
				  hostInformation.lastUpdated(Integer.parseInt(hostNodeList.item(j).getAttributes().getNamedItem("REPORTED").getNodeValue()));
			
				  NodeList metricNodeList =
					  (NodeList) metricQuery.evaluate(hostNodeList.item(j), XPathConstants.NODESET);
				  
				  for(int k = 0; k < metricNodeList.getLength(); k++){
					  
					  NamedNodeMap attributesMap =
						  metricNodeList.item(k).getAttributes();
					  
					  String metricName = attributesMap.getNamedItem("NAME").getNodeValue();
					  String metricValue = attributesMap.getNamedItem("VAL").getNodeValue();
					  String metricUnit = attributesMap.getNamedItem("UNITS").getNodeValue();
					  
//					  !lrmIor         OK
//					  XhostName       Elemento externo "host", atributo "NAME"
//					  XosName         os_name
//					  XosVersion      os_release
//					  !processorName  N/A 
//					  XprocessorMhz   cpu_speed
//					  XtotalRam       mem_total
//					  XtotalSwap      swap_total
//					  XfreeRam        mem_free
//					  XfreeSwap       swap_free
//					  XfreeDiskSpace  disk_free
//					  XcpuUsage       N/A (Mas talvez de para tirar de cpu_idle)
//					  !recentlyPicked OK
//					  XlastUpdated    Elemento externo "host", atributo "REPORTED"
					  
					  
					  
					  if(metricName.equals("os_name"))
						  hostInformation.osName(metricValue);
					  else if(metricName.equals("os_release"))
						  hostInformation.osVersion(metricValue);
					  else if(metricName.equals("machine_type"))
						  hostInformation.processorName(metricValue);
					  else if(metricName.equals("cpu_speed"))
						  hostInformation.processorMhz(Integer.parseInt(metricValue));
					  else if(metricName.equals("mem_total"))
						  hostInformation.totalRam(ConvertToBytes(Integer.parseInt(metricValue), metricUnit)); 
					  else if(metricName.equals("swap_total"))
						  hostInformation.totalSwap(ConvertToBytes(Integer.parseInt(metricValue), metricUnit));
					  else if(metricName.equals("mem_free"))
						  hostInformation.freeRam(ConvertToBytes(Integer.parseInt(metricValue), metricUnit));
					  else if(metricName.equals("swap_free"))
						  hostInformation.freeSwap(ConvertToBytes(Integer.parseInt(metricValue), metricUnit));
					  else if(metricName.equals("disk_free"))
						  hostInformation.freeDiskSpace(ConvertToBytes(Double.parseDouble(metricValue), metricUnit));
					  else if(metricName.equals("cpu_idle"))
						  hostInformation.cpuUsage(100 - Float.parseFloat(metricValue));
				  }
				  
				  nodeInformationArray.add(hostInformation);
				  
			  }
		  }
		  
		  HostInformation [] value = new HostInformation [nodeInformationArray.size()];
		  nodeInformationArray.toArray(value);
		  return value;
		  
	}
}
	
