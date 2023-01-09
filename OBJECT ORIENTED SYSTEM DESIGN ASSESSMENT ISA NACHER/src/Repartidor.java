import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;


public class Repartidor extends JFrame{
	private JButton Repartir;
	private Baraja nuevabaraja;
	private JTextArea areaDeVisualizacion; // display information to user
	private ExecutorService ejecutor; // will run players
	private ServerSocket servidor; // server socket
	private SockServer[] sockServer; // Array of objects to be threaded
	private int contador = 1; // counter of number of connections
	private String dcarta1,dcarta2;
	private ArrayList<Juego> jugadores;
	private Juego dcartas;
	private int playersleft;
	private boolean roundover = true;

	// set up GUI
	public Repartidor(){

		super( "Repartidor" );


		jugadores = new ArrayList();
		sockServer = new SockServer[ 100 ]; // allocate array for up to 10 server threads
		ejecutor = Executors.newFixedThreadPool(100); // create thread pool


		Repartir = new JButton("Repartir Cartas");

		Repartir.addActionListener(
				new ActionListener() 
				{
					// send message to client
					public void actionPerformed( ActionEvent event )
					{
						Repartir.setEnabled(false);
						nuevabaraja = new Baraja();
						roundover=false;
						DealCards();
						displayMessage("\n\nCARTAS REPARTIDAS\n\n");

					} // end method actionPerformed
				} // end anonymous inner class
				); // end call to addActionListener


		add(Repartir,BorderLayout.SOUTH);

		areaDeVisualizacion = new JTextArea(); // create displayArea
		areaDeVisualizacion.setEditable(false);
		add( new JScrollPane( areaDeVisualizacion ), BorderLayout.CENTER );

		setSize( 300, 300 ); // set size of window
		setVisible( true ); // show window
	} // end Server constructor

	// set up and run server 
	public void runDeal()
	{
		try // set up server to receive connections; process connections
		{
			servidor = new ServerSocket( 23555, 100 ); // create ServerSocket

			while ( true ) 
			{
				try 
				{
					//create a new runnable object to serve the next client to call in
					sockServer[contador] = new SockServer(contador);
					// make that new object wait for a connection on that new server object
					sockServer[contador].waitForConnection();
					// launch that server object into its own new thread
					ejecutor.execute(sockServer[ contador ]);
					// then, continue to create another object and wait (loop)

				} // end try
				catch ( EOFException eofException ) 
				{
					displayMessage( "\nServer terminated connection" );
				} // end catch
				finally 
				{
					++contador;
				} // end finally
			} // end while
		} // end try
		catch ( IOException ioException ) 
		{} // end catch
	} // end method runServer

	// manipulates displayArea in the event-dispatch thread
	private void displayMessage( final String messageToDisplay )
	{
		SwingUtilities.invokeLater(
				new Runnable() 
				{
					public void run() // updates displayArea
					{
						areaDeVisualizacion.append( messageToDisplay ); // append message
					} // end method run
				} // end anonymous inner class
				); // end call to SwingUtilities.invokeLater
	} // end method displayMessage

	private void DealCards(){

		try{
			playersleft = contador-1;
			nuevabaraja.barajarCartas();
			dcarta1 = nuevabaraja.repartirCarta();
			dcarta2 = nuevabaraja.repartirCarta();
			displayMessage("\n\n" +dcarta1 + " " +dcarta2);

			for (int i=1;i<= contador;i++) {
				String c1,c2;
				c1 = nuevabaraja.repartirCarta();
				c2 = nuevabaraja.repartirCarta();
				Juego p = new Juego(c1,c2);
				jugadores.add(p);
				sockServer[i].sendData("You were Dealt:\n" + c1 + " " + c2);
				sockServer[i].sendData("Your Total: " +  p.DameElTotal());

			}
		}
		catch(NullPointerException n){}
	}

	private void Results() {

		try{
			for (int i=1;i<= contador;i++) {
				sockServer[i].sendData("Dealer has " + dcartas.DameElTotal());

				if( (dcartas.DameElTotal() <= 21) && (jugadores.get(i-1).DameElTotal() <= 21 ) ){

					if (dcartas.DameElTotal() > jugadores.get(i-1).DameElTotal()) {
						sockServer[i].sendData("\n You Lose!");
					}

					if (dcartas.DameElTotal() < jugadores.get(i-1).DameElTotal()) {
						sockServer[i].sendData("\n You Win!");
					}

					if (dcartas.DameElTotal() == jugadores.get(i-1).DameElTotal()) {
						sockServer[i].sendData("\n Tie!");
					}				

				}//end if statement when dealer and player are under 21

				if(dcartas.ComprobarSiTeHasPasado()){
					
					if(jugadores.get(i-1).ComprobarSiTeHasPasado()){
						sockServer[i].sendData("\n Tie!");
					}
					if(jugadores.get(i-1).DameElTotal() <= 21){
						sockServer[i].sendData("\n You Won!");
					}
				}

				if(jugadores.get(i-1).ComprobarSiTeHasPasado() && dcartas.DameElTotal() <= 21){
					sockServer[i].sendData("\n You Lose!");
				}
			}//end for loop
			


		}//end try block
		catch(NullPointerException n){}
	}

