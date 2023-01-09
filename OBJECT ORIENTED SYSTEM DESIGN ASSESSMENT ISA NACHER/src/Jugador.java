import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class Jugador extends JFrame {
	private JButton Hit;
	private JButton Stay;
	private JPanel botones;
	private JTextArea areaDeVisualizacion; // display information to user
	private ObjectOutputStream output; // output stream to server
	private ObjectInputStream input; // input stream from server
	private String mensaje = ""; // message from server
	private String chatServer; // host server for this application
	private Socket cliente; // socket to communicate with server
	private int cartaamt=0; // initialize chatServer and set up GUI
	
	public Jugador(String host)
	{
		super( "Jugador" );

		chatServer = host; // set server to which this client connects

		botones = new JPanel();
		botones.setLayout(new GridLayout(1,2));
		Hit = new JButton("Hit");
		Stay = new JButton("Stay");
		
		Hit.addActionListener(
				new ActionListener() 
				{
					// send message to server
					public void actionPerformed( ActionEvent event )
					{
						sendData( "hit" );
					} // end method actionPerformed
				} // end anonymous inner class
				); // end call to addActionListener
		
		Stay.addActionListener(
				new ActionListener() 
				{
					// send message to server
					public void actionPerformed( ActionEvent event )
					{
						sendData( "stay" );
					} // end method actionPerformed
				} // end anonymous inner class
				); // end call to addActionListener

		botones.add(Hit, BorderLayout.SOUTH);
		botones.add(Stay, BorderLayout.SOUTH);
		botones.setVisible(true);
		add(botones,BorderLayout.SOUTH);
		areaDeVisualizacion = new JTextArea(); // create displayArea
		add( new JScrollPane( areaDeVisualizacion ), BorderLayout.CENTER );

		setSize( 300, 300 ); // set size of window
		setVisible( true ); // show window
	} // end Client constructor

	// connect to server and process messages from server
	public void runClient() 
	{
		try // connect to server, get streams, process connection
		{
			connectToServer(); // create a Socket to make connection
			getStreams(); // get the input and output streams
			processConnection(); // process connection
		} // end try
		catch ( EOFException eofException ) 
		{
			displayMessage( "\nClient terminated connection" );
		} // end catch
		catch ( IOException ioException ) 
		{} // end catch
		finally 
		{
			closeConnection(); // close connection
		} // end finally
	} // end method runClient

	// connect to server
	private void connectToServer() throws IOException
	{      
		displayMessage( "Attempting connection\n" );

		// create Socket to make connection to server
		cliente = new Socket( InetAddress.getByName( chatServer ), 23555 );

		// display connection information
		displayMessage( "Connected to: " + 
				cliente.getInetAddress().getHostName() );
	} // end method connectToServer

	// get streams to send and receive data
	private void getStreams() throws IOException
	{
		// set up output stream for objects
		output = new ObjectOutputStream( cliente.getOutputStream() );      
		output.flush(); // flush output buffer to send header information

		// set up input stream for objects
		input = new ObjectInputStream( cliente.getInputStream() );

		displayMessage( "\nGot I/O streams\n" );
	} // end method getStreams

	// process connection with server
	private void processConnection() throws IOException
	{


		do // process messages sent from server
		{ 
			try // read message and display it
			{
				mensaje = ( String ) input.readObject(); // read new message
				displayMessage( "\n" + mensaje ); // display message
				if (mensaje.contains("Bust!") || mensaje.contains("Please Wait")){
					botones.setVisible(false);				
				}
				
			} // end try
			catch ( ClassNotFoundException classNotFoundException ) 
			{
				displayMessage( "\nUnknown object type received" );
			} // end catch

		} while ( !mensaje.equals( "SERVER>>> TERMINATE" ) );
	} // end method processConnection

	// close streams and socket
	private void closeConnection() 
	{
		displayMessage( "\nClosing connection" );
		

		try 
		{ //here i have to wait !!!
			output.close(); // close output stream
			input.close(); // close input stream
			cliente.close(); // close socket
		} // end try
		catch ( IOException ioException ) 
		{} // end catch
	} // end method closeConnection

	// send message to server
	private void sendData( String message )
	{
		try // send object to server
		{
			output.writeObject(  message );
			output.flush(); // flush data to output
			
		} // end try
		catch ( IOException ioException )
		{
			areaDeVisualizacion.append( "\nError writing object" );
		} // end catch
	} // end method sendData

	// manipulates displayArea in the event-dispatch thread
	private void displayMessage( final String messageToDisplay )
	{
		SwingUtilities.invokeLater(
				new Runnable()
				{
					public void run() // updates displayArea
					{
						areaDeVisualizacion.append( messageToDisplay );
					} // end method run
				}  // end anonymous inner class
				); // end call to SwingUtilities.invokeLater
	} // end method displayMessage


}
