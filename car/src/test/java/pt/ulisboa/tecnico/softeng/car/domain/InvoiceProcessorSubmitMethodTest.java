package pt.ulisboa.tecnico.softeng.car.domain;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import mockit.Delegate;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import pt.ulisboa.tecnico.softeng.car.exception.RemoteAccessException;
import pt.ulisboa.tecnico.softeng.car.interfaces.BankInterface;
import pt.ulisboa.tecnico.softeng.car.interfaces.TaxInterface;
import pt.ulisboa.tecnico.softeng.bank.exception.BankException;
//import pt.ulisboa.tecnico.softeng.hotel.domain.Room.Type;
import pt.ulisboa.tecnico.softeng.tax.dataobjects.InvoiceData;
import pt.ulisboa.tecnico.softeng.tax.exception.TaxException;

@RunWith(JMockit.class)
public class InvoiceProcessorSubmitMethodTest {
	private static final String CANCEL_PAYMENT_REFERENCE = "CancelPaymentReference";
	private static final String INVOICE_REFERENCE = "InvoiceReference";
	private static final String PAYMENT_REFERENCE = "PaymentReference";
	
	private static final LocalDate date1 = LocalDate.parse("2018-01-06");
    private static final LocalDate date2 = LocalDate.parse("2018-01-07");
	
	private static final String IBAN = "BK01987654321";
	private static final String NIF = "123456789";
	
	private Vehicle car;
	private RentACar rentACar;
	private String reference;
	
	private static final String NAME1 = "eartz";
	private static final String PLATE_CAR1 = "aa-99-11";
	private static final String DRIVING_LICENSE = "br123";
	private static final int PRICE = 20;

	@Before
	public void setUp() {
		
		this.rentACar = new RentACar(NAME1, NIF, IBAN);
		this.car = new Car(PLATE_CAR1, 10, rentACar, PRICE);
	}

	@Test
	public void success(@Mocked final TaxInterface taxInterface, @Mocked final BankInterface bankInterface) {
		new Expectations() {
			{
				BankInterface.processPayment(this.anyString, this.anyInt);
				TaxInterface.submitInvoice((InvoiceData) this.any);
			}
		};
		
		RentACar.rentVehicle( DRIVING_LICENSE, date1, date2, IBAN, NIF);

		new FullVerifications() {
			{
			}
		};
	}

	@Test
	public void oneTaxFailureOnSubmitInvoice(@Mocked final TaxInterface taxInterface,
			@Mocked final BankInterface bankInterface) {
		new Expectations() {
			{
				BankInterface.processPayment(this.anyString, this.anyInt);
				this.result = PAYMENT_REFERENCE;
				TaxInterface.submitInvoice((InvoiceData) this.any);
				this.result = new TaxException();
				this.result = INVOICE_REFERENCE;
			}
		};
		
		RentACar.rentVehicle( DRIVING_LICENSE, date1, date2, IBAN, NIF);
		
		rentACar.getProcessor().submitRenting(new Renting(DRIVING_LICENSE, date1, date2,car, IBAN, NIF));
		
		new FullVerifications(taxInterface) {
			{
				TaxInterface.submitInvoice((InvoiceData) this.any);
				this.times = 3;
			}
		};
	}

	@Test
	public void oneRemoteFailureOnSubmitInvoice(@Mocked final TaxInterface taxInterface,
			@Mocked final BankInterface bankInterface) {
		new Expectations() {
			{
				BankInterface.processPayment(this.anyString, this.anyInt);
				this.result = PAYMENT_REFERENCE;
				TaxInterface.submitInvoice((InvoiceData) this.any);
				this.result = new RemoteAccessException();
				this.result = INVOICE_REFERENCE;
			}
		};

		RentACar.rentVehicle( DRIVING_LICENSE, date1, date2, IBAN, NIF);
		
		rentACar.getProcessor().submitRenting(new Renting(DRIVING_LICENSE, date1, date2,car, IBAN, NIF));

		new FullVerifications(taxInterface) {
			{
				TaxInterface.submitInvoice((InvoiceData) this.any);
				this.times = 3;
			}
		};
	}

	@Test
	public void oneBankFailureOnProcessPayment(@Mocked final TaxInterface taxInterface,
			@Mocked final BankInterface bankInterface) {
		new Expectations() {
			{
				BankInterface.processPayment(this.anyString, this.anyInt);
				this.result = new BankException();
				this.result = PAYMENT_REFERENCE;
				TaxInterface.submitInvoice((InvoiceData) this.any);
				this.result = INVOICE_REFERENCE;
			}
		};

		RentACar.rentVehicle( DRIVING_LICENSE, date1, date2, IBAN, NIF);
		rentACar.getProcessor().submitRenting(new Renting(DRIVING_LICENSE, date1, date2,car, IBAN, NIF));

		new FullVerifications(bankInterface) {
			{
				BankInterface.processPayment(this.anyString, this.anyInt);
				this.times = 3;
			}
		};
	}

	@Test
	public void oneRemoteFailureOnProcessPayment(@Mocked final TaxInterface taxInterface,
			@Mocked final BankInterface bankInterface) {
		new Expectations() {
			{
				BankInterface.processPayment(this.anyString, this.anyInt);
				this.result = new RemoteAccessException();
				this.result = PAYMENT_REFERENCE;
				TaxInterface.submitInvoice((InvoiceData) this.any);
				this.result = INVOICE_REFERENCE;
			}
		};

		RentACar.rentVehicle( DRIVING_LICENSE, date1, date2, IBAN, NIF);
		rentACar.getProcessor().submitRenting(new Renting(DRIVING_LICENSE, date1, date2,car, IBAN, NIF));

		new FullVerifications(bankInterface) {
			{
				BankInterface.processPayment(this.anyString, this.anyInt);
				this.times = 3;
			}
		};
	}

