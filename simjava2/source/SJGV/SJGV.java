/* Import statements */

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPopupMenu;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.ScrollPane;
import java.awt.Toolkit;
import java.awt.Font;
import java.awt.TextArea;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;
import eduni.simjava.Sim_stat;

public class SJGV extends JFrame implements ActionListener, MouseListener {
  private static final String suffix = ".sjg";
  private static final int NONE = 0;
  private static final int EVENTS_COMPLETED = 1; // After a number of completed events at a specific entity
  private static final int TIME_ELAPSED = 2;     // After a perion of time has elapsed
  private static final int MIN_MAX = 3;
  private static final int IND_REPLICATIONS = 4;
  private static final int BATCH_MEANS = 5;
  private static final int INTERVAL_ACCURACY = 6; // After a confidence interval has reached a certain accuracy
  private static final int RATE_BASED = 0;
  private static final int STATE_BASED = 1;
  private static final int INTERVAL_BASED = 2;

  private String graphFile,
                 oldGraphFile,
                 directory;
  private Object[] runData;
  private int outputAnalysisType = NONE;
  private List entityMeasures;
  private List observations;
  private String currentEntity;
  private String currentMeasure;
  private int currentMeasureType;
  private Sim_stat currentStat;
  private int currentReplication;
  private double[] runTimes;
  private double sampleStart,
                 sampleEnd;
  private boolean hasClicked = false;
  private boolean selected = false;

  private Graph graph;

  /*-AWT COMPONENTS-*/
  private JComboBox entitiesCombo,
                    measuresCombo;
  private TextArea totalArea,
                   sampleArea;
  private JTextField zoomXField,
                     zoomYField,
                     repField;
  private JButton zoomButton,
                  viewRepButton;
  private JCheckBox showSteadyStateCheck,
                    showTransientCheck,
                    showMeanCheck,
                    showBatchesCheck,
                    showAllRepsCheck,
                    showAnnotationsCheck;
  private JLabel repLabel;
  private JPanel samplePanel;
  private JRadioButton infoRadio,
                       totalRadio;
  private JMenuBar menuBar;
  private JMenu fileMenu,
                selectMenu,
                viewMenu,
                helpMenu,
                entityMenu,
                measureMenu,
                replicationMenu;
  private JMenuItem openItem,
                    saveItem,
                    saveAsItem,
                    saveAsImageItem,
                    helpItem,
                    aboutItem,
                    exitItem;
  private JRadioButtonMenuItem runInfoItem,
                               totalMeasurementsItem;
  private JCheckBoxMenuItem showSteadyStateItem,
                            showTransientItem,
                            showTotalMeanItem,
                            showBatchesItem,
                            showAnnotationsItem,
                            showAllReplicationsItem;
  /*----------------*/


  public SJGV(String graphFile, String directory) {
    super();
    this.graphFile = graphFile;
    this.directory = directory;
    if (!loadData(true)) {
      System.exit(0);
    }
    setupData();
    initGUI();
  }

  private void setupData() {
    outputAnalysisType = ((int[])runData[1])[2];
    List stats;
    switch (outputAnalysisType) {
      case NONE:
        stats = (List)runData[4];
        break;
      case IND_REPLICATIONS:
        stats = (List)((Object[])((List)runData[7]).get(0))[1];
        currentReplication = 0;
        break;
      default:
        stats = (List)runData[7];
        break;
    }
    entityMeasures = new ArrayList();
    int stats_size = stats.size();
    for (int i=0; i < stats_size; i++) {
      Sim_stat stat = (Sim_stat)stats.get(i);
      if ((stat == null) || (stat.detailed_measure_count() == 0)) continue;
      entityMeasures.add(new Object[] {stat.get_name(), stat.get_detailed_measures()});
    }
  }

