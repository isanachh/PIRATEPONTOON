import java.util.ArrayList;

public class Juego {
	private boolean pasarse=false;
	private int Total=0;

	private ArrayList<String> Cartas;
	private ArrayList<String> Ases;


	public Juego(String c1, String c2) {
		Cartas = new ArrayList();
		Ases = new ArrayList();

		if (c1 == "As") {
			Ases.add(c1);
		}
		else{
			Cartas.add(c1);
		}

		if (c2 == "As") {
			Ases.add(c2);
		}
		else {
			Cartas.add(c2);
		}

		SetTotal();

	}//end Constructor


public int DameElTotal() { //in english is Get Card Total
	return Total;
}

public void PedirMasCartas(String ca){ //in English is Hit

	if (ca == "As") {
		Ases.add("As");
	}
	else{
		Cartas.add(ca);
	}

	if(Ases.size() != 0){
		SetTotal();
	}

	else if (ca == "Sota" || ca =="Caballo" || ca =="Rey"){

		Total += 10;
	}

	else {
		Total += Integer.parseInt(ca);
	}


	ComprobarSiTeHasPasado();


}

private void SetTotal() {

	Total = 0;
	for(String c : Cartas){
		if (c == "Sota" || c =="Caballo" || c =="Rey"){
			Total += 10;
		}

		else{
			Total += Integer.parseInt(c);
		}

	}

	for(String a : Ases){
		if (Total <= 10){
			Total += 11;
		}
		else { 
			Total += 1;
		}

	}
}


public boolean ComprobarSiTeHasPasado(){ //in English is bust
	if(Total > 21){
		pasarse = true;
	}

	else {
		pasarse = false;
	}

	return pasarse;
}

}
