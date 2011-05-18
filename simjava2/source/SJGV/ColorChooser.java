/* Import statements */

import java.awt.Color;

public class ColorChooser {
  public static Color getColor(int count) {
    Color next = null;
    count = count % 30;
    switch (count) {
      case 0: next = Color.red; break;
      case 1: next = Color.blue; break;
      case 2: next = Color.green; break;
      case 3: next = Color.pink; break;
      case 4: next = Color.darkGray; break;
      case 5: next = Color.yellow; break;
      case 6: next = Color.magenta; break;
      case 7: next = new Color(61, 245, 255); break;
      case 8: next = new Color(160, 152, 101); break;
      case 9: next = new Color(144, 122, 204); break;
      case 10: next = new Color(168, 255, 198); break;
      case 11: next = new Color(201, 208, 255); break;
      case 12: next = new Color(251, 201, 255); break;
      case 13: next = new Color(45, 93, 135); break;
      case 14: next = new Color(122, 43, 191); break;
      case 15: next = new Color(211, 160, 174); break;
      case 16: next = new Color(67, 135, 75); break;
      case 17: next = new Color(147, 214, 59); break;
      case 18: next = new Color(140, 102, 44); break;
      case 19: next = new Color(160, 92, 40); break;
      case 20: next = new Color(40, 160, 154); break;
      case 21: next = new Color(229, 163, 64); break;
      case 22: next = new Color(229, 22, 195); break;
      case 23: next = new Color(255, 140, 0); break;
      case 24: next = new Color(196, 255, 238); break;
      case 25: next = new Color(112, 0, 100); break;
      case 26: next = new Color(46, 36, 183); break;
      case 27: next = new Color(255, 244, 168); break;
      case 28: next = new Color(255, 191, 0); break;
      case 29: next = new Color(123, 163, 242); break;
    }
    return next;
  }
}