package ftsm;

import java.util.HashSet;
import java.util.Iterator;

public class ResourceManagement {
	
	private HashSet<Resource> availableNodes = null;
	
	public ResourceManagement() {
		availableNodes = new HashSet<Resource>();
		
		newNode("no 1", "Linux", "2.6", "i686",
				200, 2000, 4000, 1000, 
				4000, 40000, 32);
		newNode("no 2", "Linux", "2.6", "i686",
				200, 2000, 4000, 1000, 
				4000, 40000, 32);
		newNode("no 3", "Linux", "2.6", "i686",
				200, 2000, 4000, 1000, 
				4000, 40000, 32);
		newNode("no 4", "Linux", "2.6", "i686",
				200, 2000, 4000, 1000, 
				4000, 40000, 32);
		newNode("no 5", "Linux", "2.6", "i686",
				200, 2000, 4000, 1000, 
				4000, 40000, 32);
		newNode("no 6", "Linux", "2.6", "i686",
				200, 2000, 4000, 1000, 
				4000, 40000, 32);
	}
	
	public class Resource {
		public String	hostName;
		public String	osName;
		public String	osVersion;
		public String	processorName;
		public long		processorMhz;
		public long		totalRam;
		public long		totalSwap;
		public long		freeRam;
		public long		freeSwap;
		public long		freeDiskSpace;
		public int		cpuUsage;

	}
	public static void dumpNodeStaticInformation(Resource node) {
		System.out.println("Hostname: " + node.hostName);
		System.out.println("OS Name: " + node.osName);
		System.out.println("OS Version: " + node.osVersion);
		System.out.println("Processor Name: " + node.processorName);
		System.out.println("Processor Mhz: " + node.processorMhz);
		System.out.println("Total RAM: " + node.totalRam);
		System.out.println("Total Swap: " + node.totalSwap);
	}

	public static void dumpNodeDynamicInformation(Resource node) {
		System.out.println("Free RAM: " + node.freeRam);
		System.out.println("Free Swap: " + node.freeSwap);
		System.out.println("Free Disk Space: " + node.freeDiskSpace);
		System.out.println("CPU Usage: " + node.cpuUsage);
	}	
	
	public void newNode(String hostName, String osName, String osVersion, String processorName,
						long processorMhz, long totalRam, long totalSwap, long freeRam, 
						long freeSwap, long freeDiskSpace, int cpuUsage) {
		
		Resource node = new Resource();
		node.hostName = hostName ;
		node.osName = osName ;
		node.osVersion = osVersion ;
		node.processorName = processorName ;
		node.processorMhz = processorMhz ;
		node.totalRam = totalRam ;
		node.totalSwap = totalSwap ;
		node.freeRam = freeRam ;
		node.freeSwap = freeSwap ;
		node.freeDiskSpace = freeDiskSpace ;
		node.cpuUsage = cpuUsage ;

	}
	
