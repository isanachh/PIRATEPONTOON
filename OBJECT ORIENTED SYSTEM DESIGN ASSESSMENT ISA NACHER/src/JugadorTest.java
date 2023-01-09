import javax.swing.JFrame;

public class JugadorTest {
	
	public static void main( String[] args )
	   {
	      Jugador application; // declare client application

	      // if no command line args
	      if ( args.length == 0 )
	         application = new Jugador( "192.168.56.1" ); // connect to my IP?
	      else
	         application = new Jugador( args[ 0 ] ); // use args to connect

	      application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	      application.runClient(); // run client application
	   } // end main
	} // end class ClientTest


