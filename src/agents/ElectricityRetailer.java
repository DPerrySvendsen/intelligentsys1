package agents;

import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class ElectricityRetailer extends HomeEnergyAgent {

	protected void setup () {
		super.setup();
		
		addBehaviour(new TickerBehaviour(this, 1 * 1000){
			public void onTick() {
				// Every second, check the received messages
				ACLMessage message = receiveMessage();
				if (message != null && message.getPerformative() == ACLMessage.REQUEST) {
					sendOffer(message, Math.floor(Math.random() * 50), Math.random() * 10);
				}
			}
		});
	}
}
