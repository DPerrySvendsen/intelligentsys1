class Button {
  int size = displayWidth/50;
  float x, y;
  String label;
  public boolean overButton;
  
  Button(float xPos, float yPos, String labelInput){
    x = xPos;
    y = yPos;
    label = labelInput;
  }

  void checkButton()  {
    if (mouseX >= x - 2 * size && mouseX <= x + 2 * size && mouseY >= y - 2 * size && mouseY <= y + 2 * size) {
      overButton = true;
      
    } else {
      overButton = false;
      
    }
  }


  void display() {
    stroke(100);
    strokeWeight(4);
    if(overButton) {
      fill(255);
    }
    else {
      fill(0);
    }
    textAlign(CENTER, CENTER);
    textSize(24);
    
    beginShape();
    vertex(x - 2 * size, y - size);
    vertex(x + 2 * size, y - size);
    vertex(x + 2 * size, y + size);
    vertex(x - 2 * size, y + size);
    endShape(CLOSE);
    fill(200);
    text(label, x, y);
  }
  
}