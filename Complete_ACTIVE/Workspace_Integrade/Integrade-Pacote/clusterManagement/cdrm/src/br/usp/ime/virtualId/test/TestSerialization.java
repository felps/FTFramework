package br.usp.ime.virtualId.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteOrder;

class Test implements Serializable {
	long value;
	String text;
}

public class TestSerialization {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Test test = new Test();
		test.value = 12345L;
		test.text = "Olá!";

		try {
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream objOut = new ObjectOutputStream ( byteOut );
			objOut.writeObject( test );
			
			//String objString = byteOut.toString();
			
			test.value = 41287L;
			test.text = "HaHaHa!";			
			
			ObjectInputStream objIn = new ObjectInputStream( new ByteArrayInputStream( byteOut.toByteArray() ) );
			Test test2 = (Test)objIn.readObject();
		
			System.out.println( "test  -> " +  test.value + " " +  test.text);
			System.out.println( "test2 -> " + test2.value + " " + test2.text);		

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
				
	}

}
