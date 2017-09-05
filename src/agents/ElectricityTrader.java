package agents;

import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class ElectricityTrader extends HomeEnergyAgent {

	protected void setup () {
		super.setup();
		
		addBehaviour(new TickerBehaviour(this, 5 * 1000){
			public void onTick() {
				// Every five seconds, send a message to the other agents
				for (String name : findOtherAgents()) {
					sendMessage("SYN", ACLMessage.INFORM, name);
				}
			}
		});
		
		addBehaviour(new TickerBehaviour(this, 1 * 1000){
			public void onTick() {
				// Every second, check the received messages, but don't do anything with them
				receiveMessage();
			}
		});
	}
	
}