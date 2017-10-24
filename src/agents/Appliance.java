package agents;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class Appliance extends HomeEnergyAgent {
	// hard coded Trader, fix to make more flexible
	private String myTrader = "ElectricityTrader1";
	private int applianceType;
	// demandMatrix is a 13 x 48 matrix of demand values for different appliances 13 types of appliance, 48 time periods for demand
	private int[][] demandMatrix;
	private String[] typeList;
	private boolean consumptionSent;
	private int oldTime;
	
	protected void setup() {
		super.setup();
		
		// myAgent = ElectricityTrader1;
		typeList = new String[] {"Toaster", "Conditioner of Air", "Fridge", "Microwave", "Coffee Machine", "Lamp", "Phone Charger", "Washing Machine", "Dryer", "TV", "Solar Panels"};
		
		// demand matrix for types
		//							00:00		  03:00		   06:00	    09:00		 12:00		  15:00		   18:00		21:00
		demandMatrix = new int[][] {{0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,5,5,5,1, 0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,0,0,0},
									{0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,0,0,0, 9,9,9,8,9,9, 9,7,9,8,8,9, 7,6,5,6,4,5, 0,0,0,0,0,0},
									{2,1,2,1,2,1, 2,0,1,0,1,0, 1,2,2,3,2,3, 2,2,1,1,2,1, 2,1,1,1,2,1, 1,2,1,1,2,1, 2,2,2,3,1,2, 1,0,1,0,1,0},
									{0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,5,2,2, 0,0,0,0,0,0, 0,0,0,0,0,0, 2,2,4,0,0,0, 3,6,0,0,0,0, 0,0,0,0,0,0},
									{0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,6,3,0,0, 0,0,0,6,3,0, 0,0,4,0,0,0, 3,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,0,0,0},
									{0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,2,2,2,2, 2,2,2,2,2,0},
									{1,0,1,0,1,0, 1,0,0,1,0,0, 0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,1,1,1,1},
									{0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,6,6,6,0, 0,0,8,8,8,0, 0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,0,0,0},
									{0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,0,0,9, 9,9,9,0,0,9, 9,9,0,0,0,0, 0,0,0,0,0,0, 0,0,0,0,0,0},
									{0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,0,0,0, 0,3,3,3,3,3, 3,3,3,3,3,3, 3,3,3,3,0,0},
									{0,0,0,0,0,0, 0,0,0,0,0,0, 0,0,0,-1,-3,-6, -6,-6,-6,-6,-7,-9, -9,-10,-12,-15,-15,-18, -18,-18,-17,-15,-12,-10, -8,-5,-2,0,0,0, 0,0,0,0,0,0}};
		
		// has ability to process messages 
		addBehaviour(new CyclicBehaviour(this){
			public void action() {
				// Every second, check the received messages
				ACLMessage message = receiveMessage();
				if (message != null) {
					processMessage(message);
				}
			}
		});
		
		//Timed behaviour at each second
		addBehaviour(new TickerBehaviour(this, 1000) {
			protected void onTick() {
				// ISSUE: getCurrentHour() returns different value from formatted simulated time so clocks are out by 10 hours
				// log("It is " + getCurrentHour() );
				int unitsUsed = demandMatrix[applianceType][getCurrentHour()*2];
				if (unitsUsed != 0) {
					sendConsumption(unitsUsed);
				}
			}
		});
		
		// randomly assign what kind of appliance this is
		int arraySize = typeList.length;
		Double appTypeDouble = Math.random()*typeList.length;
		applianceType = appTypeDouble.intValue();
		// testify!
		log("I am " + typeList[applianceType]);
		
	}
	
	private void processMessage (ACLMessage message) {
		switch (message.getPerformative()) {
			// if it gets a request it automatically refuses
			// if we were to implement battery storage appliance we would have to change this 
			case ACLMessage.REQUEST:
					sendReply("", ACLMessage.FAILURE, message);
					log("I am " + typeList[applianceType] + ", I can't provide an offer to " + message.getSender().getLocalName() + ".");
				break;
				
			case ACLMessage.AGREE:
				// Do nothing
				break;

			case ACLMessage.REFUSE:
				// For now, do nothing
				break;
				
		}
	}
	
	private void sendConsumption(int consumedAmount) {
		sendMessage(Integer.toString(consumedAmount), ACLMessage.INFORM, myTrader);
		// log for debugging
		// log(theTime + typeList[applianceType] + " used " + consumedAmount + " unit(s) of power.");
	}
	
}
