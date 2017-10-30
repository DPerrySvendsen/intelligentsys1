import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class data_visualiser extends PApplet {

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
  
  public void settings(){
    // initialise parameters for display
    size(1350, 768);
  }
  public void setup(){
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
          newRow.setInt("simDay", PApplet.parseInt(day2[0]));
          newRow.setString("simTime", day2[1]);
          newRow.setInt("powerUsage", PApplet.parseInt(values[1]));
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
        int quantity = PApplet.parseInt(toQuant2[0]);
        String[] toPrice1 = split(message, '$');
        String[] toPrice2 = split(toPrice1[1], ' ');
        float price = PApplet.parseFloat(toPrice2[0]);
        String[] day1 = split(simTime, "ay ");
        String[] day2 = split(day1[1], " ");
        
        TableRow newRow = retailerOffers.addRow();
        newRow.setString("retailer", sender);
        newRow.setInt("simDay", PApplet.parseInt(day2[0])); 
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
        int consumed = PApplet.parseInt(toConsumed2[0]);
        String[] toRemaining1 = split(message, "units. ");
        String[] toRemaining2 = split(toRemaining1[1], ' ');
        int remaining = PApplet.parseInt(toRemaining2[0]);
        String[] day1 = split(simTime, "ay ");
        String[] day2 = split(day1[1], " ");
        
        TableRow newRow = traderUsage.addRow();
        newRow.setInt("simDay", PApplet.parseInt(day2[0]));
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
        int quantity = PApplet.parseInt(toQuant[0]);
        values = split(message, '$');
        float price = PApplet.parseFloat(values[1]);
        String[] toRetailer1 = split(message, "from ");
        String[] toRetailer2 = split(toRetailer1[1], ".");
        String retailer = toRetailer2[0];
        String[] day1 = split(simTime, "ay ");
        String[] day2 = split(day1[1], " ");
        
        TableRow newRow = traderPurchases.addRow();
        newRow.setInt("simDay", PApplet.parseInt(day2[0]));
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
  
  public void draw(){
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
class Button {
  float size = width/50;
  float x, y;
  String label;
  private boolean overButton;
  private boolean isDisplayed;
  private boolean lastPressed;
  
  Button(float xPos, float yPos, String labelInput){
    x = xPos;
    y = yPos;
    label = labelInput;
  }

  public boolean checkButton()  {
    if (mouseX >= x - 2 * size && mouseX <= x + 2 * size && mouseY >= y - 2 * size && mouseY <= y + 2 * size) {
      overButton = true;
      lastPressed = true;
      return true;
    } else {
      overButton = false;
      lastPressed = false;
      return false;
    }
  }


  public void display(boolean input) {
    if (input == true){;
      stroke(100);
      strokeWeight(4);
      if(overButton) {
        fill(180);
      }
      else {
        fill(0);
      }
      textAlign(CENTER, CENTER);
      textSize(10);
      
      beginShape();
      vertex(x - 2 * size, y - size);
      vertex(x + 2 * size, y - size);
      vertex(x + 2 * size, y + size);
      vertex(x - 2 * size, y + size);
      endShape(CLOSE);
      fill(255);
      text(label, x, y);
    }
  }
  
  public boolean isDisplayed(){
    return isDisplayed;
  }
  
  public boolean overButton(){
    return overButton;
  }
  
  public void setLastPressed(boolean input){
    lastPressed = input;
  }
  
  public boolean lastPressed(){
    return lastPressed;
  }
}
class ButtonSet {
  
  Button applianceDataButton, traderPurchaseButton, retailerOfferButton, traderUsageButton;
  Button[] buttonMatrix;  
  
  ButtonSet(){
    applianceDataButton = new Button(width * 0.2f, height * 0.9f, "APPLIANCE DATA");
    traderPurchaseButton = new Button(width * 0.6f, height * 0.9f, "TRADER PURCHASES");
    retailerOfferButton = new Button(width * 0.8f, height * 0.9f, "RETAILER OFFERS");
    traderUsageButton = new Button(width * 0.4f, height * 0.9f, "TRADER USAGE");
    buttonMatrix = new Button[typeList.length];
    for (int i = 0; i < typeList.length; i++){
      buttonMatrix[i] = new Button((width / 12) * (i + 1) , height * 0.8f, typeList[i]);
    }
  }
  
  public void checkButtons(){
    applianceDataSet.setDisplayed(applianceDataButton.checkButton());
    retailerOfferSet.setDisplayed(retailerOfferButton.checkButton());
    traderPurchaseSet.setDisplayed(traderPurchaseButton.checkButton());
    traderUsageSet.setDisplayed(traderUsageButton.checkButton());
    for (int i = 0; i < typeList.length; i++){
      if (buttonMatrix[i].checkButton()) {
        applianceDataSet.setAppToDisplay(i);
        applianceDataSet.setDisplayed(true);
      }
    }
  }
  
  public void display(){
    applianceDataButton.display(true);
    retailerOfferButton.display(true);
    traderPurchaseButton.display(true);
    traderUsageButton.display(true);
    if ((retailerOfferButton.lastPressed() || traderPurchaseButton.lastPressed() || traderUsageButton.lastPressed()) == false) {
      for (int i = 0; i < typeList.length; i++){
        buttonMatrix[i].display(true);
      }
    } else {
      for (int i = 0; i < typeList.length; i++){
        buttonMatrix[i].display(false);
      }
    }
  }
}
class Graph {
  float xpos;
  float ypos = height*0.05f;
  float size = width*.33f;
  Table data;
  int[] col = {color(255, 50, 0), color(50, 255, 0), color(0, 50, 255)};
  float xScale;
  float yScale;
  String title;
  String yTitle;
  
  Graph() {
    data = new Table();
    data.addColumn("xval");
    data.addColumn("yval");
    data.addColumn("id");
    xScale = width/3/24;
    yScale = width/6/300;
    title = "X title";
    yTitle = "Y title";
  }
  
  public void display(float xPos) {
    xpos = xPos;
    strokeWeight(4);
    for (TableRow row : data.rows()) {
      float id = row.getFloat("id"); 
      stroke(col[PApplet.parseInt(id)]);
      line(row.getFloat("xval") * xScale + xpos + id * 10, ypos + size - (row.getFloat("yval") * yScale), row.getFloat("xval") * xScale + xpos + id * 10, ypos + size);
    }
    noFill();
    stroke(255);
    strokeWeight(4);
    beginShape();
    vertex(xpos, ypos + size);
    vertex(xpos, ypos);
    vertex(xpos + size, ypos);
    vertex(xpos + size, ypos + size);
    endShape(CLOSE);
    textAlign(CENTER, CENTER);
    textSize(20);
    text(title, xpos + (size * 0.5f), ypos - (size * 0.05f));
    textAlign(RIGHT, CENTER);
    textSize(11);
    text(yTitle, xpos - 0.05f * size, ypos);
    textAlign(CENTER, CENTER);
    textSize(20);
    text("Time", xpos + (size * 0.5f), ypos + size * 1.1f);
    textSize(10);
    text("12.00AM", xpos, ypos + size * 1.05f);
    text("6.00AM", xpos + size * 0.25f, ypos + size * 1.05f);
    text("12.00PM", xpos + size * 0.5f, ypos + size * 1.05f);
    text("6.00PM", xpos + size * 0.75f, ypos + size * 1.05f);
    text("12.00AM", xpos + size, ypos + size * 1.05f);
    text(0.95f * size / yScale, xpos - 0.05f*size, ypos + size * 0.05f);
    text(0.475f * size / yScale, xpos - 0.05f*size, ypos + size * 0.525f);
  }
  
  public void addPoint(float xval, float yval, float id) {
    TableRow newRow = data.addRow();
    newRow.setFloat("xval", xval);
    newRow.setFloat("yval", yval);
    newRow.setFloat("id", id);
  }
  
  public void setYScale(float input) {
    yScale = 0.95f*size/input;
  }
  
  public void setTitle(String input) {
    title = input;
  }
  
  public void setYTitle(String input) {
    yTitle = input;
  }
}
class RetailerOfferSet {
  int size;
  Table dataSet;
  boolean isDisplayed;
  Graph retOffGraph;
  Graph retQuantGraph;
  float retGraphPos = width*0.11f;
  float quantGraphPos = width*0.55f;
  int[] col = {color(255, 50, 0), color(50, 255, 0), color(0, 50, 255)};
  
  RetailerOfferSet(){
    retOffGraph = new Graph();
    retQuantGraph = new Graph();
    retOffGraph.setTitle("Retailer Offer Total Price");
    retQuantGraph.setTitle("Retailer Offer Quantity");
    retOffGraph.setYTitle("Total Price");
    retQuantGraph.setYTitle("Units offered");
  }
  
  public void display(){
    if (isDisplayed) {
      retOffGraph.display(retGraphPos);
      retQuantGraph.display(quantGraphPos);
      textSize(14);
      fill(col[0]);
      text("Retailer 1", retGraphPos + width * 0.1f, height * 0.78f);
      fill(col[1]);
      text("Retailer 2", retGraphPos + width * 0.2f, height * 0.78f);
      fill(col[2]);
      text("Retailer 3", retGraphPos + width * 0.3f, height * 0.78f);
      fill(col[0]);
      text("Retailer 1", quantGraphPos + width * 0.1f, height * 0.78f);
      fill(col[1]);
      text("Retailer 2", quantGraphPos + width * 0.2f, height * 0.78f);
      fill(col[2]);
      text("Retailer 3", quantGraphPos + width * 0.3f, height * 0.78f);
    }
  }
  
  public void loadData(Table input){
    dataSet = input;
    float largestQuant = 0;
    float largestPrice = 0;
    
    for (TableRow row : dataSet.rows()){
      if (row.getInt("simDay") == 1){
        float id = 0;
        String retailer = row.getString("retailer");
        if (retailer.contains("2")) {
          id = 1;
        } else if (retailer.contains("3")) {
          id = 2;
        }
        float price = row.getFloat("price");
        float quant = row.getInt("quantity");
        String simTime = row.getString("simTime");
        String[] time = splitTokens(simTime, ": A P");
        // carry if time is PM
        float carry = 0;
        if (simTime.contains("P")) {
          carry = 12;
        }
        float convTime = PApplet.parseFloat(time[0]) + PApplet.parseFloat(time[1]) / 60 + carry;
        retOffGraph.addPoint(convTime, price, id);
        retQuantGraph.addPoint(convTime, quant, id);
        if (price > largestPrice) {
          largestPrice = price;
        }
        if (quant > largestQuant) {
          largestQuant = quant;
        }
      }
    }
    retOffGraph.setYScale(largestPrice);
    retQuantGraph.setYScale(largestQuant);
  }
  
  public void setDisplayed(boolean input) {
    isDisplayed = input;
  }
}
class TraderPurchaseSet {
  int size;
  Table dataSet;
  boolean isDisplayed;
  Graph tradPurGraph;
  Graph tradQuantGraph;
  int[] col = {color(255, 50, 0), color(50, 255, 0), color(0, 50, 255)};
  float purGraphPos =  width* 0.11f;
  float quantGraphPos = width* 0.55f;
  
  TraderPurchaseSet(){
    tradPurGraph = new Graph();
    tradQuantGraph = new Graph();
    tradPurGraph.setTitle("Trader Purchase Total Price");
    tradQuantGraph.setTitle("Trader Purchase Quantity");
    tradPurGraph.setYTitle("total price");
    tradQuantGraph.setYTitle("units purchased");
  }
  
  public void display(){
    if (isDisplayed) {
      tradPurGraph.display(purGraphPos);
      tradQuantGraph.display(quantGraphPos);
      textSize(14);
      fill(col[0]);
      text("Retailer 1", purGraphPos + width * 0.1f, height * 0.78f);
      fill(col[1]);
      text("Retailer 2", purGraphPos + width * 0.2f, height * 0.78f);
      fill(col[2]);
      text("Retailer 3", purGraphPos + width * 0.3f, height * 0.78f);
      fill(col[0]);
      text("Retailer 1", quantGraphPos + width * 0.1f, height * 0.78f);
      fill(col[1]);
      text("Retailer 2", quantGraphPos + width * 0.2f, height * 0.78f);
      fill(col[2]);
      text("Retailer 3", quantGraphPos + width * 0.3f, height * 0.78f);
    }
  }
  
  public void loadData(Table input){
    dataSet = input;
    float largestPrice = 0;
    float largestQuant = 0;
    
    for (TableRow row : dataSet.rows()){
      if (row.getInt("simDay") == 1){
        float id = 0;
        String retailer = row.getString("retailer");
        if (retailer.contains("2")) {
          id = 1;
        } else if (retailer.contains("3")) {
          id = 2;
        }
        float price = row.getFloat("price");
        float quant = row.getInt("quantity");
        String simTime = row.getString("simTime");
        String[] time = splitTokens(simTime, ": A P");
        // carry if time is PM
        float carry = 0;
        if (simTime.contains("P")) {
          carry = 12;
        }
        float convTime = PApplet.parseFloat(time[0]) + PApplet.parseFloat(time[1]) / 60 + carry;
        tradPurGraph.addPoint(convTime, price, id);
        tradQuantGraph.addPoint(convTime, quant, id);
        if (price > largestPrice) {
          largestPrice = price;
        }
        if (quant > largestQuant) {
          largestQuant = quant;
        }
      }
    }
    tradPurGraph.setYScale(largestPrice);
    tradQuantGraph.setYScale(largestQuant);
  }
  
  public void setDisplayed(boolean input) {
    isDisplayed = input;
  }
}
class TraderUsageSet {
  int size;
  Table dataSet;
  boolean isDisplayed;
  Graph tradConsGraph;
  Graph tradRemGraph;
  
  TraderUsageSet(){
    tradConsGraph = new Graph();
    tradRemGraph = new Graph();
    tradConsGraph.setTitle("Trader Consumption");
    tradRemGraph.setTitle("Trader Units Remaining");
    tradConsGraph.setYTitle("units used");
    tradRemGraph.setYTitle("units remaining");
  }
  
  public void display(){
    if (isDisplayed){
      tradConsGraph.display(width*0.11f);
      tradRemGraph.display(width*0.55f);
    }
  }
  
  public void loadData(Table input){
    dataSet = input;
    float largestCons = 0;
    float largestRem = 0;
    
    for (TableRow row : dataSet.rows()){
      if (row.getInt("simDay") == 1){
        float id = 0;
        float cons = row.getFloat("usage");
        float rem = row.getInt("remaining");
        String simTime = row.getString("simTime");
        String[] time = splitTokens(simTime, ": A P");
        // carry if time is PM
        float carry = 0;
        if (simTime.contains("P")) {
          carry = 12;
        }
        float convTime = PApplet.parseFloat(time[0]) + PApplet.parseFloat(time[1]) / 60 + carry;
        tradConsGraph.addPoint(convTime, cons, id);
        tradRemGraph.addPoint(convTime, rem, id);
        if (cons > largestCons) {
          largestCons = cons;
        }
        if (rem > largestRem) {
          largestRem = rem;
        }
      }
    }
    tradConsGraph.setYScale(largestCons);
    tradRemGraph.setYScale(largestRem);
  }
  
  public void setDisplayed(boolean input) {
    isDisplayed = input;
  }
  
}
class ApplianceDataSet {
  int size;
  Table[] dataSet;
  boolean isDisplayed;
  int appToDisplay;
  Graph[] graphMatrix;
  
  ApplianceDataSet(){
    dataSet = new Table[typeList.length];
    graphMatrix = new Graph[typeList.length];
    for(int i = 0;  i < typeList.length; i++) {
      String title = typeList[i];
      graphMatrix[i] = new Graph();
      graphMatrix[i].setTitle(title + " power units used");
      if (typeList[i].contains("Solar")) {
        graphMatrix[i].setYTitle("units generated");
      }
      else {
        graphMatrix[i].setYTitle("units used");
      }
    }
  }
  
  public void display() {
    if (isDisplayed){
      graphMatrix[appToDisplay].display(width * 0.33f);
    }
  }
  
  public void loadData(Table[] input){
    dataSet = input;
    for(int i = 0; i < typeList.length; i++) {
      float largestCons = 0;
      Table thisTable = dataSet[i];
      for (TableRow row : thisTable.rows()){
        if (row.getInt("simDay") == 1){
          float id = 0;
          float cons = row.getFloat("powerUsage");
          if(cons < 0) {
            cons = - cons;
            id = 1;
          }
          String simTime = row.getString("simTime");
          String[] time = splitTokens(simTime, ": A P");
          // carry if time is PM
          float carry = 0;
          if (simTime.contains("P") && (PApplet.parseInt(time[0]) != 12)) {
            carry = 12;
          }
          if (simTime.contains("A") && (PApplet.parseInt(time[0]) == 12)) {
            carry = -12;
          }
          float convTime = PApplet.parseFloat(time[0]) + (PApplet.parseFloat(time[1]) / 60) + carry;
          graphMatrix[i].addPoint(convTime, cons, id);
          if (cons > largestCons) {
            largestCons = cons;
          }
        }
      }
      graphMatrix[i].setYScale(largestCons);
    }
    
  }
  
  public void setDisplayed(boolean input) {
    isDisplayed = input;
  }
  
  public void setAppToDisplay(int input) {
    appToDisplay = input;
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "data_visualiser" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
