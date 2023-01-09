import javax.swing.JFrame;

public class RepartidorTest {
	
	 public static void main( String[] args )
	   {
	      Repartidor application = new Repartidor(); // create server
	      application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	      application.runDeal(); // run server application
	   } // end main

}
