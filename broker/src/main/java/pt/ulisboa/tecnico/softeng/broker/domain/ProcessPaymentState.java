package pt.ulisboa.tecnico.softeng.broker.domain;

import pt.ulisboa.tecnico.softeng.bank.exception.BankException;
import pt.ulisboa.tecnico.softeng.broker.domain.Adventure.State;
import pt.ulisboa.tecnico.softeng.broker.exception.RemoteAccessException;
import pt.ulisboa.tecnico.softeng.broker.interfaces.BankInterface;
import pt.ulisboa.tecnico.softeng.broker.interfaces.TaxInterface;
import pt.ulisboa.tecnico.softeng.tax.dataobjects.InvoiceData;
import pt.ulisboa.tecnico.softeng.tax.exception.TaxException;
import org.joda.time.LocalDate;

public class ProcessPaymentState extends AdventureState {
	public static final int MAX_REMOTE_ERRORS = 3;

	@Override
	public State getState() {
		return State.PROCESS_PAYMENT;
	}

	@Override
	public void process(Adventure adventure) {
		try {
			adventure.setPaymentConfirmation(BankInterface.processPayment(adventure.getIBAN(), adventure.getAmount()*(1+adventure.getMargin())));
		} catch (BankException be) {
			System.out.println("1");
			adventure.setState(State.CANCELLED);
			return;
		} catch (RemoteAccessException rae) {
			incNumOfRemoteErrors();
			if (getNumOfRemoteErrors() == MAX_REMOTE_ERRORS) {
				System.out.println("2");
				adventure.setState(State.CANCELLED);
			}
			return;
		}

		resetNumOfRemoteErrors();

		try{
			adventure.setTaxConfirmation(TaxInterface.submitInvoice(
					new InvoiceData(adventure.getBroker().getSeller(), adventure.getNIF(),
							"ADVENTURE", adventure.getAmount(),new LocalDate().now())));
		}
		catch (TaxException te){
			System.out.println("3");
			adventure.setState(State.CANCELLED);
			return;
		} catch (RemoteAccessException rae) {
			incNumOfRemoteErrors();
			if (getNumOfRemoteErrors() == MAX_REMOTE_ERRORS) {
				System.out.println("4");
				adventure.setState(State.CANCELLED);
			}
			return;
		}

		adventure.setState(State.CONFIRMED);
	}

}
