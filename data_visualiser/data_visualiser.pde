  /*
  Data visualiser running on Processing
  available from https://processing.org/download/
  
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
  
  public ApplianceDataSet applianceDataSet;
  public RetailerOfferSet retailerOfferSet;
  public TraderPurchaseSet traderPurchaseSet;
  public TraderUsageSet traderUsageSet;
  
  private ButtonSet buttonSet;
  
  void settings(){
    // initialise parameters for display
    size(1350, 800);
  }
  void setup(){
    background(0);
    
    typeList = new String[] {"Toaster", "AirCon", "Fridge", "Microwave", "CoffeeMachine", "Lamp", "PhoneCharger", "WashingMachine", "Dryer", "TV", "SolarPanel"};
    
    
    applianceDataSet = new ApplianceDataSet();
    retailerOfferSet = new RetailerOfferSet();
    traderPurchaseSet = new TraderPurchaseSet();
    traderUsageSet = new TraderUsageSet();
    
    buttonSet = new ButtonSet();
    
    input = loadTable("output.csv", "csv");
    println(input.getRowCount() + " total rows in table"); 
    
    // create array of tables for appliance data and parse input to give Table for each appliance usage and time 
    Table[] applianceData = new Table[typeList.length]; 
    
    for(int i = 0; i < typeList.length; i++){
      applianceData[i] = new Table();
      applianceData[i].addColumn("simDay");
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
          String[] day1 = split(simTime, "ay ");
          String[] day2 = split(day1[1], " ");
          TableRow newRow = applianceData[i].addRow();
          newRow.setInt("simDay", int(day2[0]));
          newRow.setString("simTime", day2[1]);
          newRow.setInt("powerUsage", int(values[1]));
        }
      }
    }
    
    // parse input for retailer offers and store in table
    Table retailerOffers = new Table();
    retailerOffers.addColumn("retailer");
    retailerOffers.addColumn("simDay");
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
        String[] day1 = split(simTime, "ay ");
        String[] day2 = split(day1[1], " ");
        
        TableRow newRow = retailerOffers.addRow();
        newRow.setString("retailer", sender);
        newRow.setInt("simDay", int(day2[0])); 
        newRow.setString("simTime", day2[1]);
        newRow.setInt("quantity", quantity);
        newRow.setFloat("price", price);
      }
    }
    
    // parse input for trader usage and remaining and store in table
    
    Table traderUsage = new Table();
    traderUsage.addColumn("simDay");
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
        String[] day1 = split(simTime, "ay ");
        String[] day2 = split(day1[1], " ");
        
        TableRow newRow = traderUsage.addRow();
        newRow.setInt("simDay", int(day2[0]));
        newRow.setString("simTime", day2[1]);
        newRow.setInt("usage", consumed);
        newRow.setFloat("remaining", remaining);
      }
    
    }
    
    // parse input for trader purchases and store in table
    Table traderPurchases = new Table();
    traderPurchases.addColumn("simDay");
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
        String[] day1 = split(simTime, "ay ");
        String[] day2 = split(day1[1], " ");
        
        TableRow newRow = traderPurchases.addRow();
        newRow.setInt("simDay", int(day2[0]));
        newRow.setString("simTime", day2[1]);
        newRow.setInt("quantity", quantity);
        newRow.setFloat("price", price);
        newRow.setString("retailer", retailer);
      }
    
    }
    
    // pass data tables to objects
    applianceDataSet.loadData(applianceData);
    traderUsageSet.loadData(traderUsage);
    retailerOfferSet.loadData(retailerOffers);
    traderPurchaseSet.loadData(traderPurchases);
    
  }
  
  void draw(){
    background(0);
    if (mousePressed){
      buttonSet.checkButtons();
    }
    
    buttonSet.display();
    applianceDataSet.display();
    retailerOfferSet.display();
    traderPurchaseSet.display();
    traderUsageSet.display();
  }