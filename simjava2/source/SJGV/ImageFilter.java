/* Import statements */

import javax.swing.filechooser.FileFilter;
import java.io.File;

class ImageFilter extends FileFilter {
  private static final String suffix = "gif";
  public boolean accept(File f) {
    if (f.isDirectory()) {
      return true;
    }
    String filename = (f.getName()).toLowerCase();
    if (filename.endsWith("." + suffix)) {
      return true;
    }
    return false;
  }
  // The description of this filter
  public String getDescription() {
    return "GIF images (*.gif)";
  }
}
