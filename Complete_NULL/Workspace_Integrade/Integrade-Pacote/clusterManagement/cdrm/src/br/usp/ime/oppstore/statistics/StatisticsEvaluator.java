package br.usp.ime.oppstore.statistics;

public class StatisticsEvaluator {
	
	public static double evaluateMean(double[] values) {
    	double meanValue = 0;
    	for (double value : values)
    		meanValue += value;
    	meanValue /= values.length;

    	return meanValue;
    }
    
	public static double evaluateStdDev(double[] values, double mean) {

    	double stdDev = 0;
    	for (double value : values)
    		stdDev += (value - mean) * (value - mean);
    	stdDev = Math.sqrt( stdDev / values.length );

    	return stdDev;
    }


}
