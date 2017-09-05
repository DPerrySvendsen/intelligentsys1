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
				if (message != null) {
					// When we receive a message, send back an acknowledgment
					sendReply(message, ACLMessage.AGREE, "ACK");
				}
			}
		});
	
	}

}
