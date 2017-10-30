class TraderPurchaseSet {
  int size;
  Table dataSet;
  boolean isDisplayed;
  Graph tradPurGraph;
  Graph tradQuantGraph;
  color[] col = {color(255, 50, 0), color(50, 255, 0), color(0, 50, 255)};
  float purGraphPos =  width* 0.11;
  float quantGraphPos = width* 0.55;
  
  TraderPurchaseSet(){
    tradPurGraph = new Graph();
    tradQuantGraph = new Graph();
    tradPurGraph.setTitle("Trader Purchase Total Price");
    tradQuantGraph.setTitle("Trader Purchase Quantity");
    tradPurGraph.setYTitle("total price");
    tradQuantGraph.setYTitle("units purchased");
  }
  
  void display(){
    if (isDisplayed) {
      tradPurGraph.display(purGraphPos);
      tradQuantGraph.display(quantGraphPos);
      textSize(14);
      fill(col[0]);
      text("Retailer 1", purGraphPos + width * 0.1, height * 0.78);
      fill(col[1]);
      text("Retailer 2", purGraphPos + width * 0.2, height * 0.78);
      fill(col[2]);
      text("Retailer 3", purGraphPos + width * 0.3, height * 0.78);
      fill(col[0]);
      text("Retailer 1", quantGraphPos + width * 0.1, height * 0.78);
      fill(col[1]);
      text("Retailer 2", quantGraphPos + width * 0.2, height * 0.78);
      fill(col[2]);
      text("Retailer 3", quantGraphPos + width * 0.3, height * 0.78);
    }
  }
  
  void loadData(Table input){
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
        float convTime = float(time[0]) + float(time[1]) / 60 + carry;
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
  
  void setDisplayed(boolean input) {
    isDisplayed = input;
  }
}