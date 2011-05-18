/* Import statements */

import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Container;
import java.awt.TextArea;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

class AnnotationDialog extends JDialog implements ActionListener {
  private Graph owner;
  private boolean add; // Specifies if this dialog is used to add or view an annotation
  private String text;
  private TextArea textArea;
  private JButton button1,
                  button2,
                  button3;

  // Constructor for adding annotations
  AnnotationDialog(SJGV root, Graph owner) {
    super(root, "Add annotation", true);
    this.owner = owner;
    add = true;
    initGUI();
  }

  // Constructor for viewing annotations
  AnnotationDialog(SJGV root, Graph owner, String text) {
    super(root, "Annotation", true);
    this.owner = owner;
    this.text = text;
    add = false;
    initGUI();
  }

  private void initGUI() {
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        setVisible(false);
      }
    });

    Container rootPanel = this.getContentPane();
    GridBagLayout gb = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    rootPanel.setLayout(gb);

    textArea = new TextArea(5, 20);
    textArea.setFont(new Font("Courier", Font.PLAIN, 12));
    c.fill = GridBagConstraints.BOTH;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = new Insets(5, 5, 0, 5);
    c.weightx = 1;
    c.weighty = 1;
    gb.setConstraints(textArea, c);
    rootPanel.add(textArea);

    JPanel buttonPanel = new JPanel();
    if (add) {
      button1 = new JButton("Ok");
      button1.addActionListener(this);
      button1.setActionCommand("Ok");
      button2 = new JButton("Cancel");
      button2.addActionListener(this);
      button2.setActionCommand("Cancel");
      buttonPanel.add(button1);
      buttonPanel.add(button2);
    } else {
      textArea.setText(text);
      button1 = new JButton("Update");
      button1.addActionListener(this);
      button1.setActionCommand("Update");
      button2 = new JButton("Delete");
      button2.addActionListener(this);
      button2.setActionCommand("Delete");
      button3 = new JButton("Close");
      button3.addActionListener(this);
      button3.setActionCommand("Close");
      buttonPanel.add(button1);
      buttonPanel.add(button2);
      buttonPanel.add(button3);
    }
    c.weightx = 0;
    c.weighty = 0;
    c.fill = GridBagConstraints.NONE;
    c.insets = new Insets(0, 5, 5, 5);
    gb.setConstraints(buttonPanel, c);
    rootPanel.add(buttonPanel);

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    this.setBounds((int)screenSize.getWidth()/2 - 150, (int)screenSize.getHeight()/2 - 100, 300, 200);

//    this.setSize(300, 200);
    this.setVisible(true);
  }

  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    if (command.equals("Ok")) {
      owner.addAnnotation(textArea.getText());
      setVisible(false);
    } else if (command.equals("Cancel")) {
      setVisible(false);
    } else if (command.equals("Update")) {
      owner.setAnnotation(textArea.getText());
      setVisible(false);
    } else if (command.equals("Delete")) {
      owner.removeAnnotation();
      setVisible(false);
    } else if (command.equals("Close")) {
      setVisible(false);
    }
  }

}
