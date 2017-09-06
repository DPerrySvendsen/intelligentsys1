package agents;

import java.util.HashMap;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class ElectricityTrader extends HomeEnergyAgent {
	
	private String[] otherAgents;
	private HashMap<String, TradeOffer> offers = new HashMap<String, TradeOffer>();
	
	protected void setup () {
		super.setup();
		
		otherAgents = findOtherAgents();
		
		addBehaviour(new CyclicBehaviour(this){
			public void action() {
				checkOffers();
			}
		});
		
		requestOffers ();
	}
	
	private void requestOffers () {
		for (String name : otherAgents) {
			log("Requesting offer from " + name + "...");
			sendMessage("", ACLMessage.REQUEST, name);
		}
	}
	
	private void checkOffers () {
		ACLMessage message = receiveMessage();
		if (message != null) {
			TradeOffer offer = parseOffer(message);
			if (offer != null) {
				offers.put(message.getSender().getLocalName(), offer);
			}
		}
		if (offers.size() == otherAgents.length) {
			log("Got all offers:");
			for (TradeOffer offer : offers.values()) {
				log(
					"Offer from " + offer.getName() + ": " + String.format("%,.0f", offer.getUnitsToSell()) + 
					" units for $"    + String.format("%,.2f", offer.getPricePerUnit()) + " per unit." + 
					" Total price: $" + String.format("%,.2f", offer.getPriceTotal())
				);
			}
			offers.clear();
		}
	}
	
}