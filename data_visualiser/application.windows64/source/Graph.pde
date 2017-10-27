class Graph {
  float xpos;
  float ypos = displayHeight*0.05;
  float size = displayWidth*.35;
  Table data;
  color[] col = {color(255, 50, 0), color(50, 255, 0), color(0, 50, 255)};
  float xScale;
  float yScale;
  String title;
  
  Graph() {
    data = new Table();
    data.addColumn("xval");
    data.addColumn("yval");
    data.addColumn("id");
    xScale = displayWidth/3/24;
    yScale = displayWidth/6/300;
    title = "title";
  }
  
  void display(float xPos) {
    xpos = xPos;
    strokeWeight(10);
    for (TableRow row : data.rows()) {
      float id = row.getFloat("id"); 
      stroke(col[int(id)]);
      line(row.getFloat("xval") * xScale + xpos + id * 10, ypos + size - (row.getFloat("yval") * yScale), row.getFloat("xval") * xScale + xpos + id * 10, ypos + size);
    }
    noFill();
    stroke(255);
    strokeWeight(10);
    beginShape();
    vertex(xpos, ypos + size);
    vertex(xpos, ypos);
    vertex(xpos + size, ypos);
    vertex(xpos + size, ypos + size);
    endShape(CLOSE);
    textAlign(CENTER, CENTER);
    textSize(50);
    text(title, xpos + (size * 0.5), ypos - (size * 0.05));
    text("Time", xpos + (size * 0.5), ypos + size * 1.1);
    textSize(20);
    text("12.00AM", xpos, ypos + size * 1.05);
    text("6.00AM", xpos + size * 0.25, ypos + size * 1.05);
    text("12.00PM", xpos + size * 0.5, ypos + size * 1.05);
    text("6.00PM", xpos + size * 0.75, ypos + size * 1.05);
    text("12.00AM", xpos + size, ypos + size * 1.05);
    text(0.95 * size / yScale, xpos - 0.05*size, ypos + size * 0.05);
    text(0.475 * size / yScale, xpos - 0.05*size, ypos + size * 0.525);
  }
  
  void addPoint(float xval, float yval, float id) {
    TableRow newRow = data.addRow();
    newRow.setFloat("xval", xval);
    newRow.setFloat("yval", yval);
    newRow.setFloat("id", id);
  }
  
  void setYScale(float input) {
    yScale = 0.95*size/input;
  }
  
  void setTitle(String input) {
    title = input;
  }
}