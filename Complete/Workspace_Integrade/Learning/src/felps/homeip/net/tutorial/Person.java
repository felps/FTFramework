/**
 * 
 */
package felps.homeip.net.tutorial;

/**
 * @author felps
 *
 */
public class Person {
	//fields
	private String name;
	private int maximumBooks;
	
	//constructors
	public Person(){
		setName("unknown name");
		maximumBooks = 3;
	}

	//Methods
	public void setName(String anyName) {
		this.name = anyName;
	}

	public String getName() {
		return name;
	}
	
	public void setMaximumBooks(int maximumBooks){
		this.maximumBooks = maximumBooks;
	}
	
	public int getMaximumBooks(){
		return this.maximumBooks;
	}
	
	public String toString(){
		return this.getName() + " (" + this.getMaximumBooks() + " Books)";
	}
	
}
