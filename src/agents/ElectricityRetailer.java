package agents;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class ElectricityRetailer extends HomeEnergyAgent {

	private int unitsHeld;
	private double sellPriceMax;
	private double sellPriceMin;
	
	protected void setup () {
		super.setup();
		
		unitsHeld    = 40;
		sellPriceMax = 50;
		sellPriceMin = 10;
		
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
