package agents;

import java.util.ArrayList;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class ElectricityRetailer extends HomeEnergyAgent {

	private int retailerType = 0;
	
	private double unitsHeld = 0;
	private double productionRate = 0;
	private double sellPriceMax = 0;
	private double sellPriceMin = 0;
	
	protected void setup () {
		super.setup();
		
		// Arguments
		Object[] args = getArguments();
		
		// Initialise specific calculations depending on what the retailer is to do.
		// @param aType a integer to define what type of retailer it is. Collected from csv configuration.
		setupRetailerType(args[0]);
		
		sellPriceMax = calculateMaxPrice();
		sellPriceMin = calculateMinPrice();
		unitsHeld = 300;
		
		log(unitsHeld + " units held. Sell price range: " + formatAsPrice(sellPriceMin) + " - " + formatAsPrice(sellPriceMax));
		
		addBehaviour(new CyclicBehaviour(this){
			public void action() {
				// Every second, check the received messages
				ACLMessage message = receiveMessage();
				if (message != null) {
					processMessage(message);
				}
			}
		});
		
		//Timed behaviour. Produce more units every 5 seconds
		addBehaviour(new TickerBehaviour(this, 5000) {
			protected void onTick() {
				generatePowerUnits();
				log("Produced " + productionRate + " units. " + unitsHeld + " units in hold.");
			}
		});
		
		//Timed behaviour. Update relevant values every hour of simulated time
		addBehaviour(new TickerBehaviour(this, (60/getTimeScale() * 1000 )) {
			protected void onTick() {
				calculateMinPrice();
				calculateMaxPrice();
			}
		});
	}
	
	/**
	 * Retailer Types:
	 * 0: Be just a retailer that sells on a fixed price.
	 * 1: Sell on a price that depends on the time of day.
	 * 2: Adaptive Supply Pricing; Lower price when supply is high and vice versa.
	 * 
	 * @param aType collect from the config on how the retailer is to work.
	 */
	private void setupRetailerType (Object aType) {
		retailerType = Integer.parseInt(String.valueOf(aType));
	}
	
	// Sets the minimum price of a unit based on the current simulated time
	public Double calculateMinPrice () {
		switch (retailerType) {
		
			// Fixed minimum price
			case 0:		
				sellPriceMin = 0.2D;
				break;
			
			// Time-based pricing
			case 1:		
				//An array of the min prices the retailer will sell a unit for	
				Double minPrices[] = new Double [] {
					0.02, 0.01, 0.01, 0.01, 0.01, 0.02, // 12am -  5am
					0.03, 0.05, 0.07, 0.07, 0.06, 0.06, //  6am - 11am
					0.08, 0.05, 0.04, 0.03, 0.03, 0.04, // 12pm -  5pm
					0.06, 0.08, 0.08, 0.06, 0.04, 0.03  //  6pm - 11pm
				};
				sellPriceMin = minPrices[getSimulatedHour()];
			break;

		// Adaptive supply pricing	
		case 2:		
			sellPriceMin = Math.max(1 - ((unitsHeld / 10) * 0.01), 0.02);
			break;

		// Default to fixed minimum price	
		default:
			log(getLocalName() + " did not get a type number! Defaulting to fixed range.");
			sellPriceMin = 0.2;
			break;
		}
		return sellPriceMin;
	}
	
	public Double calculateMaxPrice () {
		// Set the maximum price of a unit based on the current simulated time
		switch (retailerType) {

			// Fixed maximum price
			case 0: 
				sellPriceMax = 0.2D;
				break;

			// Time-based pricing
			case 1:
				// An array of the max prices the retailer will sell a unit for	
				Double maxPrices[] = new Double [] {
					0.12, 0.06, 0.03, 0.03, 0.03, 0.06, // 12am -  5am
					0.09, 0.21, 0.24, 0.24, 0.21, 0.18, //  6am - 11am
					0.30, 0.18, 0.15, 0.12, 0.12, 0.12, // 12pm -  5pm
					0.24, 0.33, 0.30, 0.24, 0.21, 0.15  //  6pm - 11pm
				};
				sellPriceMax = maxPrices[getSimulatedHour()];
				break;
		
			// Adaptive supply pricing
			case 2:		
				sellPriceMax = Math.max(1 - ((unitsHeld / 10) * 0.009), 0.02);
				break;
			
			default:
				// Default to some maximum price
				log(getLocalName() + " did not get a type number! Defaulting to fixed range.");
				sellPriceMax = 0.2D;
				break;
		}
		return sellPriceMax;
	}
	
	private void processMessage (ACLMessage message) {
		switch (message.getPerformative()) {
			case ACLMessage.REQUEST:
				if (unitsHeld > 0) {
					double price = calculatePriceOffer();
					int    units = calculateUnitOffer();
					
					// Test Cases
					if (isTestingEnabled()) {
						incrementIndex();
						switch (getTestIndex()) {
							case 1: price = testMultiBestCost();      break;
							case 2: price = testNoAcceptableOffers(); break;
						}
					}

					sendOffer(message, units, price);
					break;
				}
				else {
					sendReply("", ACLMessage.FAILURE, message);
					log("No units remaining, can't provide an offer to " + message.getSender().getLocalName() + ".");
				}
				break;
				
			case ACLMessage.AGREE:
				// Subtract the number of units purchased from the total
				int unitsSold = Integer.parseInt(message.getContent());
				unitsHeld -= unitsSold;
				log("Sold " + unitsSold + " units to " + message.getSender().getLocalName() + ". " + unitsHeld + " units remaining.");
				break;

			case ACLMessage.REFUSE:
				// For now, do nothing
				break;		
				
			case ACLMessage.QUERY_IF:
				sendReply("Yes, I am a retailer", ACLMessage.INFORM, message);
				break;
				
		}
	}
	
	private int calculateUnitOffer (){
		return (int) (Math.random() * (unitsHeld - 1) + 1);
	}
	
	// Calculate prices based on retailer type.
	private double calculatePriceOffer (){
		switch (retailerType) {
			case 0:
				return sellPriceMin;
			case 1:
				return Math.random() * (sellPriceMax - sellPriceMin) + sellPriceMin;
			case 2:
				return Math.random() * (sellPriceMax - sellPriceMin) + sellPriceMin;
		}
		return 0;
	}
	
	private void generatePowerUnits () {
		int hour = getSimulatedHour();
		// If between 6am and 8pm
		if (hour > 6 && hour < 20){
			productionRate = 35;
		} else {
			productionRate = 10;
		}
		// Add to supply
		unitsHeld += productionRate;
	}
	
	// Test cases
	
	// Two or more retailers have the same best cost
	private double testMultiBestCost () {
		log("Test case: testMultiBestCost");
		return 0.15;
	}
	
	// All retailers return offers outside the acceptable range
	private double testNoAcceptableOffers () {
		log("Test case: testNoAcceptableOffers");
		return 999;
	}
}
