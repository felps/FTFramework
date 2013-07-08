
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;


public class snake {

	public static void main(String[] args) throws FileNotFoundException {
		String file = "/home/felps/Documentos/Desenvolvimento/Workspace_Integrade/Learning/src/teste.yaml";
		InputStream input = new FileInputStream(new File(file));
		Yaml yaml = new Yaml();
		int counter = 0;
		for (Object data : yaml.loadAll(input)) {
		    System.out.println((data instanceof Map)+"-"+data);
		    Map m = (Map) data;
		    
		    System.out.println(m.keySet());
		    Object taskList = m.get("duration");
			System.out.println(taskList);
			/*for (Object o : taskList ){
				System.out.println(o);
				
			}*/
		    
		    counter++;
		}

	}
}
