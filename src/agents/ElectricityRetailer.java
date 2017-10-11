package agents;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;


public class ElectricityRetailer extends HomeEnergyAgent {

	private double unitsHeld = 0;
	private double productionRate = 0;
	private double sellPriceMax = 0;
	private double sellPriceMin = 0;
	
	protected void setup () {
		super.setup();
		
		sellPriceMax = calculateMaxPrice();
		sellPriceMin = calculateMinPrice();
		productionRate = calculateProductionRate();
		unitsHeld = calculateUnitsHeld();
		
		
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
				//log(getScaledTime() + "Produced " + productionRate + " units. " + unitsHeld + " units in hold.");
			}
		});
		
		//Timed behaviour. Update relevant values every hour of simulated time
				addBehaviour(new TickerBehaviour(this, (60/getTimeScale() * 1000 )) {
					protected void onTick() {
						calculateMinPrice();
						calculateMaxPrice();
						calculateProductionRate();
						calculateUnitsHeld();
					}
				});
	}
	
	//Sets the minimum price of a unit based on the current simulated time
	public Double calculateMinPrice() {
		//An array of the min prices the retailer will sell a unit for	
		Double minPrices[] = new Double[24];
		
		
		//The index represents the respective hour and the value is the min sell price
		//Eg. minPrices[15] = 16 means that at 3pm the min sell price is $16
		minPrices[0] = (double) 13;
		minPrices[1] = (double) 8;
		minPrices[2] = (double) 7;
		minPrices[3] = (double) 5;
		minPrices[4] = (double) 5;
		minPrices[5] = (double) 7;
		minPrices[6] = (double) 16;
		minPrices[7] = (double) 20;
		minPrices[8] = (double) 18;
		minPrices[9] = (double) 16;
		minPrices[10] = (double) 15;
		minPrices[11] = (double) 13;
		minPrices[12] = (double) 12;
		minPrices[13] = (double) 12;
		minPrices[14] = (double) 14;
		minPrices[15] = (double) 16;
		minPrices[16] = (double) 18;
		minPrices[17] = (double) 22;
		minPrices[18] = (double) 25;
		minPrices[19] = (double) 27;
		minPrices[20] = (double) 23;
		minPrices[21] = (double) 20;
		minPrices[22] = (double) 18;
		minPrices[23] = (double) 15;
		
		for(int i = 0; i < minPrices.length; i++){
			if(getCurrentHour() == i) {
				sellPriceMin = minPrices[i];
			}
		}
		
		return sellPriceMin;
	}
	
	//Sets the maximum price of a unit based on the current simulated time
	public Double calculateMaxPrice() {
		//An array of the max prices the retailer will sell a unit for	
		Double maxPrices[] = new Double[24];
		
		//The index represents the respective hour and the value is the max sell price
		//Eg. minPrices[15] = 16 means that at 3pm the max sell price is $16
		maxPrices[0] = (double) 33;
		maxPrices[1] = (double) 28;
		maxPrices[2] = (double) 27;
		maxPrices[3] = (double) 25;
		maxPrices[4] = (double) 25;
		maxPrices[5] = (double) 27;
		maxPrices[6] = (double) 36;
		maxPrices[7] = (double) 40;
		maxPrices[8] = (double) 38;
		maxPrices[9] = (double) 36;
		maxPrices[10] = (double) 35;
		maxPrices[11] = (double) 33;
		maxPrices[12] = (double) 32;
		maxPrices[13] = (double) 32;
		maxPrices[14] = (double) 34;
		maxPrices[15] = (double) 36;
		maxPrices[16] = (double) 38;
		maxPrices[17] = (double) 42;
		maxPrices[18] = (double) 45;
		maxPrices[19] = (double) 47;
		maxPrices[20] = (double) 43;
		maxPrices[21] = (double) 40;
		maxPrices[22] = (double) 38;
		maxPrices[23] = (double) 35;
		
		for(int i = 0; i < maxPrices.length; i++){
			if(getCurrentHour() == i) {
				sellPriceMax = maxPrices[i];
			}
		}
		
		return sellPriceMax;
	}
	
	public Double calculateProductionRate(){
		productionRate = 10 + 2 * sellPriceMin;
		return productionRate;
	}
	
	public Double calculateUnitsHeld() {
		unitsHeld = sellPriceMin * 10;
		return unitsHeld;
	}
	
	private void processMessage (ACLMessage message) {
		switch (message.getPerformative()) {
			
			case ACLMessage.REQUEST:
				if (unitsHeld > 0) {
					sendOffer(
						message,
						// Random amount of units held
						(int) (Math.random() * (unitsHeld - 1) + 1),
						// Random price within range
						Math.random() * (sellPriceMax - sellPriceMin) + sellPriceMin
					);
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
}
