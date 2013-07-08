package yaml;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.jvyaml.YAML;

public class YamlHandler {

	
	public YamlHandler() {
		/*
		 *    public static List loadAll(final Reader io) {
		 *      return loadAll(io, new DefaultYAMLFactory(),config());
		 */	
		try {
			YAML.load(new FileReader("/home/felps/Documentos/Desenvolvimento/teste.yml")).toString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
