/* Import statements */

import javax.swing.filechooser.FileFilter;
import java.io.File;

class GraphFilter extends FileFilter {
  private static final String suffix = "sjg";
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
    return "SimJava graphs (*.sjg)";
  }
}
