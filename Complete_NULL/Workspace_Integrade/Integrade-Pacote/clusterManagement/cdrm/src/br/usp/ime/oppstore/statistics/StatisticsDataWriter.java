package br.usp.ime.oppstore.statistics;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class StatisticsDataWriter {

    Calendar calendar;
    
    StatisticsDataWriter () {
        calendar = new GregorianCalendar();        
    }
    
    public String generateFileName (String prefix) {        
        StringBuffer filenameBuffer = new StringBuffer();        
                
        String hostName = null;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            hostName = localHost.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }        
                
        filenameBuffer.append(prefix);
        filenameBuffer.append(hostName);
        filenameBuffer.append("-");
        filenameBuffer.append(calendar.get(Calendar.YEAR));
        filenameBuffer.append(calendar.get(Calendar.MONTH));
        filenameBuffer.append(calendar.get(Calendar.DAY_OF_MONTH));                
        filenameBuffer.append(calendar.get(Calendar.HOUR_OF_DAY));
        filenameBuffer.append(calendar.get(Calendar.MINUTE));
        filenameBuffer.append(".dat");

        return filenameBuffer.toString();
    }
    
    public void writeDataToFile(String filename, BrokerStatisticsCollector statistics) {
        // Appends data to a file
    }

    public void writeDataToFile(String filename, StatisticsCollector statistics) {
        // Appends data to a file
    }

}
