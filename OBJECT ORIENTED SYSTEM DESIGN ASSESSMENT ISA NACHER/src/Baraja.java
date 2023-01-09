import java.util.Random;

public class Baraja {
	private Cartas[] baraja;
	private int contador;
	private static final int NUMERO_DE_CARTAS = 52;
	private static final Random rand = new Random();

	public Baraja(){
		String[] caras = {"As","2","3","4","5","6","7","8","9","10","Sota","Caballo","Rey"};
		baraja = new Cartas[NUMERO_DE_CARTAS];
		contador = 0;

		for(int i=0; i<baraja.length; i++){
			baraja[i] = new Cartas(caras[i%13]);
		}


	}//finaliza el constructor "Baraja"

	public void barajarCartas(){ //in english is shuffle
		contador=0;

		for(int i=0; i<baraja.length; i++){
			int random = rand.nextInt(NUMERO_DE_CARTAS);
			Cartas t = baraja[i];
			baraja[i] = baraja[random];
			baraja[random]=t;
		}
	}
	
	public String repartirCarta(){ // in english is deal card
		
		if(contador<baraja.length){
			return baraja[contador++].toString();
		}
		else{
			return null;
		}
	}

}
