class ButtonSet {
  
  Button applianceDataButton, traderPurchaseButton, retailerOfferButton, traderUsageButton;
  Button[] buttonMatrix;  
  
  ButtonSet(){
    applianceDataButton = new Button(width * 0.2, height * 0.9, "APPLIANCE DATA");
    traderPurchaseButton = new Button(width * 0.6, height * 0.9, "TRADER PURCHASES");
    retailerOfferButton = new Button(width * 0.8, height * 0.9, "RETAILER OFFERS");
    traderUsageButton = new Button(width * 0.4, height * 0.9, "TRADER USAGE");
    buttonMatrix = new Button[typeList.length];
    for (int i = 0; i < typeList.length; i++){
      buttonMatrix[i] = new Button((width / 12) * (i + 1) , height * 0.8, typeList[i]);
    }
  }
  
  void checkButtons(){
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
  
  void display(){
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