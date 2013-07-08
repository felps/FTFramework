package masct.gui;

import java.awt.Button;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Label;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ImagePanel extends Frame implements ActionListener {
	
	public String fileName;
	private Image img = null;
	private Label outputFilesListLabel = null;
	private TextArea resultsTextArea = null;
	private Button returnButton = null;
	private Button okButton = null;
	private int w, h;
	
	/**
	 * This is the default constructor
	 */
	public ImagePanel(byte[] image) {
		super();

		// Exiting program on window close
		addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });		
		
		this.setUndecorated(true);
		 
		// window should be visible
		this.setVisible(true);
 
		// switching to fullscreen mode
		GraphicsEnvironment.getLocalGraphicsEnvironment().
		getDefaultScreenDevice().setFullScreenWindow(this);
		
		w = this.getWidth();
		h = this.getHeight();
		
		img = Toolkit.getDefaultToolkit().createImage(image);
	
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		
		returnButton = new Button();
		returnButton.setBounds(new Rectangle(156, 245, 73, 20));
		returnButton.setLabel("Voltar");
		returnButton.addActionListener( this );		
		
		okButton = new Button();
		okButton.setBounds(new Rectangle(10, 244, 136, 20));
		okButton.setLabel("Get Output File");
		okButton.addActionListener( this );

		outputFilesListLabel = new Label();
		outputFilesListLabel.setBounds(new java.awt.Rectangle(10, 10, 200, 18));
		outputFilesListLabel.setText("OutputFiles:");

	}
	
	/**
	 * This method initializes ResultsLst	
	 * 	
	 * @return javax.swing.JList	
	 */
	public TextArea getResultsTextArea() {
		if ( resultsTextArea == null) {
			resultsTextArea = new TextArea();
			resultsTextArea.setBounds(new java.awt.Rectangle(10,28,220,210));
		}
		return resultsTextArea;
	}
	
	public void setFile(String type, String file){
		
		
		if( type.equals("image") ){
			
		}
		else{
				
			this.add(getResultsTextArea());
			resultsTextArea.setText( file );
		}
		
	}

	public void actionPerformed(ActionEvent e) {
		this.setVisible(false);
		
		Masct.getInstance().setMenuBar( Masct.getInstance().getERMenuBar() );
		Masct.getInstance().getExecutionResultsPanel().setVisible(true);
		
	}
	
	public void paint(Graphics g)
	{
		if (img != null) // if screenImage is not null (image loaded and ready)
			g.drawImage(img, // draw it 
						w/2 - img.getWidth(this) / 2, // at the center 
						h/2 - img.getHeight(this) / 2, // of screen
						this);		

	}
	
	public void setImage( byte[] image){		
	

	}
	
}
