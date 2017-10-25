package agents;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class Appliance extends HomeEnergyAgent {
	/**
	 * 
	 */
	// hard coded Trader, fix to make more flexible
	private String myTrader = "ElectricityTrader1";
	private int applianceType;
	private int[][] demandMatrix;
	private String[] typeList;

	protected void setup() {
		super.setup();

		typeList = new String[] {
			"Toaster", "AirCon", "Fridge", "Microwave", "CoffeeMachine", "Lamp", 
			"PhoneCharger", "WashingMachine", "Dryer", "TV", "SolarPanel"
		};

		// 13 x 48 matrix of demand values for different appliances. 13 types of appliance, 48 time periods for demand.
		demandMatrix = new int[][] {
		    //                     00:00              03:00              06:00              09:00              12:00	             15:00              18:00              21:00
			/* Toaster        */ { 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 5, 5, 5, 1,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0 }, 
			/* AirCon         */ { 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  9, 9, 9, 8, 9, 9,  9, 7, 9, 8, 8, 9,  7, 6, 5, 6, 4, 5,  0, 0, 0, 0, 0, 0 }, 
			/* Fridge         */ { 2, 1, 2, 1, 2, 1,  2, 0, 1, 0, 1, 0,  1, 2, 2, 3, 2, 3,  2, 2, 1, 1, 2, 1,  2, 1, 1, 1, 2, 1,  1, 2, 1, 1, 2, 1,  2, 2, 2, 3, 1, 2,  1, 0, 1, 0, 1, 0 }, 
			/* Microwave      */ { 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 5, 2, 2,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  2, 2, 4, 0, 0, 0,  3, 6, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0 }, 
			/* CoffeeMachine  */ { 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 6, 3, 0, 0,  0, 0, 0, 6, 3, 0,  0, 0, 4, 0, 0, 0,  3, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0 }, 
			/* Lamp           */ { 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 2, 2, 2, 2,  2, 2, 2, 2, 2, 0 }, 
			/* PhoneCharger   */ { 1, 0, 1, 0, 1, 0,  1, 0, 0, 1, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 1, 1, 1, 1 }, 
			/* WashingMachine */ { 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 6, 6, 6, 0,  0, 0, 8, 8, 8, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0 }, 
			/* Dryer          */ { 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 9,  9, 9, 9, 0, 0, 9,  9, 9, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0 }, 
			/* TV             */ { 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 3, 3, 3, 3, 3,  3, 3, 3, 3, 3, 3,  3, 3, 3, 3, 0, 0 }, 
			/* SolarPanel     */ { 0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0,  0, 0, 0, 0,-1,-1, -1,-2,-2,-2,-3,-3, -3,-4,-4,-3,-2,-2, -1,-1, 0, 0, 0, 0,  0, 0, 0, 0, 0, 0 }
		};

		// Assign appliance to correct behaviour
		for (int id = 0; id < typeList.length; id++) {
			if (getLocalName() == typeList[id]) {
				applianceType = id;
				break;
			}
		}
			
		// Has ability to process messages 
		addBehaviour(new CyclicBehaviour(this){
			public void action () {
				// Every second, check the received messages
				ACLMessage message = receiveMessage();
				if (message != null) {
					processMessage(message);
				}
			}
		});

		//Timed behaviour at each second
		addBehaviour(new TickerBehaviour(this, 1000) {
			protected void onTick () {
				int unitsUsed = demandMatrix[applianceType][(int) (getSimulatedHalfHour() * 2)];
				if (unitsUsed != 0) {
					sendConsumption(unitsUsed);
				}
			}
		});

	}

	private void processMessage (ACLMessage message) {
		switch (message.getPerformative()) {
			case ACLMessage.QUERY_IF:
				sendReply("No, I am not a retailer", ACLMessage.INFORM, message);
				break;
		}
	}

	private void sendConsumption (int consumedAmount) {
		sendMessage(Integer.toString(consumedAmount), ACLMessage.INFORM, myTrader);
	}

}