	public int availableNodesWith(String spec, String value, String operator) {
		HashSet<Resource> nodes = availableNodes;
		
		int counter = 0;
		
		if(operator.equalsIgnoreCase("is")) {
			for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
				Resource resource = (Resource) iterator.next();
				
				if (isSpecSameAsValue(spec, value, resource)) counter++;
				else nodes.remove(resource);
			}
		}
		if(operator.equalsIgnoreCase("less")) {
			for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
				Resource resource = (Resource) iterator.next();
				
				if (isSpecLessThanValue(spec, value, resource)) counter++;
				else nodes.remove(resource);
			}
		}
		if(operator.equalsIgnoreCase("more")) {
			for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
				Resource resource = (Resource) iterator.next();
		
				if (isSpecMoreThanValue(spec, value, resource)) counter++;
				else nodes.remove(resource);
			}
		}
		return counter;
	}
	
	public static boolean isSpecSameAsValue(String spec, String value, Resource resource) {
			
		if(spec.equalsIgnoreCase("osName")) {
			if (resource.osName.equalsIgnoreCase(value))
				return true;
		}
		
		if(spec.equalsIgnoreCase("osVersion")) {
			if (resource.osVersion.equalsIgnoreCase(value))
				return true;
		}
		
		if(spec.equalsIgnoreCase("processorName")) {
			if (resource.processorName.equalsIgnoreCase(value))
				return true;
		}
		
		if(spec.equalsIgnoreCase("processorMhz")) {
			String specStr = "" + resource.processorMhz;
			if (specStr.equalsIgnoreCase(value))
				return true;
		}
		
		if(spec.equalsIgnoreCase("freeDiskSpace")) {
			String specStr = ""+resource.freeDiskSpace;
			if (specStr.equalsIgnoreCase(value))
				return true;
		}
		
		if(spec.equalsIgnoreCase("freeRam")) {
			String specStr = ""+resource.freeRam;
			if (specStr.equalsIgnoreCase(value))
				return true;
		}
		
		if(spec.equalsIgnoreCase("freeSwap")) {
			String specStr = ""+resource.freeSwap;
			if (specStr.equalsIgnoreCase(value))
				return true;
		}
		
		if(spec.equalsIgnoreCase("totalRam")) {
			String specStr = ""+resource.totalRam;
			if (specStr.equalsIgnoreCase(value))
				return true;
		}
		
		if(spec.equalsIgnoreCase("totalSwap")) {
			String specStr = ""+resource.freeDiskSpace;
			if (specStr.equalsIgnoreCase(value))
				return true;
		}
		
		return false;
	}

	public static boolean isSpecLessThanValue(String spec, String value, Resource resource) {

		if(spec.equalsIgnoreCase("processorMhz")) {
			if (resource.processorMhz < Long.parseLong(value))
				return true;
		}
		
		if(spec.equalsIgnoreCase("freeDiskSpace")) {
			if (resource.freeDiskSpace  < Long.parseLong(value))
				return true;
		}
		
		if(spec.equalsIgnoreCase("freeRam")) {
			if (resource.freeRam < Long.parseLong(value))
				return true;
		}
		
		if(spec.equalsIgnoreCase("freeSwap")) {
			if (resource.freeSwap < Long.parseLong(value))
				return true;
		}
		
		if(spec.equalsIgnoreCase("totalRam")) {
			if (resource.totalRam < Long.parseLong(value))
				return true;
		}
		
		if(spec.equalsIgnoreCase("totalSwap")) {
			
			if (resource.freeDiskSpace < Long.parseLong(value))
				return true;
		}

		if(spec.equalsIgnoreCase("totalSwap")) {
			
			if (resource.freeDiskSpace < Long.parseLong(value))
				return true;
		}
		
		return false;
	}

	public static boolean isSpecMoreThanValue(String spec, String value, Resource resource) {

		if(spec.equalsIgnoreCase("processorMhz")) {
			if (resource.processorMhz > Long.parseLong(value))
				return true;
		}
		
		if(spec.equalsIgnoreCase("freeDiskSpace")) {
			if (resource.freeDiskSpace  > Long.parseLong(value))
				return true;
		}
		
		if(spec.equalsIgnoreCase("freeRam")) {
			if (resource.freeRam > Long.parseLong(value))
				return true;
		}
		
		if(spec.equalsIgnoreCase("freeSwap")) {
			if (resource.freeSwap > Long.parseLong(value))
				return true;
		}
		
		if(spec.equalsIgnoreCase("totalRam")) {
			if (resource.totalRam > Long.parseLong(value))
				return true;
		}
		
		if(spec.equalsIgnoreCase("totalSwap")) {
			if (resource.freeDiskSpace > Long.parseLong(value))
				return true;
		}

		if(spec.equalsIgnoreCase("totalSwap")) {
			if (resource.freeDiskSpace > Long.parseLong(value))
				return true;
		}
		
		return false;
	}

}
