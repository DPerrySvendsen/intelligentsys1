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
		
		//Arguments.
		Object[] args = getArguments();
		
		//Initialise specific calculations depending on what the retailer is to do.
		//@param aType a integer to define what type of retailer it is. Collected from csv configuration.
		setupRetailerType(args[0]);
		
		sellPriceMax = calculateMaxPrice();
		sellPriceMin = calculateMinPrice();
		productionRate = 35;
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
				unitsHeld += productionRate;
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
	private void setupRetailerType(Object aType) {
		String s = String.valueOf(aType);
		retailerType = Integer.parseInt(s);
	}
	
	//Sets the minimum price of a unit based on the current simulated time
	public Double calculateMinPrice() {
		
		switch(retailerType) {
		
		case 0:		//fixed minimum price
			sellPriceMin = 0.025D;
			break;
			
		case 1:		//time-based pricing
			//An array of the min prices the retailer will sell a unit for	
			Double minPrices[] = new Double[24];
			
			//The index represents the respective hour and the value is the min sell price
			//Eg. minPrices[15] = 16 means that at 3pm the min sell price is $16
			//Index is mapped to a peak design
			minPrices[0] = 0.02D;		//12am
			minPrices[1] = 0.01D;
			minPrices[2] = 0.01D;
			minPrices[3] = 0.01D;
			minPrices[4] = 0.01D;
			minPrices[5] = 0.02D;
			minPrices[6] = 0.03D;		//6am
			minPrices[7] = 0.05D;
			minPrices[8] = 0.07D;
			minPrices[9] = 0.07D;
			minPrices[10] = 0.06D;
			minPrices[11] = 0.06D;
			minPrices[12] = 0.08D;	//12pm
			minPrices[13] = 0.05D;
			minPrices[14] = 0.04D;
			minPrices[15] = 0.03D;
			minPrices[16] = 0.03D;
			minPrices[17] = 0.04D;
			minPrices[18] = 0.06D;	//6pm
			minPrices[19] = 0.08D;
			minPrices[20] = 0.08D;
			minPrices[21] = 0.06D;
			minPrices[22] = 0.04D;
			minPrices[23] = 0.03D;	//11pm
			
			for(int i = 0; i < minPrices.length; i++){
				if(getCurrentHour() == i) {
					sellPriceMin = minPrices[i];
				}
			}
			break;
			
		case 2:		
			//Adaptive supply pricing
			sellPriceMin = (double)1 - ((unitsHeld / 10) * 0.01);
			if(sellPriceMin < 0.02D){
				sellPriceMin = 0.01D;
			}
			break;
			
		default:
			//default to fixed minimum price
			log(getLocalName() + " did not get a type number! Defaulting to fixed range.");
			sellPriceMin = 0.025D;
			break;
		}
		
		return sellPriceMin;
	}
	
	//Sets the maximum price of a unit based on the current simulated time
	public Double calculateMaxPrice() {
		
		switch(retailerType) {
		
		case 0:		//fixed maximum price
			sellPriceMax = 0.025D;
			break;
			
		case 1:		//time-based pricing
			//An array of the max prices the retailer will sell a unit for	
			Double maxPrices[] = new Double[24];
			
			//The index represents the respective hour and the value is the max sell price
			//Eg. minPrices[15] = 16 means that at 3pm the max sell price is $16
			maxPrices[0] = 0.12D;
			maxPrices[1] = 0.06D;
			maxPrices[2] = 0.03D;
			maxPrices[3] = 0.03D;
			maxPrices[4] = 0.03D;
			maxPrices[5] = 0.06D;
			maxPrices[6] = 0.09D;
			maxPrices[7] = 0.21D;
			maxPrices[8] = 0.24D;
			maxPrices[9] = 0.24D;
			maxPrices[10] = 0.21D;
			maxPrices[11] = 0.18D;
			maxPrices[12] = 0.3D;
			maxPrices[13] = 0.18D;
			maxPrices[14] = 0.15D;
			maxPrices[15] = 0.12D;
			maxPrices[16] = 0.12D;
			maxPrices[17] = 0.12D;
			maxPrices[18] = 0.24D;
			maxPrices[19] = 0.33D;
			maxPrices[20] = 0.3D;
			maxPrices[21] = 0.24D;
			maxPrices[22] = 0.21D;
			maxPrices[23] = 0.15D;
			
			for(int i = 0; i < maxPrices.length; i++){
				if(getCurrentHour() == i) {
					sellPriceMax = maxPrices[i];
				}
			}
			break;
		
		case 2:		//Adaptive supply pricing
			sellPriceMax = (double)1 - ((unitsHeld / 10) * 0.009);
			if(sellPriceMax < 0.02D){
				sellPriceMax = 0.01D;
			}
			break;
			
		default:
			//default to some maximum price
			log(getLocalName() + " did not get a type number! Defaulting to fixed range.");
			sellPriceMax = 0.025D;
			break;
		}
		
		return sellPriceMax;
	}
	
	private void processMessage (ACLMessage message) {
		switch (message.getPerformative()) {
			case ACLMessage.REQUEST:
				if (unitsHeld > 0) {
					double price = calculatePriceOffer();
					int units = calculateUnitOffer();
					
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
		}
	}
	
	private int calculateUnitOffer(){
		int result = (int)(Math.random() * (unitsHeld - 1) + 1);
		
		return result;
	}
	
	// Calculate prices based on retailer type.
	private double calculatePriceOffer(){
		double result = 0D;
		
		switch(retailerType){
		case 0:
			result = sellPriceMin;
			break;
		case 1:
			result = Math.random() * (sellPriceMax - sellPriceMin) + sellPriceMin;
			break;
		case 2:
			result = Math.random() * (sellPriceMax - sellPriceMin) + sellPriceMin;
			break;
		default:
			break;
		}
		
		return result;
	}
}
