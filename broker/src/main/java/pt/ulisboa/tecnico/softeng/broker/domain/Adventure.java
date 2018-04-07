package pt.ulisboa.tecnico.softeng.broker.domain;



import org.joda.time.LocalDate;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;



import pt.ulisboa.tecnico.softeng.broker.exception.BrokerException;



public class Adventure {

	private static Logger logger = LoggerFactory.getLogger(Adventure.class);



	public static enum State {

		PROCESS_PAYMENT, RESERVE_ACTIVITY, BOOK_ROOM, UNDO, CONFIRMED, CANCELLED, RENT_VEHICLE

	}



	private static int counter = 0;



	private final String ID;

	private final Broker broker;

	private final LocalDate begin;

	private final LocalDate end;

	private final Client client;

	private final int amount;

	private String paymentConfirmation;

	private String paymentCancellation;

	private String roomConfirmation;

	private String roomCancellation;

	private String vehicleConfirmation;

	private String vehicleCancellation;

	private String activityConfirmation;

	private String activityCancellation;

	private String taxConfirmation;

	private String taxCancellation;



	private AdventureState state;



	public Adventure(Broker broker, LocalDate begin, LocalDate end, Client client, int amount) {

		checkArguments(broker, begin, end, amount);



		this.ID = broker.getCode() + Integer.toString(++counter);

		this.broker = broker;

		this.begin = begin;

		this.end = end;

		this.client = client;

		this.amount = amount;



		broker.addAdventure(this);



		setState(State.RESERVE_ACTIVITY);

	}



	private void checkArguments(Broker broker, LocalDate begin, LocalDate end, int amount) {

		if (broker == null || begin == null || end == null) {

			throw new BrokerException();

		}



		if (end.isBefore(begin)) {

			throw new BrokerException();

		}



		if (amount < 1) {

			throw new BrokerException();

		}

	}



	public String getID() {

		return this.ID;

	}



	public Broker getBroker() {

		return this.broker;

	}



	public LocalDate getBegin() {

		return this.begin;

	}



	public LocalDate getEnd() {

		return this.end;

	}



	public int getAge() {

		return this.client.getAGE();

	}



	public String getIBAN() {

		return this.client.getIBAN();

	}



	public int getAmount() {

		return this.amount;

	}



	public String getPaymentConfirmation() {

		return this.paymentConfirmation;

	}



	public void setPaymentConfirmation(String paymentConfirmation) {

		this.paymentConfirmation = paymentConfirmation;

	}



	public String getPaymentCancellation() {

		return this.paymentCancellation;

	}



	public void setPaymentCancellation(String paymentCancellation) {

		this.paymentCancellation = paymentCancellation;

	}



	public String getActivityConfirmation() {

		return this.activityConfirmation;

	}



	public void setActivityConfirmation(String activityConfirmation) {

		this.activityConfirmation = activityConfirmation;

	}



	public String getActivityCancellation() {

		return this.activityCancellation;

	}



	public void setActivityCancellation(String activityCancellation) {

		this.activityCancellation = activityCancellation;

	}



	public String getRoomConfirmation() {

		return this.roomConfirmation;

	}



	public void setRoomConfirmation(String roomConfirmation) {

		this.roomConfirmation = roomConfirmation;

	}



	public String getRoomCancellation() {

		return this.roomCancellation;

	}



	public void setRoomCancellation(String roomCancellation) {

		this.roomCancellation = roomCancellation;

	}



	public String getVehicleConfirmation() {

		return this.vehicleConfirmation;

	}



	public void setVehicleConfirmation(String vehicleConfirmation) {

		this.vehicleConfirmation = vehicleConfirmation;

	}



	public String getVehicleCancellation() {

		return this.vehicleCancellation;

	}



	public void setVehicleCancellation(String vehicleCancellation) {

		this.vehicleCancellation = vehicleCancellation;

	}



	public String getTaxConfirmation() {

		return this.taxConfirmation;

	}



	public void setTaxConfirmation(String taxConfirmation) {

		this.taxConfirmation = taxConfirmation;

	}



	public String getTaxCancellation() {

		return this.taxCancellation;

	}



	public void setTaxCancellation(String taxCancellation) {

		this.taxCancellation = taxCancellation;

	}



	public State getState() {

		return this.state.getState();

	}



	public void setState(State state) {

		switch (state) {

		case PROCESS_PAYMENT:

			this.state = new ProcessPaymentState();

			break;

		case RESERVE_ACTIVITY:

			this.state = new ReserveActivityState();

			break;

		case BOOK_ROOM:

			this.state = new BookRoomState();

			break;

		case RENT_VEHICLE:

			this.state = new RentVehicleState();

			break;

		case UNDO:

			this.state = new UndoState();

			break;

		case CONFIRMED:

			this.state = new ConfirmedState();

			break;

		case CANCELLED:

			this.state = new CancelledState();

			break;

		default:

			new BrokerException();

			break;

		}

	}



	public void process() {

		this.state.process(this);

	}



	public boolean cancelRoom() {

		return getRoomConfirmation() != null && getRoomCancellation() == null;

	}



	public boolean cancelActivity() {

		return getActivityConfirmation() != null && getActivityCancellation() == null;

	}



	public boolean cancelPayment() {

		return getPaymentConfirmation() != null && getPaymentCancellation() == null;

	}

}

