/* Import statements */

import java.awt.Color;
import java.awt.Panel;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;

class ColorPanel extends Panel {
  private Image staticImage;
  private Graphics staticGraphics;
  private int width;
  private int height;
  private int count;

  ColorPanel(int count) {
    super();
    this.count = count;
    if (count > 5) {
      width = 400;
      height = (count/2)*20 + 45;
    } else {
      width = 200;
      height = count*20 + 45;
    }
    this.setSize(width, height);
  }

  private void draw(Graphics g) {
    g.setColor(new Color(192, 196, 192));
    g.fillRect(0, 0, width, height);
    g.setFont(new Font("Courier", Font.BOLD, 16));
    g.setColor(Color.black);
    g.drawString("Legend:", 20, 25);
    int currentY = 35;
    g.setFont(new Font("Courier", Font.PLAIN, 14));
    for (int i=0; i < count; i++) {
      g.drawString("Replication " + (i+1), 20, currentY+15);
      g.setColor(ColorChooser.getColor(i));
      g.fillOval(150, currentY+2, 15, 15);
      g.setColor(Color.black);
      g.drawOval(150, currentY+2, 15, 15);
      if ((count > 5) && ((i+1) < count)) {
        g.drawString("Replication " + (i+2), 210, currentY+15);
        g.setColor(ColorChooser.getColor(i+1));
        g.fillOval(340, currentY+2, 15, 15);
        g.setColor(Color.black);
        g.drawOval(340, currentY+2, 15, 15);
        i++;
      }
      currentY += 20;
    }
  }

  private void setupStatic() {
    staticImage = createImage(width, height);
    staticGraphics = staticImage.getGraphics();
  }

  public void paint(Graphics g) {
    this.setSize(width, height);
    if (staticImage == null) {
      setupStatic();
    }
    draw(staticGraphics);
    g.drawImage(staticImage,0,0,this);
  }

  public void update(Graphics g) {
    paint(g);
  }
}
