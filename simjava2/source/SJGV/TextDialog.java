/* Import statements */

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Canvas;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.io.IOException;
import java.net.URL;

class TextDialog extends JDialog implements HyperlinkListener {

public final static int HELP_DIALOG = 0;
public final static int ABOUT_DIALOG = 1;
private JEditorPane helpPane;
private SJGV owner;

  TextDialog(SJGV owner, int type) {
    super(owner, true);
    this.owner = owner;
    if (type == HELP_DIALOG) {
      initHelp();
    } else {
      initAbout();
    }
  }

  private void initAbout() {
    this.setTitle("About");
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        setVisible(false);
      }
    });
    TextCanvas canvas = new TextCanvas();
    canvas.setSize(300, 180);

    JButton button = new JButton("Ok");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(button);
    this.getContentPane().setLayout(new BorderLayout());
    this.getContentPane().add("Center", canvas);
    this.getContentPane().add("South", buttonPanel);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    this.setBounds((int)screenSize.getWidth()/2 - 175, (int)screenSize.getHeight()/2 - 100, 350, 200);
    this.setSize(350, 200);
    this.setResizable(false);
    this.setVisible(true);
  }

  private void initHelp() {
    this.setTitle("Help");
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        setVisible(false);
      }
    });
    JButton button = new JButton("Ok");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });

    helpPane = new JEditorPane();
    helpPane.setEditable(false);
    try {
      helpPane.setPage(ClassLoader.getSystemResource("help.html"));
    } catch (IOException e) {
      System.out.println("The help file could not be found.");
    }
    helpPane.addHyperlinkListener(this);

    helpPane.setEditable(false);
    JScrollPane helpScroll = new JScrollPane(helpPane);
    helpScroll.setPreferredSize(new Dimension(400, 400));

    JPanel buttonPanel = new JPanel();
    buttonPanel.add(button);
    this.getContentPane().setLayout(new BorderLayout());
    this.getContentPane().add("Center", helpScroll);
    this.getContentPane().add("South", buttonPanel);

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    this.setBounds((int)screenSize.getWidth()/2 - 250, (int)screenSize.getHeight()/2 - 250, 500, 500);
    this.setVisible(true);
  }

  public void hyperlinkUpdate(HyperlinkEvent e) {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      try {
        helpPane.setPage((e.getURL()));
      } catch (IOException ioe) {}
    }
  }

  class TextCanvas extends Canvas {
    public void paint(Graphics g) {
      g.setColor(Color.black);
      g.setFont(new Font("TimesNewRoman", Font.BOLD, 26));
      g.drawString("The SimJava Graph Viewer", 23, 40);
      g.setFont(new Font("TimesNewRoman", Font.ITALIC, 18));
      g.drawString("by Costas Simatos", 105, 100);
      g.setFont(new Font("TimesNewRoman", Font.PLAIN, 18));
      g.drawString("The University of Edinburgh, 2002", 55, 125);
    }
  }
}
