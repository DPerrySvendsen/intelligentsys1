class RetailerOfferSet {
  int size;
  
  RetailerOfferSet(){
    
  }
  
  void display(){
    if (retailerOfferButton.overButton){
    fill(100);
    ellipseMode(CENTER);
    ellipse(displayWidth/2, displayHeight/2, 400, 400);
    fill(0);
    textAlign(CENTER, CENTER);
    text("RETAILER OFFER GOES HERE", displayWidth/2, displayHeight/2);
    }
  }
  
}