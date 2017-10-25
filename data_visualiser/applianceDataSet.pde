class ApplianceDataSet {
  int size;
  
  ApplianceDataSet(){
    
  }
  
  void display(){
    if (applianceDataButton.overButton){
    fill(200);
    ellipseMode(CENTER);
    ellipse(displayWidth/2, displayHeight/2, 400, 400);
    fill(0);
    textAlign(CENTER, CENTER);
    text("APPLIANCE DATA GOES HERE", displayWidth/2, displayHeight/2);
    }
  }
  
}