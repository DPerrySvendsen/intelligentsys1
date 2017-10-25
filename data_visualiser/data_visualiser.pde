  /*
  Reads in data from csv file 
  - Appliance agents power usage
  - Trader agent power remaining and consumed
  - Retailer agent offer amounts
  - Retailer agent successful offers and price paid
  
  csv file must be in project's data folder
  csv file cannot contain quotation marks
  
  */
  
  Table input;
  public String[] typeList;
  
  public Table[] applianceData;
  
  public Table traderUsage;
  public Table traderPurchases;
  public Table retailerOffers;
  
  private ApplianceDataSet applianceDataSet;
  private RetailerOfferSet retailerOfferSet;
  private TraderPurchaseSet traderPurchaseSet;
  private TraderUsageSet traderUsageSet;
  
  
  Button applianceDataButton, traderPurchaseButton, retailerOfferButton, traderUsageButton;
  
  
  void setup(){
    // initialise parameters for display
    fullScreen();
    pixelDensity(1);
    background(0);
    
    typeList = new String[] {"Toaster", "AirCon", "Fridge", "Microwave", "CoffeeMachine", "Lamp", "PhoneCharger", "WashingMachine", "Dryer", "TV", "SolarPanel"};
    
    applianceDataButton = new Button(displayWidth * 0.2, displayHeight * 0.9, "APPLIANCE DATA");
    traderPurchaseButton = new Button(displayWidth * 0.4, displayHeight * 0.9, "TRADER PURCHASES");
    retailerOfferButton = new Button(displayWidth * 0.6, displayHeight * 0.9, "RETAILER OFFERS");
    traderUsageButton = new Button(displayWidth * 0.8, displayHeight * 0.9, "TRADER USAGE");
    
    applianceDataSet = new ApplianceDataSet();
    retailerOfferSet = new RetailerOfferSet();
    traderPurchaseSet = new TraderPurchaseSet();
    traderUsageSet = new TraderUsageSet();
    
    input = loadTable("output.csv", "csv");
    println(input.getRowCount() + " total rows in table"); 
    
    // create array of tables for appliance data and parse input to give Table for each appliance usage and time 
    Table[] applianceData = new Table[typeList.length]; 
    
    for(int i = 0; i < typeList.length; i++){
      applianceData[i] = new Table();
      applianceData[i].addColumn("simTime");
      applianceData[i].addColumn("powerUsage");
    }
    
    for (int i = 0; i < typeList.length; i++){
      for (TableRow row : input.rows()) {
        
        String systemTime = row.getString(0);
        String simTime = row.getString(1);
        String sender = row.getString(2);
        String message = row.getString(3);
        
        if(sender.contains(typeList[i]) && message.contains("INFORM")){
          String[] values = split(message, "[INFORM] ");
          TableRow newRow = applianceData[i].addRow();
          newRow.setString("simTime", simTime);
          newRow.setInt("powerUsage", int(values[1]));
        }
      }
    }
    
    // parse input for retailer offers and store in table
    Table retailerOffers = new Table();
    retailerOffers.addColumn("retailer");
    retailerOffers.addColumn("simTime");
    retailerOffers.addColumn("quantity");
    retailerOffers.addColumn("price");
    
    for (TableRow row : input.rows()) {
      
      String systemTime = row.getString(0);
      String simTime = row.getString(1);
      String sender = row.getString(2);
      String message = row.getString(3);
      
      if(sender.contains("Retailer") && message.contains("Sent offer")){
        String[] toQuant1 = split(message, "der1: ");
        String[] toQuant2 = split(toQuant1[1], ' ');
        int quantity = int(toQuant2[0]);
        String[] toPrice1 = split(message, '$');
        String[] toPrice2 = split(toPrice1[1], ' ');
        float price = float(toPrice2[0]);
        
        TableRow newRow = retailerOffers.addRow();
        newRow.setString("retailer", sender);
        newRow.setString("simTime", simTime);
        newRow.setInt("quantity", quantity);
        newRow.setFloat("price", price);
      }
    }
    
    // parse input for trader usage and remaining and store in table
    
    Table traderUsage = new Table();
    traderUsage.addColumn("simTime");
    traderUsage.addColumn("remaining");
    traderUsage.addColumn("usage");
    
    for (TableRow row : input.rows()) {
      
      String systemTime = row.getString(0);
      String simTime = row.getString(1);
      String sender = row.getString(2);
      String message = row.getString(3);
      
      if(sender.contains("Trader") && message.contains("Consumed")){
        String[] toConsumed1 = split(message, "Consumed ");
        String[] toConsumed2 = split(toConsumed1[1], ' ');
        int consumed = int(toConsumed2[0]);
        String[] toRemaining1 = split(message, "units. ");
        String[] toRemaining2 = split(toRemaining1[1], ' ');
        int remaining = int(toRemaining2[0]);
        
        TableRow newRow = traderUsage.addRow();
        newRow.setString("simTime", simTime);
        newRow.setInt("usage", consumed);
        newRow.setFloat("remaining", remaining);
      }
    
    }
    
    // parse input for trader purchases and store in table
    Table traderPurchases = new Table();
    traderPurchases.addColumn("simTime");
    traderPurchases.addColumn("quantity");
    traderPurchases.addColumn("price");
    traderPurchases.addColumn("retailer");
    
    for (TableRow row : input.rows()) {
      
      String systemTime = row.getString(0);
      String simTime = row.getString(1);
      String sender = row.getString(2);
      String message = row.getString(3);
      
      if(sender.contains("Trader") && message.contains("Purchased")){
        String[] values = split(message, "Purchased ");
        String[] toQuant = split(values[1], ' ');
        int quantity = int(toQuant[0]);
        values = split(message, '$');
        float price = float(values[1]);
        String[] toRetailer1 = split(message, "from ");
        String[] toRetailer2 = split(toRetailer1[1], ".");
        String retailer = toRetailer2[0];
        
        TableRow newRow = traderPurchases.addRow();
        newRow.setString("simTime", simTime);
        newRow.setInt("quantity", quantity);
        newRow.setFloat("price", price);
        newRow.setString("retailer", retailer);
      }
    
    }
    
    
    // Print out data from tables to check it's been stored correctly
    for (int i = 0; i < typeList.length; i++){
      for (TableRow row : applianceData[i].rows()){
        int usage = row.getInt("powerUsage");
        String simTime = row.getString("simTime");
        println(typeList[i] + " used " + usage + "  units at " + simTime);
      }
    }
    
    for (TableRow row : traderPurchases.rows()){
      String simTime = row.getString("simTime");
      int quantity = row.getInt("quantity");
      float price = row.getFloat("price");
      String retailer = row.getString("retailer");
      println("At " + simTime + " the trader purchased " + quantity + " units for $" + price + ", from " + retailer + " at $" + price/quantity + " per unit");
    }
    
    for (TableRow row : retailerOffers.rows()){
      String simTime = row.getString("simTime");
      int quantity = row.getInt("quantity");
      float price = row.getFloat("price");
      String retailer = row.getString("retailer");
      println("At " + simTime + " " + retailer + " offered " + quantity + " units for $" + price + ",  at $" + price/quantity + " per unit");
    }
    
    for (TableRow row : traderUsage.rows()){
      String simTime = row.getString("simTime");
      int remaining = row.getInt("remaining");
      int usage = row.getInt("usage");
      println("At " + simTime + " the Trader used " + usage + " units and had " + remaining + " units remaining");
    }
  }
  
  void draw(){
    background(0);
    if (mousePressed){
      applianceDataButton.checkButton();
      retailerOfferButton.checkButton();
      traderPurchaseButton.checkButton();
      traderUsageButton.checkButton();
    }
    
    applianceDataSet.display();
    retailerOfferSet.display();
    traderPurchaseSet.display();
    traderUsageSet.display();
   
    applianceDataButton.display();
    retailerOfferButton.display();
    traderPurchaseButton.display();
    traderUsageButton.display();
  }