  private boolean loadData(boolean starting) {
    System.out.print("Loading graph data...");
    ObjectInputStream ois = null;
    try {
      ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(graphFile)));
      runData = (Object[])ois.readObject();
      ois.close();
      if (measuresExist()) {
        System.out.println("done.");
        return true;
      } else {
        if (starting) {
          System.out.println("failed.");
        } else {
          System.out.println("failed.");
          graphFile = oldGraphFile;
          loadData(false);
        }
        JOptionPane.showMessageDialog(this, "No detailed measures have been defined for this simulation.");
        return false;
      }
    } catch (FileNotFoundException fnfe) {
      JOptionPane.showMessageDialog(this, new File(graphFile).getName() + " not found.");
      graphFile = oldGraphFile;
      System.out.println("failed.");
      return false;
    } catch (IOException ioe) {
      JOptionPane.showMessageDialog(this, "An error occurred while opening " + graphFile + ".");
      graphFile = oldGraphFile;
      System.out.println("failed.");
      return false;
    } catch (ClassNotFoundException cnfe) {
      JOptionPane.showMessageDialog(this, new File(graphFile).getName() + " has an invalid format.");
      graphFile = oldGraphFile;
      System.out.println("failed.");
      return false;
    } finally {
      if (ois != null) { try { ois.close(); } catch (Exception e) {} }
    }
  }

  private boolean measuresExist	() {
    List stats;
    if (((int[])runData[1])[2] == NONE) {
      stats = (List)runData[4];
    } else if (((int[])runData[1])[2] == IND_REPLICATIONS) {
      stats = (List)((Object[])(((List)runData[7]).get(0)))[1];
    } else {
      stats = (List)runData[7];
    }
    int stats_size = stats.size();
    for (int i=0; i < stats_size; i++) {
      Sim_stat stat = (Sim_stat)stats.get(i);
      if ((stat != null) && (stat.detailed_measure_count() > 0)) {
        return true;
      }
    }
    return false;
  }

  private void initGUI() {
    File f = new File(graphFile);
    this.setTitle("SimJava Graph Viewer - " + f.getName());
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);

    String[] entities = getEntityNames();
    currentEntity = entities[0];
    currentMeasure = getMeasureNames(currentEntity)[0];
    currentMeasureType = getMeasureType(currentEntity, currentMeasure);
    currentStat = getStat(entities[0]);
    runTimes = getRunTimes();
    sampleStart = 0.0;
    sampleEnd = runTimes[0];
    String[] measureNames = getMeasureNames(entities[0]);

    GridBagLayout gb = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    JPanel selectionPanel = new JPanel();
    selectionPanel.setLayout(gb);

    c.gridwidth = GridBagConstraints.REMAINDER;
    if (outputAnalysisType != NONE) {
      infoRadio = new JRadioButton("Run information", true);
      totalRadio = new JRadioButton("Total measurements");
      infoRadio.setFocusPainted(false);
      totalRadio.setFocusPainted(false);
      infoRadio.addActionListener(this);
      totalRadio.addActionListener(this);
      ButtonGroup group = new ButtonGroup();
      group.add(infoRadio);
      group.add(totalRadio);
      c.insets = new Insets(5, 4, 0, 2);
      c.anchor = GridBagConstraints.WEST;
      gb.setConstraints(infoRadio, c);
      selectionPanel.add(infoRadio);
      c.insets = new Insets(0, 4, 0, 2);
      gb.setConstraints(totalRadio, c);
      selectionPanel.add(totalRadio);
      c.insets = new Insets(5, 10, 5, 10);
      c.anchor = GridBagConstraints.CENTER;
    } else {
      c.insets = new Insets(10, 10, 10, 10);
    }
    entitiesCombo = new JComboBox(entities);
    entitiesCombo.addActionListener(this);
    measuresCombo = new JComboBox(measureNames);
    measuresCombo.addActionListener(this);
    measuresCombo.setActionCommand("Ready");


    gb.setConstraints(entitiesCombo, c);
    selectionPanel.add(entitiesCombo);
    c.insets = new Insets(10, 10, 10, 10);
    gb.setConstraints(measuresCombo, c);
    selectionPanel.add(measuresCombo);

    gb = new GridBagLayout();
    c = new GridBagConstraints();
    JPanel totalPanel = new JPanel();
    totalPanel.setLayout(gb);
    totalArea = new TextArea(6, 60);
    totalArea.setEditable(false);
    totalArea.setFont(new Font("Courier", Font.PLAIN, 12));
    setInfoText();
    c.insets = new Insets(5, 5, 5, 5);
    c.weightx = 1;
    c.weighty = 1;
    c.fill = GridBagConstraints.BOTH;
    c.gridwidth = GridBagConstraints.REMAINDER;
    gb.setConstraints(totalArea, c);
    totalPanel.add(totalArea);

    gb = new GridBagLayout();
    c = new GridBagConstraints();
    JPanel graphPanel = new JPanel(true);
    graphPanel.setLayout(gb);
    if (outputAnalysisType == IND_REPLICATIONS) {
      graph = new Graph(this, 0, currentStat, currentMeasure, currentMeasureType, 0.0, runTimes[0], runTimes[1], getTotalMean(), (List)runData[7]);
    } else {
      graph = new Graph(this, currentStat, currentMeasure, currentMeasureType, 0.0, runTimes[0], runTimes[1], getTotalMean());
    }
    graph.addMouseListener(this);
    ScrollPane scroll = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
    scroll.add(graph);
    c.insets = new Insets(5, 5, 5, 5);
    c.weightx = 1.0;
    c.weighty = 1.0;
    c.fill = GridBagConstraints.BOTH;
    c.gridwidth = GridBagConstraints.REMAINDER;
    gb.setConstraints(scroll, c);
    graphPanel.add(scroll);

    gb = new GridBagLayout();
    c = new GridBagConstraints();
    JPanel optionsPanel = new JPanel();
    optionsPanel.setLayout(gb);

    JPanel zoomPanel = new JPanel();
    GridBagLayout gbz = new GridBagLayout();
    GridBagConstraints cz = new GridBagConstraints();
    zoomPanel.setLayout(gbz);
    zoomXField = new JTextField(3);
    zoomYField = new JTextField(3);
    zoomXField.setHorizontalAlignment(JTextField.RIGHT);
    zoomYField.setHorizontalAlignment(JTextField.RIGHT);
    zoomXField.setText("100");
    zoomYField.setText("100");
    zoomButton = new JButton("Zoom");
    zoomButton.setFocusPainted(false);
    zoomButton.addActionListener(this);
    JLabel xLabel1 = new JLabel("X:");
    JLabel yLabel1 = new JLabel("Y:");
    JLabel xLabel2 = new JLabel("%");
    JLabel yLabel2 = new JLabel("%");
    cz.weighty = 1.0;
    cz.gridwidth = 1;
    cz.gridheight = 2;
    cz.insets = new Insets(2, 2, 2, 2);
    gbz.setConstraints(zoomButton, cz);
    zoomPanel.add(zoomButton);
    cz.weighty = 0.0;
    cz.gridwidth = 1;
    cz.gridheight = 1;
    cz.insets = new Insets(4, 2, 2, 2);
    gbz.setConstraints(xLabel1, cz);
    zoomPanel.add(xLabel1);
    cz.insets = new Insets(2, 0, 0, 2);
    gbz.setConstraints(zoomXField, cz);
    zoomPanel.add(zoomXField);
    cz.gridwidth = GridBagConstraints.REMAINDER;
    cz.anchor = GridBagConstraints.WEST;
    cz.insets = new Insets(4, 0, 2, 2);
    gbz.setConstraints(xLabel2, cz);
    zoomPanel.add(xLabel2);
    cz.anchor = GridBagConstraints.CENTER;
    cz.gridwidth = 1;
    cz.insets = new Insets(0, 2, 2, 2);
    gbz.setConstraints(yLabel1, cz);
    zoomPanel.add(yLabel1);
    cz.insets = new Insets(0, 0, 2, 2);
    gbz.setConstraints(zoomYField, cz);
    zoomPanel.add(zoomYField);
    cz.gridwidth = GridBagConstraints.REMAINDER;
    cz.anchor = GridBagConstraints.WEST;
    gbz.setConstraints(yLabel2, cz);
    zoomPanel.add(yLabel2);
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.WEST;
    gb.setConstraints(zoomPanel, c);
    optionsPanel.add(zoomPanel);

    showSteadyStateCheck = new JCheckBox("Steady state only", false);
    showTransientCheck = new JCheckBox("Show transient", false);
    showMeanCheck = new JCheckBox("Show total mean", false);
    showAnnotationsCheck = new JCheckBox("Show annotations", false);
    showSteadyStateCheck.addActionListener(this);
    showTransientCheck.addActionListener(this);
    showMeanCheck.addActionListener(this);
    showAnnotationsCheck.addActionListener(this);
    showSteadyStateCheck.setFocusPainted(false);
    showTransientCheck.setFocusPainted(false);
    showMeanCheck.setFocusPainted(false);
    showAnnotationsCheck.setFocusPainted(false);

    c.insets = new Insets(0, 2, 0, 2);
    c.anchor = GridBagConstraints.WEST;
    c.gridwidth = GridBagConstraints.REMAINDER;
    gb.setConstraints(showSteadyStateCheck, c);
    optionsPanel.add(showSteadyStateCheck);
    gb.setConstraints(showTransientCheck, c);
    optionsPanel.add(showTransientCheck);
    gb.setConstraints(showMeanCheck, c);
    optionsPanel.add(showMeanCheck);
    if (outputAnalysisType == BATCH_MEANS) {
      showBatchesCheck = new JCheckBox("Show batches", false);
      showBatchesCheck.addActionListener(this);
      showBatchesCheck.setFocusPainted(false);
      gb.setConstraints(showBatchesCheck, c);
      optionsPanel.add(showBatchesCheck);
      Object[] bInfo = (Object[])runData[6];
      graph.setBatches(((Integer)bInfo[0]).intValue(), ((Double)bInfo[1]).doubleValue());
    } else if (outputAnalysisType == IND_REPLICATIONS) {
      JPanel repPanel = new JPanel();
      GridBagLayout gbr = new GridBagLayout();
      GridBagConstraints cr = new GridBagConstraints();
      repPanel.setLayout(gbr);

      repLabel = new JLabel("Rep:");
      cr.insets = new Insets(0, 2, 0, 2);
      cr.anchor = GridBagConstraints.WEST;
      cr.gridwidth = 1;
      gbr.setConstraints(repLabel, cr);
      repPanel.add(repLabel);
      repField = new JTextField(2);
      repField.setHorizontalAlignment(JTextField.RIGHT);
      repField.setText("1");
      gbr.setConstraints(repField, cr);
      repPanel.add(repField);
      viewRepButton = new JButton("Show");
      viewRepButton.addActionListener(this);
      viewRepButton.setFocusPainted(false);
      c.gridwidth = GridBagConstraints.REMAINDER;
      gbr.setConstraints(viewRepButton, cr);
      repPanel.add(viewRepButton);

      gb.setConstraints(repPanel, c);
      optionsPanel.add(repPanel);

      showAllRepsCheck = new JCheckBox("All replications", false);
      showAllRepsCheck.addActionListener(this);
      showAllRepsCheck.setFocusPainted(false);
      gb.setConstraints(showAllRepsCheck, c);
      optionsPanel.add(showAllRepsCheck);
    }
    gb.setConstraints(showAnnotationsCheck, c);
    optionsPanel.add(showAnnotationsCheck);

    gb = new GridBagLayout();
    c = new GridBagConstraints();
    samplePanel = new JPanel();
    samplePanel.setLayout(gb);
    sampleArea = new TextArea(6, 40);
    sampleArea.setEditable(false);
    sampleArea.setFont(new Font("Courier", Font.PLAIN, 12));
    setSampleText();
    c.insets = new Insets(5, 5, 5, 5);
    c.weightx = 1;
    c.weighty = 1;
    c.fill = GridBagConstraints.BOTH;
    c.gridwidth = GridBagConstraints.REMAINDER;
    gb.setConstraints(sampleArea, c);
    samplePanel.add(sampleArea);

    selectionPanel.setBorder(BorderFactory.createRaisedBevelBorder());
    totalPanel.setBorder(BorderFactory.createRaisedBevelBorder());
    graphPanel.setBorder(BorderFactory.createRaisedBevelBorder());
    optionsPanel.setBorder(BorderFactory.createRaisedBevelBorder());
    samplePanel.setBorder(BorderFactory.createRaisedBevelBorder());

    gb = new GridBagLayout();
    c = new GridBagConstraints();
    JPanel rootPanel = new JPanel();
    rootPanel.setLayout(gb);
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 0;
    c.weighty = 0;
    c.gridwidth = 1;
    gb.setConstraints(selectionPanel, c);
    rootPanel.add(selectionPanel);
    c.weightx = 1;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.BOTH;
    gb.setConstraints(totalPanel, c);
    rootPanel.add(totalPanel);
    c.weightx = 1;
    c.weighty = 1;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.BOTH;
    gb.setConstraints(graphPanel, c);
    rootPanel.add(graphPanel);
    c.weightx = 0;
    c.weighty = 0;
    c.gridwidth = 1;
    gb.setConstraints(optionsPanel, c);
    rootPanel.add(optionsPanel);
    c.weightx = 1;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.fill = GridBagConstraints.BOTH;
    gb.setConstraints(samplePanel, c);
    rootPanel.add(samplePanel);

    /* ADD THE FRAME'S MENU */

    menuBar = new JMenuBar();
    this.setJMenuBar(menuBar);
    fileMenu = new JMenu("File");
    menuBar.add(fileMenu);
    selectMenu = new JMenu("Select");
    menuBar.add(selectMenu);
    viewMenu = new JMenu("View");
    menuBar.add(viewMenu);
    helpMenu = new JMenu("Help");
    menuBar.add(helpMenu);
    // File menu
    openItem = new JMenuItem("Open...");
    openItem.addActionListener(this);
    fileMenu.add(openItem);
    fileMenu.addSeparator();
    saveItem = new JMenuItem("Save");
    saveItem.addActionListener(this);
    fileMenu.add(saveItem);
    saveAsItem = new JMenuItem("Save as...");
    saveAsItem.addActionListener(this);
    fileMenu.add(saveAsItem);
    fileMenu.addSeparator();
    saveAsImageItem = new JMenuItem("Save as image...");
    saveAsImageItem.addActionListener(this);
    fileMenu.add(saveAsImageItem);
    fileMenu.addSeparator();
    exitItem = new JMenuItem("Exit");
    exitItem.addActionListener(this);
    fileMenu.add(exitItem);
    // Select menu
    if (outputAnalysisType != NONE) {
      runInfoItem = new JRadioButtonMenuItem("Run information", true);
      runInfoItem.addActionListener(this);
      selectMenu.add(runInfoItem);
      totalMeasurementsItem = new JRadioButtonMenuItem("Total measurements", false);
      totalMeasurementsItem.addActionListener(this);
      selectMenu.add(totalMeasurementsItem);
      selectMenu.addSeparator();
      ButtonGroup group = new ButtonGroup();
      group.add(runInfoItem);
      group.add(totalMeasurementsItem);
    }
    entityMenu = new JMenu("Entity");
    for (int i=0; i < entities.length; i++) {
      JMenuItem entityItem = new JMenuItem(entities[i]);
      entityItem.addActionListener(this);
      entityItem.setActionCommand("Entity item"+i);
      entityMenu.add(entityItem);
    }
    selectMenu.add(entityMenu);
    measureMenu = new JMenu("Measure");
    for (int i=0; i < measureNames.length; i++) {
      JMenuItem measureItem = new JMenuItem(measureNames[i]);
      measureItem.addActionListener(this);
      measureItem.setActionCommand("Measure item"+i);
      measureMenu.add(measureItem);
    }
    selectMenu.add(measureMenu);
    // View menu
    showSteadyStateItem = new JCheckBoxMenuItem("Steady state only");
    showSteadyStateItem.addActionListener(this);
    viewMenu.add(showSteadyStateItem);
    showTransientItem = new JCheckBoxMenuItem("Show transient");
    showTransientItem.addActionListener(this);
    viewMenu.add(showTransientItem);
    showTotalMeanItem = new JCheckBoxMenuItem("Show total mean");
    showTotalMeanItem.addActionListener(this);
    viewMenu.add(showTotalMeanItem);
    viewMenu.addSeparator();
    if (outputAnalysisType == BATCH_MEANS) {
      showBatchesItem = new JCheckBoxMenuItem("Show batches");
      showBatchesItem.addActionListener(this);
      viewMenu.add(showBatchesItem);
      viewMenu.addSeparator();
    } else if (outputAnalysisType == IND_REPLICATIONS) {
      replicationMenu = new JMenu("Replication");
      for (int i=0; i < ((List)runData[7]).size(); i++) {
        JMenuItem repItem = new JMenuItem(String.valueOf(i+1));
        repItem.addActionListener(this);
        repItem.setActionCommand("Replication item"+i);
        replicationMenu.add(repItem);
      }
      viewMenu.add(replicationMenu);
      showAllReplicationsItem = new JCheckBoxMenuItem("All replications");
      showAllReplicationsItem.addActionListener(this);
      viewMenu.add(showAllReplicationsItem);
      viewMenu.addSeparator();
    }
    showAnnotationsItem = new JCheckBoxMenuItem("Show annotations");
    showAnnotationsItem.addActionListener(this);
    viewMenu.add(showAnnotationsItem);
    // Help menu
    helpItem = new JMenuItem("Help");
    helpItem.addActionListener(this);
    helpMenu.add(helpItem);
    helpMenu.addSeparator();
    aboutItem = new JMenuItem("About");
    aboutItem.addActionListener(this);
    helpMenu.add(aboutItem);

    gb = new GridBagLayout();
    c = new GridBagConstraints();
    this.getContentPane().setLayout(gb);
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1.0;
    c.weighty = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    gb.setConstraints(rootPanel, c);
    this.getContentPane().add(rootPanel);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    this.setBounds((int)screenSize.getWidth()/2 - 313, (int)screenSize.getHeight()/2 - 300, 627, 600);
    this.setVisible(true);
  }

  private double[] getRunTimes() {
    if (outputAnalysisType == IND_REPLICATIONS) {
      return (double[])((Object[])((List)runData[7]).get(currentReplication))[0];
    } else {
      return (double[])runData[2];
    }
  }

  private double[] getTotalMean() {
    if (outputAnalysisType == NONE) {
      return new double[] {currentStat.average(currentMeasure, runTimes[1], runTimes[0])};
    } else {
      List confidenceIntervals = (List)runData[5];
      int confidenceIntervals_size = confidenceIntervals.size();
      for (int i=0; i < confidenceIntervals_size; i++) {
        Object[] c_data = (Object[])confidenceIntervals.get(i);
        if (((String)c_data[0]).equals(currentEntity)) {
          List measures = (List)c_data[1];
          int measures_size = measures.size();
          for (int j=0; j < measures_size; j++) {
            Object[] m_data = (Object[])measures.get(j);
            if (((String)m_data[0]).equals(currentMeasure)) {
              Object[] interval = (Object[])m_data[2];
              return new double[] {((Double)interval[0]).doubleValue(), ((Double)interval[1]).doubleValue(), ((Double)interval[2]).doubleValue()};
            }
          }
        }
      }
      return null;
    }
  }

  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    if (source == entitiesCombo) {
      currentEntity = (String)entitiesCombo.getSelectedItem();
      String[] measures = getMeasureNames(currentEntity);
      currentMeasure = measures[0];
      currentMeasureType = getMeasureType(currentEntity, currentMeasure);
      measuresCombo.setActionCommand("Updating");
      measuresCombo.removeAllItems();
      measureMenu.removeAll();
      for (int i=0; i < measures.length; i++) {
        measuresCombo.addItem(measures[i]);
        JMenuItem measureItem = new JMenuItem(measures[i]);
        measureItem.addActionListener(this);
        measureItem.setActionCommand("Measure item"+i);
        measureMenu.add(measureItem);
      }
      currentStat = getStat(currentEntity);
      currentReplication = 0;
      measuresCombo.setActionCommand("Ready");
      graph.setEntity(currentStat, currentMeasure, currentMeasureType, getTotalMean());
      if ((outputAnalysisType != NONE) && (totalRadio.isSelected())) {
        setTotalText();
      }
      if ((showAllRepsCheck == null) || !showAllRepsCheck.isSelected()) {
        setSampleText();
      }
    } else if (source == measuresCombo) {
      if (((JComboBox)source).getActionCommand().equals("Ready")) {
        currentMeasure = (String)measuresCombo.getSelectedItem();
        currentMeasureType = getMeasureType(currentEntity, currentMeasure);
        graph.setMeasure(currentMeasure, currentMeasureType, getTotalMean());
        if ((outputAnalysisType != NONE) && (totalRadio.isSelected())) {
          setTotalText();
        }
        if ((showAllRepsCheck == null) || !showAllRepsCheck.isSelected()) {
          setSampleText();
        }
      }
    } else if (source == zoomButton) {
      double zoomX, zoomY;
      try {
        zoomX = Double.parseDouble(zoomXField.getText())/100;
        zoomY = Double.parseDouble(zoomYField.getText())/100;
        if (zoomX < 0.35) {
          JOptionPane.showMessageDialog(this, "X-axis zoom too small.");
        } else if (zoomY < 0.25) {
          JOptionPane.showMessageDialog(this, "Y-axis zoom too small.");
        } else {
          graph.setZoom(zoomX, zoomY);
        }
      } catch (NumberFormatException nfe) {
        JOptionPane.showMessageDialog(this, "Invalid zoom level specified.");
      }
    } else if (source == viewRepButton) {
      int rep;
      try {
        rep = Integer.parseInt(repField.getText());
        if ((rep < 1) || (rep > ((List)runData[7]).size())) {
          JOptionPane.showMessageDialog(this, "Replication must be within 1 and " + ((List)runData[7]).size() + ".");
        } else {
          currentReplication = rep-1;
          currentStat = getStat(currentEntity);
          runTimes = getRunTimes();
          graph.setReplication(currentReplication, currentStat, runTimes[0], runTimes[1]);
          if (showSteadyStateCheck.isSelected()) {
            sampleStart = runTimes[1];
          } else {
            sampleStart = 0.0;
          }
          sampleEnd = runTimes[0];
          setSampleText();
        }
      } catch (NumberFormatException nfe) {
        JOptionPane.showMessageDialog(this, "Invalid replication specified.");
      }
    } else if (source == showSteadyStateCheck) { /** CHECK BUTTONS AND RADIO BUTTONS **/
      if (showSteadyStateCheck.isSelected()) {
        showSteadyStateItem.setSelected(true);
        sampleStart = runTimes[1];
        if (sampleEnd <= runTimes[1]) {
          sampleEnd = runTimes[0];
        }
        showTransientCheck.setEnabled(false);
        showTransientItem.setEnabled(false);
        graph.showSteadyState(true);
      } else {
        showSteadyStateItem.setSelected(false);
        sampleStart = 0.0;
        if ((showAllRepsCheck == null) || (!showAllRepsCheck.isSelected())) {
          showTransientItem.setEnabled(true);
          showTransientCheck.setEnabled(true);
        }
        graph.showSteadyState(false);
      }
      setSampleText();
    } else if (source == showTransientCheck) {
      if (showTransientCheck.isSelected()) {
        showTransientItem.setSelected(true);
        graph.showTransientTime(true);
      } else {
        showTransientItem.setSelected(false);
        graph.showTransientTime(false);
      }
    } else if (source == showMeanCheck) {
      if (showMeanCheck.isSelected()) {
        showTotalMeanItem.setSelected(true);
        graph.showTotalMean(true);
      } else {
        showTotalMeanItem.setSelected(false);
        graph.showTotalMean(false);
      }
    } else if (source == showAnnotationsCheck) {
      if (showAnnotationsCheck.isSelected()) {
        showAnnotationsItem.setSelected(true);
        graph.showAnnotations(true);
      } else {
        showAnnotationsItem.setSelected(false);
        graph.showAnnotations(false);
      }
    } else if (source == showBatchesCheck) {
      if (showBatchesCheck.isSelected()) {
        showBatchesItem.setSelected(true);
        graph.showBatches(true);
      } else {
        showBatchesItem.setSelected(false);
        graph.showBatches(false);
      }
    } else if (source == showAllRepsCheck) {
      if (showAllRepsCheck.isSelected()) {
        showAllReplicationsItem.setSelected(true);
        repField.setEnabled(false);
        viewRepButton.setEnabled(false);
        repLabel.setEnabled(false);
        showTransientCheck.setEnabled(false);
        showAnnotationsCheck.setEnabled(false);
        replicationMenu.setEnabled(false);
        showTransientItem.setEnabled(false);
        showAnnotationsItem.setEnabled(false);
        graph.showAllReplications(true);
        showLegend();
      } else {
        showAllReplicationsItem.setSelected(false);
        repField.setEnabled(true);
        viewRepButton.setEnabled(true);
        repLabel.setEnabled(true);
        showAnnotationsCheck.setEnabled(true);
        replicationMenu.setEnabled(true);
        showAnnotationsItem.setEnabled(true);
        if (!showSteadyStateCheck.isSelected()) {
          showTransientCheck.setEnabled(true);
          showTransientItem.setEnabled(true);
        }
        graph.showAllReplications(false);
        showSampleText();
      }
    } else if (source == infoRadio) {
      runInfoItem.setSelected(true);
      setInfoText();
    } else if (source == totalRadio) {
      totalMeasurementsItem.setSelected(true);
      setTotalText();
    } else if (source == openItem) {     /**** Menu bar change events ****/
      JFileChooser chooser = new JFileChooser();
      chooser.setDialogTitle("Open a SimJava graph");
      chooser.setFileFilter(new GraphFilter());
      chooser.setCurrentDirectory(new File(directory));
      int returnVal = chooser.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        oldGraphFile = graphFile;
        graphFile = chooser.getSelectedFile().getPath();
        if (!graphFile.endsWith(suffix)) {
          JOptionPane.showMessageDialog(this, "The file provided is of invalid format.");
          graphFile = oldGraphFile;
          return;
        }
        if (!loadData(false)) {
          return;
        }
        directory = chooser.getCurrentDirectory().getName();
        setupData();
        this.getContentPane().removeAll();
        initGUI();
      }
    } else if (source == saveItem) {
      try {
        ObjectOutputStream output = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(graphFile)));
        output.writeObject(runData);
        output.flush();
        output.close();
        JOptionPane.showMessageDialog(this, "Graph saved.");
      } catch (IOException ioe) {
        JOptionPane.showMessageDialog(this, "Unable to save graph.");
      }
    } else if (source == saveAsItem) {
      String file = null;
      JFileChooser chooser = new JFileChooser();
      chooser.setDialogTitle("Save as...");
      chooser.setFileFilter(new GraphFilter());
      chooser.setCurrentDirectory(new File(directory));
      int returnVal = chooser.showSaveDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        file = chooser.getSelectedFile().getPath();
        if (!file.endsWith(suffix)) {
          file += suffix;
        }
        File f = new File(file);
        int n = JOptionPane.YES_OPTION;
        if (f.exists()) {
          n = JOptionPane.showConfirmDialog(this,
                  "Overwrite " + f.getName() + "?",
                  "Save confirmation",
                  JOptionPane.YES_NO_OPTION);
        }
        if (n != JOptionPane.YES_OPTION) {
          return;
        }
        try {
          ObjectOutputStream output = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
          output.writeObject(runData);
          output.flush();
          output.close();
          graphFile = f.getName();
          this.setTitle("SimJava Graph Viewer - " + f.getName());
          directory = chooser.getCurrentDirectory().getName();
          JOptionPane.showMessageDialog(this, "Graph saved.");
        } catch (IOException ioe) {
          JOptionPane.showMessageDialog(this, "Unable to save graph.");
        }
      }
    } else if (source == saveAsImageItem) {
      String file = null;
      JFileChooser chooser = new JFileChooser();
      chooser.setDialogTitle("Save as image...");
      chooser.setFileFilter(new ImageFilter());
      chooser.setCurrentDirectory(new File(directory));
      int returnVal = chooser.showSaveDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        file = chooser.getSelectedFile().getPath();
        if (!file.endsWith(".gif")) {
          file += ".gif";
        }
        File f = new File(file);
        int n = JOptionPane.YES_OPTION;
        if (f.exists()) {
          n = JOptionPane.showConfirmDialog(this,
                  "Overwrite " + f.getName() + "?",
                  "Save confirmation",
                  JOptionPane.YES_NO_OPTION);
        }
        if (n != JOptionPane.YES_OPTION) {
          return;
        }
        try  {
          OutputStream out = new FileOutputStream(file);
          GifEncoder enc = new GifEncoder(graph.getGraphImage(), out);
          enc.encode();
          out.flush();
          out.close();
          directory = chooser.getCurrentDirectory().getName();
          JOptionPane.showMessageDialog(this, "Image saved.");
        } catch (IOException ioe) {
          JOptionPane.showMessageDialog(this, "Unable to save image.");
        }
      }
    } else if (source == exitItem) {
      System.exit(0);
    } else if (source == helpItem) {
      TextDialog aboutDialog = new TextDialog(this, TextDialog.HELP_DIALOG);
    } else if (source == aboutItem) {
      TextDialog aboutDialog = new TextDialog(this, TextDialog.ABOUT_DIALOG);
    } else if (source == runInfoItem) {
      infoRadio.setSelected(true);
      setInfoText();
    } else if (source == totalMeasurementsItem) {
      totalRadio.setSelected(true);
      setTotalText();
    } else if (source == showSteadyStateItem) {
      if (showSteadyStateItem.isSelected()) {
        showSteadyStateCheck.setSelected(true);
        sampleStart = runTimes[1];
        if (sampleEnd <= runTimes[1]) {
          sampleEnd = runTimes[0];
        }
        showTransientCheck.setEnabled(false);
        showTransientItem.setEnabled(false);
        graph.showSteadyState(true);
      } else {
        showSteadyStateCheck.setSelected(false);
        sampleStart = 0.0;
        if ((showAllRepsCheck == null) || (!showAllRepsCheck.isSelected())) {
          showTransientCheck.setEnabled(true);
          showTransientItem.setEnabled(true);
        }
        graph.showSteadyState(false);
      }
      setSampleText();
    } else if (source == showTransientItem) {
      if (showTransientItem.isSelected()) {
        showTransientCheck.setSelected(true);
        graph.showTransientTime(true);
      } else {
        showTransientCheck.setSelected(false);
        graph.showTransientTime(false);
      }
    } else if (source == showTotalMeanItem) {
      if (showTotalMeanItem.isSelected()) {
        showMeanCheck.setSelected(true);
        graph.showTotalMean(true);
      } else {
        showMeanCheck.setSelected(false);
        graph.showTotalMean(false);
      }
    } else if (source == showBatchesItem) {
      if (showBatchesItem.isSelected()) {
        showBatchesCheck.setSelected(true);
        graph.showBatches(true);
      } else {
        showBatchesCheck.setSelected(false);
        graph.showBatches(false);
      }
    } else if (source == showAnnotationsItem) {
      if (showAnnotationsItem.isSelected()) {
        showAnnotationsCheck.setSelected(true);
        graph.showAnnotations(true);
      } else {
        showAnnotationsCheck.setSelected(false);
        graph.showAnnotations(false);
      }
    } else if (source == showAllReplicationsItem) {
      if (showAllReplicationsItem.isSelected()) {
        showAllRepsCheck.setSelected(true);
        repField.setEnabled(false);
        viewRepButton.setEnabled(false);
        repLabel.setEnabled(false);
        showTransientCheck.setEnabled(false);
        showAnnotationsCheck.setEnabled(false);
        replicationMenu.setEnabled(false);
        showTransientItem.setEnabled(false);
        showAnnotationsItem.setEnabled(false);
        graph.showAllReplications(true);
        showLegend();
      } else {
        showAllRepsCheck.setSelected(false);
        repField.setEnabled(true);
        viewRepButton.setEnabled(true);
        repLabel.setEnabled(true);
        showAnnotationsCheck.setEnabled(true);
        replicationMenu.setEnabled(true);
        showAnnotationsItem.setEnabled(true);
        if (!showSteadyStateCheck.isSelected()) {
          showTransientCheck.setEnabled(true);
          showTransientItem.setEnabled(true);
        }
        graph.showAllReplications(false);
        showSampleText();
      }
    } else if (source instanceof JMenuItem) {
      // Entities, measures, replications
      String command = ((JMenuItem)source).getActionCommand();
      if (command.startsWith("Entity item")) {
        entitiesCombo.setSelectedIndex(Integer.parseInt(command.substring(11)));
      } else if (command.startsWith("Measure item")) {
        measuresCombo.setSelectedIndex(Integer.parseInt(command.substring(12)));
      } else if (command.startsWith("Replication item")) {
        currentReplication = Integer.parseInt(command.substring(16));
        repField.setText(String.valueOf(currentReplication+1));
        currentStat = getStat(currentEntity);
        runTimes = getRunTimes();
        graph.setReplication(currentReplication, currentStat, runTimes[0], runTimes[1]);
        if (showSteadyStateCheck.isSelected()) {
          sampleStart = runTimes[1];
        } else {
          sampleStart = 0.0;
        }
        sampleEnd = runTimes[0];
        setSampleText();
      }
    }
  }

  public void selectAnnotations() {
    if (!showAnnotationsCheck.isSelected()) {
      showAnnotationsCheck.setSelected(true);
      showAnnotationsItem.setSelected(true);
      graph.showAnnotations(true);
    }
  }

  public void mouseClicked(MouseEvent e) {
    if ((showAllRepsCheck == null) || (!showAllRepsCheck.isSelected())) {
      if (e.isMetaDown()) {
        // Click to add annotation
        graph.rightClick(e.getX(), e.getY());
      } else {
        if (graph.findAnnotation(e.getX(), e.getY())) {
          // Time click
          double timeClicked = graph.getTimeClicked(e.getX(), e.getY());
          if (timeClicked == -1.0) {
            sampleEnd = runTimes[0];
            hasClicked = false;
          } else {
            sampleEnd = timeClicked;
            hasClicked = true;
          }
          setSampleText();
        }
      }
    }
  }

  public void mousePressed(MouseEvent e) {};
  public void mouseReleased(MouseEvent e) {};
  public void mouseEntered(MouseEvent e) {};
  public void mouseExited(MouseEvent e) {};


  private void showSampleText() {
    samplePanel.remove(0);
    GridBagLayout gb = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    samplePanel.setLayout(gb);
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1;
    c.weighty = 1;
    c.insets = new Insets(5, 5, 5, 5);
    gb.setConstraints(sampleArea, c);
    samplePanel.add(sampleArea);
    samplePanel.validate();
  }

  private void showLegend() {
    ColorPanel legend = new ColorPanel(((List)runData[7]).size());
    ScrollPane scroll = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
    scroll.add(legend);
    samplePanel.remove(sampleArea);
    GridBagLayout gb = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    samplePanel.setLayout(gb);
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1;
    c.weighty = 1;
    c.insets = new Insets(5, 5, 5, 5);
    gb.setConstraints(scroll, c);
    samplePanel.add(scroll);
    samplePanel.validate();
  }

  private String[] getEntityNames() {
    String[] result = new String[entityMeasures.size()];
    int entityMeasures_size = entityMeasures.size();
    for (int i=0; i < entityMeasures_size; i++) {
      result[i] = (String)((Object[])entityMeasures.get(i))[0];
    }
    return result;
  }

  private String[] getMeasureNames(String entity) {
    String[] result = null;
    int entityMeasures_size = entityMeasures.size();
    for (int i=0; i < entityMeasures_size; i++) {
      Object[] eData = (Object[])entityMeasures.get(i);
      if (((String)eData[0]).equals(entity)) {
        List measures = (List)eData[1];
        result = new String[measures.size()];
        int measures_size = measures.size();
        for (int j=0; j < measures_size; j++) {
          result[j] = (String)((Object[])measures.get(j))[0];
        }
        break;
      }
    }
    return result;
  }


  // Gets the measures for an entity
  private List getMeasures(String entity) {
    int entityMeasures_size = entityMeasures.size();
    for (int i=0; i < entityMeasures_size; i++) {
      Object[] eData = (Object[])entityMeasures.get(i);
      if (((String)eData[0]).equals(entity)) {
        return (List)eData[1];
      }
    }
    return null;
  }

  // Gets an entity's Sim_stat
  private Sim_stat getStat(String entity) {
    Sim_stat stat;
    List stats;
    switch (outputAnalysisType) {
      case NONE:
        stats = (List)runData[4];
        break;
      case IND_REPLICATIONS:
        Object[] repData = (Object[])((List)runData[7]).get(currentReplication);
        stats = (List)repData[1];
        break;
      default:
        stats = (List)runData[7];
        break;
    }
    int stats_size = stats.size();
    for (int i=0; i < stats_size; i++) {
      stat = (Sim_stat)stats.get(i);
      if ((stat != null) && (stat.get_name().equals(entity))) {
        return stat;
      }
    }
    return null;
  }

  private int getMeasureType(String entity, String measure) {
    int entityMeasures_size = entityMeasures.size();
    for (int i=0; i < entityMeasures_size; i++) {
      Object[] eData = (Object[])entityMeasures.get(i);
      if (((String)eData[0]).equals(entity)) {
        List measures = (List)eData[1];
        int measures_size = measures.size();
        for (int j=0; j < measures_size; j++) {
          Object[] mData = (Object[])measures.get(j);
          if (((String)mData[0]).equals(measure)) {
            return ((Integer)mData[1]).intValue();
          }
        }
      }
    }
    return -1;
  }

  private void setSampleText() {
    StringBuffer text = new StringBuffer("Sample measurements:\n" +
                                         "--------------------\n\n" +
                                         "From: " + sampleStart + "\n" +
                                         "To:   " + sampleEnd + "\n\n");
    switch (currentMeasureType) {
      case RATE_BASED:
        text.append("Sample mean:          " + currentStat.average(currentMeasure, sampleStart, sampleEnd) + "\n");
        text.append("Event count:          " + currentStat.count(currentMeasure, sampleStart, sampleEnd) + "\n");
        break;
      default:
        text.append("Sample mean:          " + currentStat.average(currentMeasure, sampleStart, sampleEnd) + "\n");
        if (!currentMeasure.equals("Utilisation")) {
          text.append("Sample variance:      " + currentStat.variance(currentMeasure, sampleStart, sampleEnd) + "\n");
          text.append("Sample std deviation: " + currentStat.std_deviation(currentMeasure, sampleStart, sampleEnd) + "\n");
          text.append("Maximum:              " + currentStat.maximum(currentMeasure, sampleStart, sampleEnd) + "\n");
          text.append("Minimum:              " + currentStat.minimum(currentMeasure, sampleStart, sampleEnd) + "\n");
          double[] levels = currentStat.get_levels(currentMeasure);
          if (levels != null) {
            text.append("Exceedence proportions:\n");
            double[] props = currentStat.exc_proportion(currentMeasure, levels, sampleStart, sampleEnd);
            for (int k=0; k < levels.length; k++) {
              if (k+1 == levels.length) {
                text.append("      " + levels[k] + " < " + currentMeasure + " : " + props[k] + "\n");
              } else {
                text.append("      " + levels[k] + " < " + currentMeasure + " <= " + levels[k+1] + " : " + Math.abs(props[k]-props[k+1]) + "\n");
              }
            }
          }
        }
        break;
    }
    sampleArea.setText(text.toString());
  }

  private void setInfoText() {
    StringBuffer text = new StringBuffer();
    text.append("Overall simulation run information:\n");
    text.append("-----------------------------------\n\n");
    text.append("Total simulated time:       " + ((double[])runData[2])[0] + "\n");
    text.append("Total transient time:       " + ((double[])runData[2])[1] + "\n");
    text.append("Total steady state time:    " + (((double[])runData[2])[0] - ((double[])runData[2])[1]) + "\n");
    int transCondition = ((int[])runData[1])[0],
        termCondition = ((int[])runData[1])[1];
    String transConditionName, termConditionName, outputAnalysisTypeName;
    switch (transCondition) {
      case EVENTS_COMPLETED:
        transConditionName = "Event completions";
        break;
      case TIME_ELAPSED:
        transConditionName = "Simulation time elapsed";
        break;
      case MIN_MAX:
        transConditionName = "Truncation based on minimum and maximum observations";
        break;
      default:
        transConditionName = "None";
        break;
    }
    switch (termCondition) {
      case EVENTS_COMPLETED:
        termConditionName = "Event completions";
        break;
      case TIME_ELAPSED:
        termConditionName = "Simulation time elapsed";
        break;
      case INTERVAL_ACCURACY:
        termConditionName = "Confidence interval accuracy";
        break;
      default:
        termConditionName = "None";
        break;
    }
    switch (outputAnalysisType) {
      case IND_REPLICATIONS:
        outputAnalysisTypeName = "Independent replications";
        break;
      case BATCH_MEANS:
        outputAnalysisTypeName = "Batch means";
        break;
      default:
        outputAnalysisTypeName = "None";
        break;
    }
    text.append("Transient condition type:   " + transConditionName + "\n");
    text.append("Termination condition type: " + termConditionName + "\n");
    text.append("Output analysis method:     " + outputAnalysisTypeName + "\n");
    if (outputAnalysisType == IND_REPLICATIONS) {
      text.append("Confidence level:           " + (Double)runData[4] + "\n");
      text.append("Replications performed:     " + (Integer)runData[6] + "\n");
    } else if (outputAnalysisType == BATCH_MEANS) {
      text.append("Confidence level:           " + (Double)runData[4] + "\n");
      text.append("Number of batches:          " + (Integer)((Object[])runData[6])[0] + "\n");
      text.append("Individual batch length:    " + (Double)((Object[])runData[6])[1] + "\n");
    }
    totalArea.setText(text.toString());
  }

  private void setTotalText() {
    if (outputAnalysisType == NONE) {
    } else {
      StringBuffer text = new StringBuffer("Total measurements:\n-------------------\n\n");
      List confidenceIntervals = (List)runData[5];
      int confidenceIntervals_size = confidenceIntervals.size();
      for (int i=0; i < confidenceIntervals_size; i++) {
        Object[] cData = (Object[])confidenceIntervals.get(i);
        if (((String)cData[0]).equals(currentEntity)) {
          List measures = (List)cData[1];
          int measures_size = measures.size();
          for (int j=0; j <measures_size; j++) {
            Object[] mData = (Object[])measures.get(j);
            String mName = (String)mData[0];
            Object[] measurements = (Object[])mData[2];
            if (mName.equals(currentMeasure)) {
              switch (currentMeasureType) {
                case RATE_BASED:
                  text.append("Total mean:          " + ((Double)measurements[1]).doubleValue() + "\n");
                  text.append("Interval low bound:  " + ((Double)measurements[0]).doubleValue() + "\n");
                  text.append("Interval high bound: " + ((Double)measurements[2]).doubleValue() + "\n");
                  text.append("Interval half width: " + (((Double)measurements[2]).doubleValue()-((Double)measurements[0]).doubleValue())/2.0 + "\n");
                  text.append("Accuracy ratio:      " + ((Double)measurements[4]).doubleValue() + "\n");
                  text.append("Mean variance:       " + ((Double)measurements[3]).doubleValue() + "\n");
                  text.append("Mean std deviation:  " + ((Double)measurements[5]).doubleValue() + "\n");
                  text.append("Average event count: " + ((Double)measurements[6]).doubleValue() + "\n");
                  break;
                default:
                  text.append("Total mean:          " + ((Double)measurements[1]).doubleValue() + "\n");
                  text.append("Interval low bound:  " + ((Double)measurements[0]).doubleValue() + "\n");
                  text.append("Interval high bound: " + ((Double)measurements[2]).doubleValue() + "\n");
                  text.append("Interval half width: " + (((Double)measurements[2]).doubleValue()-((Double)measurements[0]).doubleValue())/2.0 + "\n");
                  text.append("Accuracy ratio:      " + ((Double)measurements[4]).doubleValue() + "\n");
                  text.append("Mean variance:       " + ((Double)measurements[3]).doubleValue() + "\n");
                  text.append("Mean std deviation:  " + ((Double)measurements[5]).doubleValue() + "\n");
                  if (!mName.equals("Utilisation")) {
                    text.append("Total maximum:       " + ((Double)measurements[6]).doubleValue() + "\n");
                    text.append("Total minimum:       " + ((Double)measurements[7]).doubleValue() + "\n");
                    if (measurements.length == 9) {
                      text.append("Total average exceedence proportions:\n");
                      double[][] totalProps = (double[][])measurements[8];
                      for (int k=0; k < totalProps.length; k++) {
                        if (k+1 == totalProps.length) {
                          text.append("    " + totalProps[k][0] + " < " + mName + " : " + totalProps[k][1] + "\n");
                        } else {
                          text.append("    " + totalProps[k][0] + " < " + mName + " <= " + totalProps[k+1][0] + " : " + Math.abs(totalProps[k][1]-totalProps[k+1][1]) + "\n");
                        }
                      }
                    }
                  }
                  break;
              }
              break;
            }
          }
        }
      }
      totalArea.setText(text.toString());
    }
  }

  private static void loadInitialFile() {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Open a SimJava graph");
    chooser.setFileFilter(new GraphFilter());
    chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
    int returnVal = chooser.showOpenDialog(null);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      SJGV gui = new SJGV(chooser.getSelectedFile().getPath(), chooser.getCurrentDirectory().getName());
    } else {
      System.exit(0);
    }
  }

  public static void main(String[] args) {
    if (args.length > 1) {
      System.out.println("Only one graph data object may be passed to the viewer.");
      System.exit(0);
    }
    if (args.length == 1) {
      if (args[0].endsWith(suffix)) {
        SJGV gui = new SJGV(args[0], System.getProperty("user.dir"));
      } else {
        System.out.println("The file provided is of invalid format.");
        System.exit(0);
      }
    } else {
      loadInitialFile();
    }
  }
}


