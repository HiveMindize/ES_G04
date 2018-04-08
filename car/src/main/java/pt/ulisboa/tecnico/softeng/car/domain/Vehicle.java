package pt.ulisboa.tecnico.softeng.car.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ulisboa.tecnico.softeng.car.exception.CarException;

public abstract class Vehicle {
	private static Logger logger = LoggerFactory.getLogger(Vehicle.class);

	private static String plateFormat = "..-..-..";
	static Set<String> plates = new HashSet<>();

	private final String plate;
	private int price;
	private int kilometers;
	private final RentACar rentACar;
	public final Map<String, Renting> rentings = new HashMap<>();

	public Vehicle(String plate, int kilometers, RentACar rentACar, int price) {
		logger.debug("Vehicle plate: {}", plate);
		checkArguments(plate, kilometers, rentACar, price);

		this.price = price;
		this.plate = plate;
		this.kilometers = kilometers;
		this.rentACar = rentACar;

		plates.add(plate.toUpperCase());
		rentACar.addVehicle(this);
	}

	private void checkArguments(String plate, int kilometers, RentACar rentACar, int price) {
		if (plate == null || !plate.matches(plateFormat) || plates.contains(plate.toUpperCase()) || (price <= 0)) {
			throw new CarException(plate);
		} else if (kilometers < 0) {
			throw new CarException();
		} else if (rentACar == null) {
			throw new CarException();
		}
	}

	/**
	 * @return the plate
	 */
	public String getPlate() {
		return this.plate;
	}

	/**
	 * @return the price
	 */
	public int getPrice() {
		return this.price;
	}

	/**
	 * @return the kilometers
	 */
	public int getKilometers() {
		return this.kilometers;
	}

	/**
	 * @param kilometers
	 *            the kilometers to set
	 */
	public void addKilometers(int kilometers) {
		if (kilometers < 0) {
			throw new CarException();
		}
		this.kilometers += kilometers;
	}

	/**
	 * @return the rentACar
	 */
	public RentACar getRentACar() {
		return this.rentACar;
	}

	public boolean isFree(LocalDate begin, LocalDate end) {
		if (begin == null || end == null) {
			throw new CarException();
		}
		for (Renting renting : this.rentings.values()) {
			if (renting.conflict(begin, end)) {
				if (renting.isCancelled()) {
					return true;
				}
				
				return false;
			}
			
						
		}
		return true;
	}

	/**
	 * Add a <code>Renting</code> object to the vehicle. Use with caution --- no
	 * validation is being made.
	 * 
	 * @param renting
	 */
	private void addRenting(Renting renting) {
		this.rentings.put(renting.getReference(), renting);
	}

	/**
	 * Lookup for a <code>Renting</code> with the given reference.
	 * 
	 * @param reference
	 * @return Renting with the given reference
	 */
	public Renting getRenting(String reference) {
		return this.rentings.get(reference);
	}

	/**
	 * @param drivingLicense
	 * @param begin
	 * @param end
	 * @return
	 */
	public Renting rent(String drivingLicense, LocalDate begin, LocalDate end, String buyerIban, String buyerNif) {
		if (!isFree(begin, end)) {
			throw new CarException();
		}

		Renting renting = new Renting(drivingLicense, begin, end, this, buyerIban, buyerNif);
		this.addRenting(renting);

		return renting;
	}

	public static void removePlates() {

		plates.clear();
	}

}
