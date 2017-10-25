class TraderUsageSet {
  int size;
  
  TraderUsageSet(){
    
  }
  
  void display(){
    if (traderUsageButton.overButton){
    fill(200);
    ellipseMode(CENTER);
    ellipse(displayWidth/2, displayHeight/2, 400, 400);
    fill(0);
    textAlign(CENTER, CENTER);
    text("TRADER USAGE DATA GOES HERE", displayWidth/2, displayHeight/2);
    }
  }
  
}