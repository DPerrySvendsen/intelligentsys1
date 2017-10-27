class ButtonSet {
  
  Button applianceDataButton, traderPurchaseButton, retailerOfferButton, traderUsageButton;
  Button[] buttonMatrix;  
  
  ButtonSet(){
    applianceDataButton = new Button(displayWidth * 0.2, displayHeight * 0.9, "APPLIANCE DATA");
    traderPurchaseButton = new Button(displayWidth * 0.6, displayHeight * 0.9, "TRADER PURCHASES");
    retailerOfferButton = new Button(displayWidth * 0.8, displayHeight * 0.9, "RETAILER OFFERS");
    traderUsageButton = new Button(displayWidth * 0.4, displayHeight * 0.9, "TRADER USAGE");
    buttonMatrix = new Button[typeList.length];
    for (int i = 0; i < typeList.length; i++){
      buttonMatrix[i] = new Button((displayWidth / 12) * (i + 1) , displayHeight * 0.8, typeList[i]);
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