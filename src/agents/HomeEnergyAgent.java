package agents;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.AMSService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;

public class HomeEnergyAgent extends Agent {

	private static final String           OUTPUT_FILENAME       = "output.csv";
	private static final SimpleDateFormat DATE_FORMAT_SIMULATED = new SimpleDateFormat("'Day' DD hh:mmaa");
	private static final SimpleDateFormat DATE_FORMAT_SYSTEM    = new SimpleDateFormat("hh:mm:ssaa"); 
	
	// One (system) second equals how many (simulated) minutes?
	private static long timeScale = 30;
	private static long startupTime = System.currentTimeMillis();
	
	public static String padRight (String s, int n) {
		// Because Java doesn't have a sensible native way to pad strings
		return String.format("%1$-" + n + "s", s);  
	}
	
	public static String padLeft (String s, int n) {
		// Because Java doesn't have a sensible native way to pad strings
		return String.format("%1$" + n + "s", s);  
	}
	
	public static String formatAsPrice (Double value) {
		return String.format("$%,.2f", value);	
	}
	
	protected void setup () {
		// Print a message so we know the agent has come online
		log("Hello World!");
		// Create the output file (or clear it if it already exists)
		createFile(OUTPUT_FILENAME);
	}
	
	private void createFile (String path) {
		try {
			BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(
					new FileOutputStream(path), "utf-8"
				)
			); 
			writer.close();
		}
		catch (IOException e) {}
	}
	
	private void writeToFile (String path, String content) {
		try {
			Files.write(Paths.get(path), content.getBytes(), StandardOpenOption.APPEND);
		} 
		catch (IOException e) {}
	}
	
	private void outputToCSV(String[] outputValues) {
		// Format the array of output values as comma-separated values 
		// and append them to the output file
		String outputLine = "";
		boolean addComma = false;
		for (String value : outputValues) {
			outputLine += (addComma ? "," : "") + value.replace(",", "");
			addComma = true;
		}
		outputLine += "\n";
		writeToFile(OUTPUT_FILENAME, outputLine);
	}
	
	protected void log (String message) {
		// Print the system time, simulated time, the agent's name and the message
		outputToCSV(new String [] {
			getFormattedSystemTime(),
			getFormattedSimulatedTime(),
			getLocalName(),
			message
		});
		String output = 
			getFormattedSystemTime() + " " + 
			getFormattedSimulatedTime() + " " +
			padRight(getLocalName(), 22) + " " + 
			message;
		System.out.println(output);
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
	
	protected void sendOffer (ACLMessage message, int unitsToSell, Double pricePerUnit) {
		sendReply(unitsToSell + " " + String.format("%.2f", pricePerUnit), ACLMessage.PROPOSE, message);
		log(
			"Sent offer to " + message.getSender().getLocalName() + ": " + 
			unitsToSell + " units for " + formatAsPrice(pricePerUnit) + " each."
		);
	}
	
	protected TradeOffer parseOffer (ACLMessage message) {
		if (message.getPerformative() == ACLMessage.PROPOSE) {
			String[] content = message.getContent().split(" ");
			try {
				// Create and return a trade offer
				TradeOffer offer = new TradeOffer(
					message.getSender().getLocalName(),
					Double.parseDouble(content[1]),
					Double.parseDouble(content[0])	
				);
				log(
					"Received offer from " + offer.getName() + ": " + 
					String.format("%,.0f", offer.getUnitsToSell()) + " units for " + 
					formatAsPrice(offer.getPricePerUnit()) + " each."
				);
				return offer;
			}
			catch (NumberFormatException e) {
				// The value(s) couldn't be parsed correctly, just let the method return null
				log("Error: Could not parse offer.");
			}
		}
		return null;
	}
	
	// Returns the system time in seconds since the program started
	public int getRunTime () {
		return (int) ((System.currentTimeMillis() - startupTime) / 100);
	}
	
	// Returns the formatted system time
	private String getFormattedSystemTime() {
		return DATE_FORMAT_SYSTEM.format(new Date());
	}
	
	// Returns the formatted simulated time
	private String getFormattedSimulatedTime() {
		return DATE_FORMAT_SIMULATED.format((System.currentTimeMillis() - startupTime) * 60 * timeScale);
	}
}