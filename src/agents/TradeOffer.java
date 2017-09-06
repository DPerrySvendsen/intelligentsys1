package agents;

public class TradeOffer {
	
	private String name;
	private double pricePerUnit;
	private double unitsToSell;
	
	public TradeOffer (String pName, double pPricePerUnit, double pUnitsToSell) {
		name         = pName;
		pricePerUnit = pPricePerUnit;
		unitsToSell  = pUnitsToSell;
	} 
	
	public String getName () {
		return name;
	}
	
	public double getPricePerUnit () {
		return pricePerUnit;
	} 
	
	public double getUnitsToSell () {
		return unitsToSell;
	}
	
	public double getPriceTotal () {
		return unitsToSell * pricePerUnit;
	}
	
}