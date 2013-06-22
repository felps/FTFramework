package mocks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

public class BufferedReaderMock extends BufferedReader {
	
	ArrayList<String> lines;
	int n;

	public BufferedReaderMock(Reader arg0) {
		super(arg0);
		n = 0;
		lines = new ArrayList<String>();
		
	}
	
	public void writeLine(String line) {
		lines.add(line);
		n++;
	}

	@Override
	public String readLine() throws IOException {
		if (n == 0) {
			return null;
		}
		n--;
		return lines.remove(0);
	}
	
	

}
