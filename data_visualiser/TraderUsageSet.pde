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
  }
  
  void display(){
    if (isDisplayed){
      tradConsGraph.display(width*0.1);
      tradRemGraph.display(width*0.55);
    }
  }
  
  void loadData(Table input){
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
        float convTime = float(time[0]) + float(time[1]) / 60 + carry;
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
  
  void setDisplayed(boolean input) {
    isDisplayed = input;
  }
  
}