	@Test
	public void successCancel(@Mocked final TaxInterface taxInterface, @Mocked final BankInterface bankInterface) {
		new Expectations() {
			{
				TaxInterface.submitInvoice((InvoiceData) this.any);
				BankInterface.processPayment(this.anyString, this.anyInt);

				TaxInterface.cancelInvoice(this.anyString);
				BankInterface.cancelPayment(this.anyString);
			}
		};

		this.reference = RentACar.rentVehicle(DRIVING_LICENSE, date1, date2, IBAN, NIF);
		this.car.getRenting(reference).cancel();
		
		new FullVerifications() {
			{
			}
		};
	}


	@Test
	public void oneBankExceptionOnCancelPayment(@Mocked final TaxInterface taxInterface,
			@Mocked final BankInterface bankInterface) {
		new Expectations() {
			{
				TaxInterface.submitInvoice((InvoiceData) this.any);
				BankInterface.processPayment(this.anyString, this.anyInt);

				BankInterface.cancelPayment(this.anyString);
				this.result = new BankException();
				this.result = CANCEL_PAYMENT_REFERENCE;
				TaxInterface.cancelInvoice(this.anyString);
			}
		};

		this.reference = RentACar.rentVehicle(DRIVING_LICENSE, date1, date2, IBAN, NIF);
		this.car.getRenting(this.reference).cancel();
		this.reference = RentACar.rentVehicle(DRIVING_LICENSE, date1, date2, IBAN, NIF);

		new FullVerifications(bankInterface) {
			{
				BankInterface.cancelPayment(this.anyString);
				this.times = 2;
			}
		};
	}
	
	@Test
	public void oneRemoteExceptionOnCancelPayment(@Mocked final TaxInterface taxInterface,
			@Mocked final BankInterface bankInterface) {
		new Expectations() {
			{
				TaxInterface.submitInvoice((InvoiceData) this.any);
				BankInterface.processPayment(this.anyString, this.anyInt);

				BankInterface.cancelPayment(this.anyString);
				this.result = new RemoteAccessException();
				this.result = CANCEL_PAYMENT_REFERENCE;
				TaxInterface.cancelInvoice(this.anyString);
			}
		};


		this.reference = RentACar.rentVehicle(DRIVING_LICENSE, date1, date2, IBAN, NIF);
		this.car.getRenting(reference).cancel();
		this.reference = RentACar.rentVehicle(DRIVING_LICENSE, date1, date2, IBAN, NIF);

		new FullVerifications(bankInterface) {
			{
				BankInterface.cancelPayment(this.anyString);
				this.times = 2;
			}
		};
	}

	@Test
	public void oneTaxExceptionOnCancelInvoice(@Mocked final TaxInterface taxInterface,
			@Mocked final BankInterface bankInterface) {
		new Expectations() {
			{
				BankInterface.processPayment(this.anyString, this.anyInt);
				TaxInterface.submitInvoice((InvoiceData) this.any);
				BankInterface.cancelPayment(this.anyString);
				this.result = CANCEL_PAYMENT_REFERENCE;
				TaxInterface.cancelInvoice(this.anyString);
				this.result = new Delegate() {
					int i = 0;

					public void delegate() {
						if (this.i < 1) {
							this.i++;
							throw new TaxException();
						}
					}
				};
			}
		};


		this.reference = RentACar.rentVehicle(DRIVING_LICENSE, date1, date2, IBAN, NIF);
		this.car.getRenting(reference).cancel();
		this.reference = RentACar.rentVehicle(DRIVING_LICENSE, date1, date2, IBAN, NIF);

		new FullVerifications(taxInterface) {
			{
				TaxInterface.cancelInvoice(this.anyString);
				this.times = 2;
			}
		};
	}
	
	
	@Test
	public void oneRemoteExceptionOnCancelInvoice(@Mocked final TaxInterface taxInterface,
			@Mocked final BankInterface bankInterface) {
		new Expectations() {
			{
				BankInterface.processPayment(this.anyString, this.anyInt);
				TaxInterface.submitInvoice((InvoiceData) this.any);

				BankInterface.cancelPayment(this.anyString);
				this.result = CANCEL_PAYMENT_REFERENCE;
				TaxInterface.cancelInvoice(this.anyString);
				this.result = new Delegate() {
					int i = 0;

					public void delegate() {
						if (this.i < 1) {
							this.i++;
							throw new RemoteAccessException();
						}
					}
				};
			}
		};


		this.reference = RentACar.rentVehicle(DRIVING_LICENSE, date1, date2, IBAN, NIF);
		this.car.getRenting(reference).cancel();
		this.reference = RentACar.rentVehicle(DRIVING_LICENSE, date1, date2, IBAN, NIF);

		new FullVerifications(taxInterface) {
			{
				TaxInterface.cancelInvoice(this.anyString);
				this.times = 2;
			}
		};
	}
	
	@After
	public void tearDown() {
		this.car.rentings.clear();
		Vehicle.plates.clear();
		RentACar.rentACars.clear();
	}

}
