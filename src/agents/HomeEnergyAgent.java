package agents;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;

public class HomeEnergyAgent extends Agent {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("hh:mm:ss"); 
	
	private static String padRight (String s, int n) {
		// Because Java doesn't have a sensible native way to pad strings
		return String.format("%1$-" + n + "s", s);  
	}
	
	protected void setup () {
		// Print a message so we know the agent has come online
		log("Hello World!");
	}
	
	protected void log (String message) {
		// Print the time, the agent's name and the message
		System.out.println(
			DATE_FORMAT.format(new Date()) + 
			" " + padRight(getLocalName(), 22) + " " + message
		);
	}
	
	protected void log (Object messageObject) {
		// Wrapper that handles any type, like System.out.println() does
		log(messageObject.toString());
	}
	
	private void logMessage (String direction, String content, int performative, String localName) {
		// Log the details of a message, including the direction (inbound or outbound), content
		// performative (ie. INFORM, AGREE) and sender/receiver name
		log(
			direction + padRight(localName, 22) + " [" + 
			ACLMessage.getPerformative(performative) +
			"] \"" + content + "\""
		);
	}
	
	private void logMessageOutbound (String content, int performative, String receiver) {
		logMessage("-> ", content, performative, receiver);
	}
	
	private void logMessageInbound (String content, int performative, String sender) {
		logMessage("<- ", content, performative, sender);
	}
	
	protected void sendReply (String content, int performative, ACLMessage message) {
		ACLMessage reply = message.createReply();
		reply.setPerformative(performative);
		reply.setContent(content);
		logMessageOutbound(
			content, performative,
			message.getSender().getLocalName()
		);
		send(reply);
	}
	
	protected void sendMessage (String content, int performative, String[] receivers) {
		// Construct and send an outbound message to the specified receiver(s)
		ACLMessage message = new ACLMessage(performative);
		message.setContent(content);
		for (String receiver : receivers) {
			// Log each recipient separately, even though it's technically the one message
			logMessageOutbound(content, performative, receiver);
			message.addReceiver(new AID(receiver, AID.ISLOCALNAME));
		}
		send(message);
	}
	
	protected void sendMessage(String content, int performative, String receiver) {
		// Wrapper that takes a single receiver
		sendMessage(content, performative, new String[]{receiver});	
	}
	
	protected ACLMessage receiveMessage() {
		// Check for any messages in the inbound queue
		ACLMessage message = receive();
		if (message != null) {
			// Log the received message
			logMessageInbound(
				message.getContent(), 
				message.getPerformative(), 
				message.getSender().getLocalName()
			);
		}
		return message;
	}
	
	protected String[] findOtherAgents () {
		// Find the local names of all other agents
		ArrayList<String> result = new ArrayList<String>();
		try {
			SearchConstraints constraints = new SearchConstraints();
			constraints.setMaxResults((long) -1);
			AMSAgentDescription[] agents = AMSService.search(this, new AMSAgentDescription(), constraints);
			for (AMSAgentDescription agent : agents) {
				String name = ((AID) agent.getName()).getLocalName(); 
				// Filter out this agent and the three system agents
				if (!name.equals(getLocalName()) && !name.equals("ams") && !name.equals("df") && !name.equals("rma")) {
					result.add(name);
				}
			}
		} catch (FIPAException e) {
			e.printStackTrace();
			return null;
		}
		// Return the results as a String array
		return result.toArray(new String[0]);
	}
	
	protected void sendOffer (ACLMessage message, Double unitsToSell, Double pricePerUnit) {
		sendReply(unitsToSell + " " + pricePerUnit, ACLMessage.PROPOSE, message);
		log(
			"Sent offer to " + message.getSender().getLocalName() + ": " + 
			String.format("%,.2f", unitsToSell)  + " units for $" + 
			String.format("%,.2f", pricePerUnit) + " each."
		);
	}
	
	protected TradeOffer parseOffer (ACLMessage message) {
		if (message.getPerformative() == ACLMessage.PROPOSE) {
			String[] content = message.getContent().split(" ");
			try {
				String name = message.getSender().getLocalName();
				Double unitsToSell  = Double.parseDouble(content[0]);
				Double pricePerUnit = Double.parseDouble(content[1]);
				log(
					"Received offer from " + name + ": " + 
					String.format("%,.2f", unitsToSell)  + " units for $" + 
					String.format("%,.2f", pricePerUnit) + " each."
				);
				// Create and return a trade offer
				return new TradeOffer(name, pricePerUnit, unitsToSell);
			}
			catch (NumberFormatException e) {
				// The value(s) couldn't be parsed correctly, just let the method return null
				log("Error: Could not parse offer.");
			}
		}
		return null;
	}

}