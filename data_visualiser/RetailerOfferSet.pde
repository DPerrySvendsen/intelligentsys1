class RetailerOfferSet {
  int size;
  Table dataSet;
  boolean isDisplayed;
  Graph retOffGraph;
  Graph retQuantGraph;
  float retGraphPos = displayWidth*0.1;
  float quantGraphPos = displayWidth*0.55;
  color[] col = {color(255, 50, 0), color(50, 255, 0), color(0, 50, 255)};
  
  RetailerOfferSet(){
    retOffGraph = new Graph();
    retQuantGraph = new Graph();
    retOffGraph.setTitle("Retailer Offer Total Price");
    retQuantGraph.setTitle("Retailer Offer Quantity");
  }
  
  void display(){
    if (isDisplayed) {
      retOffGraph.display(retGraphPos);
      retQuantGraph.display(quantGraphPos);
      textSize(30);
      fill(col[0]);
      text("Retailer 1", retGraphPos + displayWidth * 0.1, displayHeight * 0.78);
      fill(col[1]);
      text("Retailer 2", retGraphPos + displayWidth * 0.2, displayHeight * 0.78);
      fill(col[2]);
      text("Retailer 3", retGraphPos + displayWidth * 0.3, displayHeight * 0.78);
      fill(col[0]);
      text("Retailer 1", quantGraphPos + displayWidth * 0.1, displayHeight * 0.78);
      fill(col[1]);
      text("Retailer 2", quantGraphPos + displayWidth * 0.2, displayHeight * 0.78);
      fill(col[2]);
      text("Retailer 3", quantGraphPos + displayWidth * 0.3, displayHeight * 0.78);
    }
  }
  
  void loadData(Table input){
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
        float convTime = float(time[0]) + float(time[1]) / 60 + carry;
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
  
  void setDisplayed(boolean input) {
    isDisplayed = input;
  }
}