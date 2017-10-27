class Button {
  float size = width/50;
  float x, y;
  String label;
  private boolean overButton;
  private boolean isDisplayed;
  private boolean lastPressed;
  
  Button(float xPos, float yPos, String labelInput){
    x = xPos;
    y = yPos;
    label = labelInput;
  }

  boolean checkButton()  {
    if (mouseX >= x - 2 * size && mouseX <= x + 2 * size && mouseY >= y - 2 * size && mouseY <= y + 2 * size) {
      overButton = true;
      lastPressed = true;
      return true;
    } else {
      overButton = false;
      lastPressed = false;
      return false;
    }
  }


  void display(boolean input) {
    if (input == true){;
      stroke(100);
      strokeWeight(4);
      if(overButton) {
        fill(180);
      }
      else {
        fill(0);
      }
      textAlign(CENTER, CENTER);
      textSize(10);
      
      beginShape();
      vertex(x - 2 * size, y - size);
      vertex(x + 2 * size, y - size);
      vertex(x + 2 * size, y + size);
      vertex(x - 2 * size, y + size);
      endShape(CLOSE);
      fill(255);
      text(label, x, y);
    }
  }
  
  boolean isDisplayed(){
    return isDisplayed;
  }
  
  boolean overButton(){
    return overButton;
  }
  
  void setLastPressed(boolean input){
    lastPressed = input;
  }
  
  boolean lastPressed(){
    return lastPressed;
  }
}