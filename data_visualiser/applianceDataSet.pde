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
      graphMatrix[i].setTitle(title);
    }
  }
  
  void display() {
    if (isDisplayed){
      graphMatrix[appToDisplay].display(width * 0.3);
    }
  }
  
  void loadData(Table[] input){
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
          if (simTime.contains("P") && (int(time[0]) != 12)) {
            carry = 12;
          }
          if (simTime.contains("A") && (int(time[0]) == 12)) {
            carry = -12;
          }
          float convTime = float(time[0]) + (float(time[1]) / 60) + carry;
          graphMatrix[i].addPoint(convTime, cons, id);
          if (cons > largestCons) {
            largestCons = cons;
          }
        }
      }
      graphMatrix[i].setYScale(largestCons);
    }
    
  }
  
  void setDisplayed(boolean input) {
    isDisplayed = input;
  }
  
  void setAppToDisplay(int input) {
    appToDisplay = input;
  }
}