	/* This new Inner Class implements Runnable and objects instantiated from this
	 * class will become server threads each serving a different client
	 */
	private class SockServer implements Runnable
	{
		private ObjectOutputStream output; // output stream to client
		private ObjectInputStream input; // input stream from client
		private Socket connection; // connection to client
		private int myConID;

		public SockServer(int counterIn)
		{
			myConID = counterIn;
		}

		public void run() {
			try {
				try {
					getStreams(); // get input & output streams
					processConnection(); // process connection

				} // end try
				catch ( EOFException eofException ) 
				{
					displayMessage( "\nServer" + myConID + " terminated connection" );
				}
				finally
				{
					closeConnection(); //  close connection
				}// end catch
			} // end try
			catch ( IOException ioException ) 
			{} // end catch
		} // end try

		// wait for connection to arrive, then display connection info
		private void waitForConnection() throws IOException
		{

			displayMessage( "Waiting for connection" + myConID + "\n" );
			connection = servidor.accept(); // allow server to accept connection            
			displayMessage( "Connection " + myConID + " received from: " +
					connection.getInetAddress().getHostName() );
		} // end method waitForConnection

		private void getStreams() throws IOException
		{
			// set up output stream for objects
			output = new ObjectOutputStream( connection.getOutputStream() );
			output.flush(); // flush output buffer to send header information

			// set up input stream for objects
			input = new ObjectInputStream( connection.getInputStream() );

			displayMessage( "\nGot I/O streams\n" );
		} // end method getStreams

		// process connection with client
		private void processConnection() throws IOException
		{
			String message = "Connection " + myConID + " successful";
			sendData( message ); // send connection successful message


			do // process messages sent from client
			{ 
				try // read message and display it
				{
					if(message.contains("hit")){				
						cardhit();
					}

					if(message.contains("stay")){
						this.sendData("Please Wait");
						playersleft--;
						CheckDone();
					}


					message = ( String ) input.readObject(); // read new message

				} // end try
				catch ( ClassNotFoundException classNotFoundException ) 
				{
					displayMessage( "\nUnknown object type received" );
				} // end catch

			} while ( !message.equals( "CLIENT>>> TERMINATE" ) );
		} // end method processConnection


		private void DealerGo() {		
			dcartas = new Juego(dcarta1,dcarta2);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (dcartas.DameElTotal() < 16){
				while(dcartas.DameElTotal() < 16){
					String card1 = nuevabaraja.repartirCarta();
					dcartas.PedirMasCartas(card1);
					displayMessage("Dealer hits..." + card1 +  "\n" + "Total:" + dcartas.DameElTotal() + "\n");				
				}
			}
			if(dcartas.ComprobarSiTeHasPasado()){
				displayMessage("Dealer Busts!");
			}
			else{
				displayMessage("Dealer has" + " " + dcartas.DameElTotal());
			}

			Results();
		}

		private void cardhit() {

			String nextc = nuevabaraja.repartirCarta();
			sendData(nextc);
			jugadores.get(this.myConID -1).PedirMasCartas(nextc);
			sendData("Your Total: " +  jugadores.get(this.myConID -1).DameElTotal());
			if(jugadores.get(this.myConID -1).ComprobarSiTeHasPasado()) {			//if player busted
				sendData("Bust!\n");		
				playersleft--;
				if(playersleft==0){
					DealerGo();
				}
			}


		}


		private void CheckDone() {

			if(playersleft==0){

				DealerGo();
			}
		}

		// close streams and socket
		private void closeConnection() 
		{
			displayMessage( "\nTerminating connection " + myConID + "\n" );

			try 
			{
				output.close(); // close output stream
				input.close(); // close input stream
				connection.close(); // close socket
			} // end try
			catch ( IOException ioException ) 
			{} // end catch
		} // end method closeConnection

		private void sendData( String message )
		{
			try // send object to client
			{
				output.writeObject( message );
				output.flush(); // flush output to client

			} // end try
			catch ( IOException ioException ) 
			{
				areaDeVisualizacion.append( "\nError writing object" );
			} // end catch
		} // end method sendData


	}
}
