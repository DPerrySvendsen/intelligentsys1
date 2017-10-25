package agents;

import java.util.ArrayList;
import java.util.HashMap;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class ElectricityTrader extends HomeEnergyAgent {
	private ArrayList<String> retailerAgents          = new ArrayList<String>();
	private HashMap<String, TradeOffer> offers = new HashMap<String, TradeOffer>();
	private int pendingResponses;
	
	private int unitStock;
	private int unitUsageRate;
	private int unitApplianceRequest;
	private int unitsRequired;
	
	private double maxBuyPrice;
	private boolean isRequestSent;
	
	protected void setup () {
		super.setup();
		
		identifyRetailers();
		
		unitStock = 200;
		// unitUsageRate now dynamically updated by appliances
		unitUsageRate = 0;
		
		unitsRequired = 100;
		maxBuyPrice   = 0.30D;
		
		isRequestSent = false;
		
		log(unitStock + " starting units. Usage rate at: " + unitUsageRate + " units.");
		log(unitsRequired + " units required. Maximum buy price: " + formatAsPrice(maxBuyPrice));
		
		addBehaviour(new CyclicBehaviour(this) {
			public void action () {
				checkMessages();
			}
		});
	}
	
	private void initialiseTradingBehaviour () {
		//Timed behaviour at each second
		addBehaviour(new TickerBehaviour(this, 1000) {
			protected void onTick () {
				consumeUnits();
				updateRate();
			}
		});
		
	}

	private void identifyRetailers () {
		String[] otherAgents = findOtherAgents();
		pendingResponses = otherAgents.length;
		for (String name : otherAgents) {
			sendMessage("Are you a retailer?", ACLMessage.QUERY_IF, name);
		}
	}
	
	private void requestOffers () {
		// Find all other agents
		pendingResponses = retailerAgents.size();
		for (String name : retailerAgents) {
			log("Requesting offer from " + name + "...");
			sendMessage("", ACLMessage.REQUEST, name);
			isRequestSent = true;
		}
	}
	
	private void checkMessages () {
		// Did the agent receive a message?
		ACLMessage message = receiveMessage();
		if (message == null) {
			return;
		}
		switch (message.getPerformative()) {
			case ACLMessage.PROPOSE:
				// Record the offer
				TradeOffer offer = parseOffer(message);
				if (offer != null) {
					offers.put(message.getSender().getLocalName(), offer);
				}
				break;
			case ACLMessage.INFORM:
				if (message.getContent().contains("retailer")) {
					pendingResponses--;
					if (message.getContent().contains("Yes")) {
						// Add this agent to the list of retailers
						String name = message.getSender().getLocalName();
						log("Identified " + name + " as a retailer.");
						retailerAgents.add(name);
					}
					if (pendingResponses == 0) {
						// We've identified all the other agents, launch the trading behaviour
						log("All other agents have been identified.");
						initialiseTradingBehaviour();
					}
					return;
				}
				// Trader has received consumption data from an Appliance, should this be in checkOffers?
				int newConsumption = Integer.parseInt(message.getContent());
				unitUsageRate += newConsumption;
				break;
			case ACLMessage.FAILURE:
				// Agent could not provide an offer, remove the agent from the list of other agents
				String name = message.getSender().getLocalName();
				if(!name.contains("Appliance")){
					log("No offer from " + name);
				}
				pendingResponses--;
		};
		// Has an offer been received from all other agents?
		if (pendingResponses == 0) {
			processOffers();
			offers.clear();
		}
	}
	
	private void processOffers () {

		// Log all offers
		log("Got all offers:");
		for (TradeOffer offer : offers.values()) {
			log(
				"Offer from " + offer.getName() + ": " + 
				padLeft(String.format( "%,.0f", offer.getUnitsToSell()),  3) + " units for " + 
				padLeft(formatAsPrice(offer.getPricePerUnit()), 6) + " per unit. Total price: " +
				padLeft(formatAsPrice(offer.getPriceTotal()), 10)
			);
		}

		// Determine the offer with the cheapest price per unit
		TradeOffer bestOffer = null;
		for (TradeOffer offer : offers.values()) {
			if (bestOffer == null || offer.getPricePerUnit() < bestOffer.getPricePerUnit()) {
				bestOffer = offer;
			}
		}
		if (bestOffer == null) {
			return;
		}
		log("The best offer is " + formatAsPrice(bestOffer.getPricePerUnit()) + " per unit from " + bestOffer.getName());

		// Reject all other offers
		for (TradeOffer offer : offers.values()) {
			if (offer != bestOffer) {
				sendMessage("", ACLMessage.REFUSE, offer.getName());
				log("Declined offer from " + offer.getName());
			}
		}
		
		// Is the best offer price within the acceptable range?
		if (bestOffer.getPricePerUnit() > maxBuyPrice) {
			// Reject the best offer
			sendMessage("", ACLMessage.REFUSE, bestOffer.getName());
			log("Declined best offer from " + bestOffer.getName());
		}
		else {
			// Accept the best offer, purchase the units required or the units on offer, whichever is lower
			int unitsToBuy = (int) Math.min(unitsRequired, bestOffer.getUnitsToSell());
			sendMessage("" + unitsToBuy, ACLMessage.AGREE, bestOffer.getName());
			log("Accepted best offer from " + bestOffer.getName() + ". Purchased " + unitsToBuy +  
				" units for " + formatAsPrice(unitsToBuy * bestOffer.getPricePerUnit()));
			unitsRequired -= unitsToBuy;
			unitStock += unitsToBuy;
		}
		
		offers.clear();
		isRequestSent = false;
		
		if (unitsRequired == 0) {
			log("Power requirements have been met.");
		}
		else {
			log(unitsRequired + " units still required.");
			requestOffers();
		}
	}

	// Called onTick by the TickerBehaviour
	private void consumeUnits () {
		int unitsLost = unitUsageRate;
		
		unitStock     -= unitsLost;
		unitsRequired += unitsLost;
		log("Consumed " + unitsLost + " units. " + unitStock + " remaining.");
		
		// Request an offer when units are running low, deny if a request has already been made
		if (unitStock < unitsRequired && !isRequestSent) {
			requestOffers();
		}
	}
	
	// Hook to update usage rate when appliances are turned on
	private void updateRate() {
		unitUsageRate = unitApplianceRequest;
		unitApplianceRequest = 0;
	}
}