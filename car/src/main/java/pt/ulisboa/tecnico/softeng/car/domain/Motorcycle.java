package pt.ulisboa.tecnico.softeng.car.domain;

public class Motorcycle extends Motorcycle_Base {
	public Motorcycle(String plate, int kilometers, double price, RentACar rentACar) {
		super();
		init(plate, kilometers, price, rentACar);
	}
	
	protected void init(String plate, int kilometers, double price, RentACar rentACar) {
	     super.init(plate, kilometers, price, rentACar);
	   }

}
