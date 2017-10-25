class TraderPurchaseSet {
  int size;
  
  TraderPurchaseSet(){
    
  }
  
  void display(){
    if (traderPurchaseButton.overButton){
    fill(230);
    ellipseMode(CENTER);
    ellipse(displayWidth/2, displayHeight/2, 400, 400);
    fill(0);
    textAlign(CENTER, CENTER);
    text("TRADER PURCHASE DATA GOES HERE", displayWidth/2, displayHeight/2);
    }
  }
  
}