package mocks;

import java.io.PrintWriter;
import java.io.Writer;

public class PrintWriterMock extends PrintWriter {

	private String content;

	public PrintWriterMock(Writer arg0)  {
		super(arg0);
		content = "";
	}

	public String getContent() {
		return content;
	}

	@Override
	public void println(String arg0) {

		content += arg0 + "\n";
	}
	
	@Override
	public void print(String arg0) {

		content += arg0;
	}

}
