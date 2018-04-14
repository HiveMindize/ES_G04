package pt.ulisboa.tecnico.softeng.car.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;

import pt.ist.fenixframework.FenixFramework;
import pt.ulisboa.tecnico.softeng.bank.domain.Operation;
import pt.ulisboa.tecnico.softeng.car.dataobjects.RentingData;
import pt.ulisboa.tecnico.softeng.car.exception.CarException;

public class RentACar extends RentACar_Base {

	public int getNextCounter() {
		int counter = super.getCounter() + 1;
		setCounter(counter);
		return counter;
	}

	public RentACar(String name, String nif, String iban) {
		checkArguments(name, nif, iban);
		setName(name);
		setNif(nif);
		setIban(iban);
		setCode(getNif() + Integer.toString(getNextCounter()));
		
		setProcessor(new Processor());

		FenixFramework.getDomainRoot().addRentACar(this);
	}
	
	public void delete() {
		setProcessor(null);

		for (VehicleAndPlate vap : this.getVehicleAndPlateSet()) {
			vap.delete();
		}

		deleteDomainObject();
	}

	private void checkArguments(String name, String nif, String iban) {
		if (name == null || name.isEmpty() || nif == null || nif.isEmpty() || iban == null || iban.isEmpty()) {

			throw new CarException();
		}

		for (final RentACar rental : FenixFramework.getDomainRoot().getRentACarSet()) {
			if (rental.getNif().equals(nif)) {
				throw new CarException();
			}
		}
	}

	void addVehicle(VehicleAndPlate vap) {
		this.addVehicleAndPlate(vap);
	}

	public boolean hasVehicle(String plate) {
		for (VehicleAndPlate vap : getVehicleAndPlateSet()) {
			if (vap.getVehicle().getPlate().equals(plate)) {
				return true;
			}
		}
		
		return false;
	}

	public Set<Vehicle> getAvailableVehicles(Class<?> cls, LocalDate begin, LocalDate end) {
		final Set<Vehicle> availableVehicles = new HashSet<>();
		
		for (VehicleAndPlate vap : getVehicleAndPlateSet()) {
			if (cls == vap.getVehicle().getClass() && vap.getVehicle().isFree(begin, end)) {
				availableVehicles.add(vap.getVehicle());
			}			
		}
		
		return availableVehicles;
	}

	private static Set<Vehicle> getAllAvailableVehicles(Class<?> cls, LocalDate begin, LocalDate end) {
		final Set<Vehicle> vehicles = new HashSet<>();
		for (final RentACar rentACar : FenixFramework.getDomainRoot().getRentACarSet()) {
			vehicles.addAll(rentACar.getAvailableVehicles(cls, begin, end));
		}
		return vehicles;
	}

	public static Set<Vehicle> getAllAvailableMotorcycles(LocalDate begin, LocalDate end) {
		return getAllAvailableVehicles(Motorcycle.class, begin, end);
	}

	public static Set<Vehicle> getAllAvailableCars(LocalDate begin, LocalDate end) {
		return getAllAvailableVehicles(Car.class, begin, end);
	}

	public static String rent(Class<? extends Vehicle> vehicleType, String drivingLicense, String buyerNIF,
			String buyerIBAN, LocalDate begin, LocalDate end) {
		Set<Vehicle> availableVehicles;

		if (vehicleType == Car.class) {
			availableVehicles = getAllAvailableCars(begin, end);
		} else {
			availableVehicles = getAllAvailableMotorcycles(begin, end);
		}

		return availableVehicles.stream().findFirst().map(v -> v.rent(drivingLicense, begin, end, buyerNIF, buyerIBAN))
				.orElseThrow(CarException::new).getReference();
	}

	public static String cancelRenting(String reference) {
		final Renting renting = getRenting(reference);

		if (renting != null) {
			return renting.cancel();
		}

		throw new CarException();
	}

	/**
	 * Lookup for a renting using its reference.
	 *
	 * @param reference
	 * @return the renting with the given reference.
	 */
	protected static Renting getRenting(String reference) {
		for (final RentACar rentACar : FenixFramework.getDomainRoot().getRentACarSet()) {
			for (VehicleAndPlate vap : getVehicleAndPlateSet()) {
				final Renting renting = vap.getVehicle().getRenting(reference);
				if (renting != null) {
					return renting;
				}
			}
		}
		return null;
	}

	public static RentingData getRentingData(String reference) {
		final Renting renting = getRenting(reference);
		if (renting == null) {
			throw new CarException();
		}
		return new RentingData(renting);
	}
}
