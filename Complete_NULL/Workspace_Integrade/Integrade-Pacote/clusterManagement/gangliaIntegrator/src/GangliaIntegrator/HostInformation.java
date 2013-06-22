package GangliaIntegrator;


public class HostInformation {

	private String _lrmIor;
	private String _hostName;
	private String _osName;
	private String _osVersion;
	private String _processorName;
	private int _processorMhz;
	private int _totalRam;
	private int _totalSwap; 
	private int _freeRam;
	private int _freeSwap;
	private int _freeDiskSpace;
	private float _cpuUsage;
	//private boolean _recentlyPicked;
	private int _lastUpdated;
	private String _ipAddress;
	
	private static final String NOT_AVAILABLE_STRING = "NA";
	private static final int NOT_AVAILABLE_INT = Integer.MIN_VALUE;
	private static final float NOT_AVAILABLE_FLOAT = Float.MIN_VALUE;
	
	public HostInformation(){
		_lrmIor = NOT_AVAILABLE_STRING;
		_hostName = NOT_AVAILABLE_STRING;
		_osName = NOT_AVAILABLE_STRING;
		_osVersion = NOT_AVAILABLE_STRING;
		_processorName = NOT_AVAILABLE_STRING;
		_processorMhz = NOT_AVAILABLE_INT;
		_totalRam = NOT_AVAILABLE_INT;
		_totalSwap = NOT_AVAILABLE_INT; 
		_freeRam = NOT_AVAILABLE_INT;
		_freeSwap = NOT_AVAILABLE_INT;
		_freeDiskSpace = NOT_AVAILABLE_INT;
		_cpuUsage = NOT_AVAILABLE_FLOAT;
		//_recentlyPicked = false;
		
		//TODO: initialize with the current time (check if it is seconds since epoch)  
		_lastUpdated = NOT_AVAILABLE_INT;   
	}
	
	public String lrmIor() {
		return _lrmIor;
	}
	public void lrmIor(String ior) {
		_lrmIor = ior;
	}
	public String hostName() {
		return _hostName;
	}
	public void hostName(String name) {
		_hostName = name;
	}
	public String osName() {
		return _osName;
	}
	public void osName(String name) {
		_osName = name;
	}
	public String osVersion() {
		return _osVersion;
	}
	public void osVersion(String version) {
		_osVersion = version;
	}
	public String processorName() {
		return _processorName;
	}
	public void processorName(String name) {
		_processorName = name;
	}
	public int processorMhz() {
		return _processorMhz;
	}
	public void processorMhz(int mhz) {
		_processorMhz = mhz;
	}
	public int totalRam() {
		return _totalRam;
	}
	public void totalRam(int ram) {
		_totalRam = ram;
	}
	public int totalSwap() {
		return _totalSwap;
	}
	public void totalSwap(int swap) {
		_totalSwap = swap;
	}
	public int freeRam() {
		return _freeRam;
	}
	public void freeRam(int ram) {
		_freeRam = ram;
	}
	public int freeSwap() {
		return _freeSwap;
	}
	public void freeSwap(int swap) {
		_freeSwap = swap;
	}
	public int freeDiskSpace() {
		return _freeDiskSpace;
	}
	public void freeDiskSpace(int diskSpace) {
		_freeDiskSpace = diskSpace;
	}
	public float cpuUsage() {
		return _cpuUsage;
	}
	public void cpuUsage(float usage) {
		_cpuUsage = usage;
	}
//	public boolean recentlyPicked() {
//		return _recentlyPicked;
//	}
//	public void recentlyPicked(boolean picked) {
//		_recentlyPicked = picked;
//	}
	public int lastUpdated() {
		return _lastUpdated;
	}
	public void lastUpdated(int updated) {
		_lastUpdated = updated;
	}

	public void ipAddress(String ipAddress) {
		_ipAddress = ipAddress;
	}

	public String ipAddress() {
		return _ipAddress;
	}

	
}
