package masct.util;
import java.util.Vector;

public class Copy {

	public String name;
	public String arg;
	public Vector input;
	public boolean stdout;
	public boolean stderr;
	public Vector output;
	
	public Copy() {
		super();
		name = "0";
		arg = "";
		input = new Vector();
		output = new Vector();
		stdout = true;
		stderr = true;
	}

	public Vector getInput() {
		return input;
	}

	public void setInput(Vector input) {
		this.input = input;
	}

	public Vector getOutput() {
		return output;
	}

	public void setOutput(Vector output) {
		this.output = output;
	}

}
