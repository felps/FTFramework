package logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MyLogger {

	private static Logger logger = LogManager.getLogger("GLOBAL");

	public static Logger getLogger() {
		return logger;
	}
	
	public static void setLogger(Logger log) {
		logger = log;
	}
}
