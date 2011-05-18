/* Import statements */

import eduni.simjava.Sim_stat;
import java.awt.Panel;
import java.awt.Font;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.Graphics;
import java.util.List;
import java.util.ArrayList;
import java.text.NumberFormat;

class Graph extends Panel {
  private static final int RATE_BASED = 0;
  private static final int STATE_BASED = 1;
  private static final int INTERVAL_BASED = 2;

  private static final int initialWidth = 600;
  private static final int initialHeight = 400;
  private Color bgColor = Color.white;
  private Color axisColor = Color.black;
  private Color plotColor = Color.red;
  private Color letterColor = new Color(100, 100, 100);
  private Color totalMeanColor = new Color(165, 0, 0);
  private Color intervalColor = new Color(255, 212, 0);
  private Color intervalContentColor = new Color(255, 255, 0);
  private Color transientTimeColor = new Color(0, 0, 255);
  private Color transientContentColor = new Color(0, 203, 255);
  private Color batchColor = new Color(0, 155, 0);
  private Color batchMeanColor = new Color(0, 255, 0);

  private Image staticImage;
  private Graphics staticGraphics;
  private Sim_stat stat;
  private double zoomX = 1.0,
                 zoomY = 1.0;
  private int panelWidth = initialWidth;
  private int panelHeight = initialHeight;
  private double endTime = 0.0;
  private double startTime = 0.0;
  private String measure;
  private int measureType;
  private double minValue, maxValue;
  private NumberFormat nf;
  private boolean showTransientTime = false;
  private boolean showTotalMean = false;
  private boolean showBatches = false;
  private double[] totalMean;
  private double transTime;
  private int batches;
  private double batchLength;
  private int replication = -1;
  private List replications;
  private boolean showAllReplications = false;
  private boolean showSteadyState = false;
  private double repsStartTime,
                 repsEndTime;
  private double clickedTime = -1.0;

  private double annotationTime = -1.0,
                 annotationValue = -1.0;
  private boolean showAnnotations = false;
  private Image aImage = Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("a.gif"));
  private SJGV parent;

  Graph(SJGV parent, Sim_stat stat, String measure, int measureType, double startTime, double endTime, double transTime, double totalMean[]) {
    super();
    this.parent = parent;
    this.stat = stat;
    this.measure = measure;
    this.measureType = measureType;
    this.startTime = startTime;
    this.endTime = endTime;
    this.transTime = transTime;
    this.totalMean = totalMean;
    this.setSize(initialWidth, initialHeight);
    nf = NumberFormat.getInstance();
  }

  Graph(SJGV parent, int replication, Sim_stat stat, String measure, int measureType, double startTime, double endTime, double transTime, double totalMean[], List replications) {
    super();
    this.parent = parent;
    this.replication = replication;
    this.stat = stat;
    this.measure = measure;
    this.measureType = measureType;
    this.startTime = startTime;
    this.endTime = endTime;
    this.transTime = transTime;
    this.totalMean = totalMean;
    this.replications = replications;
    this.setSize(initialWidth, initialHeight);
    nf = NumberFormat.getInstance();
  }

  public void setZoom(double zoomX, double zoomY) {
    this.zoomX = zoomX;
    this.zoomY = zoomY;
    panelWidth = (int)(zoomX*initialWidth);
    panelHeight = (int)(zoomY*initialHeight);
    staticImage = createImage(panelWidth, panelHeight);
    staticGraphics = staticImage.getGraphics();
    repaint();
  }
  public double getZoomX() { return zoomX; }
  public double getZoomY() { return zoomY; }

  private void setupStatic() {
    staticImage = createImage(panelWidth, panelHeight);
    staticGraphics = staticImage.getGraphics();
    this.setSize(panelWidth, panelHeight);
  }

  private void drawGraph(Graphics g) {
    // Draw the background
    g.setColor(bgColor);
    g.fillRect(0, 0, panelWidth, panelHeight);
    // Draw the plot
    drawPlot(g);
  }

  private void drawPlot(Graphics g) {
    int width = panelWidth-150;
    int height = panelHeight-100;
    double startTime = 0.0, endTime = 0.0;

    /* GET THE X-AXIS TIMES */
    if (showAllReplications) {
      if (showSteadyState) {
        int replications_size = replications.size();
        for (int i=0; i < replications_size; i++) {
          double[] rTimes = (double[])((Object[])replications.get(i))[0];
          if (i == 0) {
            startTime = rTimes[1];
            endTime = rTimes[0];
          } else {
            if (rTimes[1] < startTime) {
              startTime = rTimes[1];
            }
            if (rTimes[0] > endTime) {
              endTime = rTimes[0];
            }
          }
        }
      } else {
        int replications_size = replications.size();
        for (int i=0; i < replications_size; i++) {
          double[] rTimes = (double[])((Object[])replications.get(i))[0];
          if (i == 0) {
            endTime = rTimes[0];
          } else {
            if (rTimes[0] > endTime) {
              endTime = rTimes[0];
            }
          }
        }
      }
      repsStartTime = startTime;
      repsEndTime = endTime;
    } else {
      startTime = this.startTime;
      endTime = this.endTime;
    }
    double inc = (endTime-startTime) / width;
    double[] times = new double[width+1];
    for (int i=0; i <= width; i++) {
      times[i] = startTime + (i)*inc;
    }

    /* GET THE Y-AXIS VALUES (MIN-MAX) */
    minValue = 0.0;
    maxValue = 0.0;
    double[] values = null;
    double[][] valuesR = null;
    if (showTotalMean) {
      if (totalMean.length == 1) {
        maxValue = totalMean[0]; // Set the maximum to be the total mean
        if (totalMean[0] < minValue) {
          minValue = totalMean[0];
        }
      } else {
        maxValue = totalMean[2]; // Set the maximum to be the confidence interval's high bound
        if (totalMean[0] < minValue) {
          minValue = totalMean[0];
        }
      }
    }
    if (showAllReplications) {
      int replications_size = replications.size();
      valuesR = new double[replications_size][width];
      for (int i=0; i < replications_size; i++) {
        List stats = (List)((Object[])replications.get(i))[1];
        int stats_size = stats.size();
        for (int j=0; j < stats_size; j++) {
          Sim_stat currentStat = (Sim_stat)stats.get(j);
          if ((currentStat != null) && (currentStat.get_name().equals(stat.get_name()))) {
            Object[] vData = currentStat.averages(measure, times);
            valuesR[i] = (double[])vData[0];
            if (((Double)vData[1]).doubleValue() < minValue) {
              minValue = ((Double)vData[1]).doubleValue();
            }
            if (((Double)vData[2]).doubleValue() > maxValue) {
              maxValue = ((Double)vData[2]).doubleValue();
            }
          }
        }
      }
    } else {
      Object[] vData = stat.averages(measure, times);
      values = (double[])vData[0];
      if (showBatches) {
        for (int i=0; i < batches; i++) {
          double batchMean = stat.average(measure, transTime+(i*batchLength), transTime+((i+1)*batchLength));
          if (batchMean < minValue) {
            minValue = batchMean;
          }
          if (batchMean > maxValue) {
            maxValue = batchMean;
          }
        }
      }
      if (((Double)vData[1]).doubleValue() < minValue) {
        minValue = ((Double)vData[1]).doubleValue();
      }
      if (((Double)vData[2]).doubleValue() > maxValue) {
        maxValue = ((Double)vData[2]).doubleValue();
      }
    }

    /* DRAW THE CONFIDENCE INTERVAL CONTENT */
    int lowBoundScaled = 0, highBoundScaled = 0;
    if (showTotalMean && (totalMean.length == 3)) {
      lowBoundScaled = getScaledY(totalMean[0]);
      highBoundScaled = getScaledY(totalMean[2]);
      // Draw interval content lines
      int currentX = 100;
      g.setColor(intervalContentColor);
      while (currentX <= (panelWidth-40)) {
        g.drawLine(currentX, highBoundScaled, currentX, lowBoundScaled);
        currentX += 5;
      }
      g.setColor(intervalColor);
      g.drawLine(100, lowBoundScaled, panelWidth-40, lowBoundScaled);
      g.drawLine(100, highBoundScaled, panelWidth-40, highBoundScaled);
    }
    /* DRAW THE TRANSIENT TIME */
    if (showTransientTime && !showAllReplications) {
      if ((transTime > 0.0) && (startTime != transTime)) {
        int transTimeScaled = getScaledX(transTime);
        g.setColor(transientContentColor);
        int currentY = (panelHeight-50)-5;
        while (currentY >= 40) {
          g.drawLine(100, currentY, transTimeScaled, currentY);
          currentY -= 5;
        }
        g.setColor(transientTimeColor);
        g.drawLine(transTimeScaled , 40, transTimeScaled, panelHeight-50);
      }
    }

    /* DRAW THE PLOT */
    if (showAllReplications) {
      int replications_size = replications.size();
      for (int i = 0; i < replications_size; i++) {
        g.setColor(ColorChooser.getColor(i));
        for (int j=0; j < width; j++) {
          g.drawLine(getScaledX(times[j]), getScaledY(valuesR[i][j]), getScaledX(times[j+1]), getScaledY(valuesR[i][j+1]));
        }
      }
    } else {
      if (replication != -1) {
        g.setColor(ColorChooser.getColor(replication));
      } else {
        g.setColor(plotColor);
      }
      for (int i=0; i < width; i++) {
        g.drawLine(getScaledX(times[i]), getScaledY(values[i]), getScaledX(times[i+1]), getScaledY(values[i+1]));
      }
      /* DRAW THE BATCHES AND THEIR MEANS */
      if (showBatches) {
        g.setColor(batchColor);
        for (int i=0; i < batches; i++) {
          double batchStart = transTime + batchLength*i;
          double batchEnd = transTime + batchLength*(i+1);
          int batchScaled = getScaledX(batchStart);
          g.drawLine(batchScaled, 40, batchScaled, panelHeight-50);
          int batchMeanScaled = getScaledY(stat.average(measure, batchStart, batchEnd));
          g.drawLine(batchScaled+1, batchMeanScaled, getScaledX(batchEnd), batchMeanScaled);
        }
        int batchScaled = getScaledX(transTime + batchLength*batches);
        g.drawLine(batchScaled, 40, batchScaled, panelHeight-50);
      }
    }

    /* DRAW THE TOTAL MEAN */
    if (showTotalMean) {
      if (totalMean.length == 3) {
        g.setColor(totalMeanColor);
        int totalMeanScaled = getScaledY(totalMean[1]);
        g.drawLine(100, totalMeanScaled, panelWidth-40, totalMeanScaled);
      } else {
        g.setColor(totalMeanColor);
        int totalMeanScaled = getScaledY(totalMean[0]);
        g.drawLine(100, totalMeanScaled, panelWidth-40, totalMeanScaled);
      }
    }
    /* DRAW THE AXES */
    g.setColor(axisColor);
    g.drawLine(100, 40, 100, panelHeight-40);
    int xAxisHeight = getScaledY(0.0);
    g.drawLine(90, xAxisHeight, panelWidth-40, xAxisHeight);
    /* DRAW THE GRAPH TITLE */
    g.setColor(axisColor);
    g.setFont(new Font("TimesNewRoman", Font.BOLD+Font.ITALIC, 26));
    if (replication == -1) {
      g.drawString(stat.get_name() + ": " + measure, 15, 25);
    } else {
      if (showAllReplications) {
        g.drawString(stat.get_name() + ": " + measure + " - All " + replications.size() + " replications", 15, 25);
      } else {
        g.drawString(stat.get_name() + ": " + measure + " - Replication " + (replication+1), 15, 25);
      }
    }
    /* DRAW THE GRAPH LABELS */
    g.setColor(axisColor);
    g.setFont(new Font("TimesNewRoman", Font.PLAIN, 14));
    // Along the X-axis
    int gaps = 0;
    int incG = 0;
    do {
      gaps++;
      incG = width/gaps;
    } while (incG >= 150);
    String label = "";
    for (int i=0; i <= gaps; i++) {
      if (i == gaps) {
        int integerLength = getIntegerDigitCount(endTime);
        nf.setMinimumFractionDigits(10-integerLength);
        nf.setMaximumFractionDigits(10-integerLength);
        label = nf.format(endTime);
        g.drawLine(panelWidth-50, xAxisHeight, panelWidth-50, xAxisHeight+3);
        g.drawString(label, (panelWidth-50)-40, xAxisHeight+20);
      } else {
        double val = getUnscaledX(100 + i*incG);
        if (val == 0.0) {
          g.drawLine(100+i*incG, xAxisHeight, 100+i*incG, xAxisHeight+3);
          g.drawString("0.0", (100+i*incG)-25, xAxisHeight+20);
        } else {
          int integerLength = getIntegerDigitCount(val);
          nf.setMinimumFractionDigits(10-integerLength);
          nf.setMaximumFractionDigits(10-integerLength);
          label = nf.format(val);
          g.drawLine(100+i*incG, xAxisHeight, 100+i*incG, xAxisHeight+3);
          g.drawString(label, (100+i*incG)-40, xAxisHeight+20);
        }
      }
    }
    // Along the Y-axis
    gaps = 0;
    incG = 0;
    do {
      gaps++;
      incG = height/gaps;
    } while (incG >= 100);
    double labelValue = 0.0;
    for (int i=0; i <= gaps; i++) {
      if (i == 0) {
        if (minValue == 0.0) {
          continue;
        }
        labelValue = minValue;
      } else if (i == gaps) {
        labelValue = maxValue;
      } else {
        labelValue = getUnscaledY((panelHeight-50)-i*incG);
      }
      int integerLength = getIntegerDigitCount(labelValue);
      nf.setMinimumFractionDigits(10-integerLength);
      nf.setMaximumFractionDigits(10-integerLength);
      label = nf.format(labelValue);
      if (i == gaps) {
        g.drawLine(100 - 3, 50, 100, 50);
        g.drawString(label, 15, 50+5);
      } else {
        g.drawLine(100 - 3, (panelHeight-50)-i*incG, 100, (panelHeight-50)-i*incG);
        g.drawString(label, 15, (panelHeight-50)-i*incG+5);
      }
    }
    /* DRAW THE CLICKED POINT */
    if (!showAllReplications && (clickedTime != -1.0)) {
      int clickedX = getScaledX(clickedTime);
      int clickedY = getScaledY(stat.average(measure, startTime, clickedTime));
      g.setColor(new Color(0, 35, 165));
      g.drawLine(100, clickedY, clickedX, clickedY);
      g.drawLine(clickedX, xAxisHeight, clickedX, clickedY);
    }
    /* DRAW THE ANNOTATIONS */
    if (!showAllReplications && showAnnotations) {
      List annotations = stat.getAnnotations(measure);
      if (annotations != null) {
        int annotations_size = annotations.size();
        for (int i=0; i < annotations_size; i++) {
          Object[] aData = (Object[])annotations.get(i);
          int x = getScaledX(((Double)aData[0]).doubleValue());
          int y = getScaledY(((Double)aData[1]).doubleValue());
          if ((x >= 100) && (x <= panelWidth-50) && (y >= 50) && (y <= panelHeight-50)) {
            g.drawImage(aImage, x, y, Color.white, this);
          }
        }
      }
    }
  }

  /*
  To Scale a value V in an Intarval (minV, maxV) represented as (minX, maxX):
    scaledValue = [(V-minV)/(maxV-minV)] * (maxX-minX) + minX
  */
  private int getScaledX(double x) {
    if (showAllReplications) {
      return (int)(((x-repsStartTime)/(repsEndTime-repsStartTime))*(panelWidth-50-100)) + 100;
    } else {
      return (int)(((x-startTime)/(endTime-startTime))*(panelWidth-50-100)) + 100;
    }
  }

  private int getScaledY(double y) {
    return panelHeight-((int)(((y-minValue)/(maxValue-minValue))*((panelHeight-50)-50))+50);
  }

  private double getUnscaledX(int x) {
    if (showAllReplications) {
      return (((double)(x-100))/((double)(panelWidth-150)))*(repsEndTime-repsStartTime) + repsStartTime;
    } else {
      return (((double)(x-100))/((double)(panelWidth-150)))*(endTime-startTime) + startTime;
    }
  }

  private double getUnscaledY(int y) {
    return ((double)(panelHeight-y-50))/((double)((panelHeight-50)-50))*(maxValue-minValue);
  }

  public void paint(Graphics g) {
    this.setSize(panelWidth, panelHeight);
    if (staticImage == null) {
      setupStatic();
    }
    drawGraph(staticGraphics);
    g.drawImage(staticImage,0,0,this);
  }

  public void update(Graphics g) {
    paint(g);
  }

  public void showSteadyState(boolean show) {
    showSteadyState = show;
    if (show) {
      if (clickedTime <= transTime) {
        clickedTime = -1.0;
      }
      startTime = transTime;
    } else {
      startTime = 0.0;
    }
    this.repaint();
  }

  public void setMeasure(String measure, int measureType, double[] totalMean) {
    this.measure = measure;
    this.measureType = measureType;
    this.totalMean = totalMean;
    clickedTime = -1.0;
    this.repaint();
  }

  public void setEntity(Sim_stat stat, String measure, int measureType, double[] totalMean) {
    this.stat = stat;
    this.measure = measure;
    this.measureType = measureType;
    this.totalMean = totalMean;
    clickedTime = -1.0;
    this.repaint();
  }

  public void setReplication(int replication, Sim_stat stat, double endTime, double transTime) {
    this.replication = replication;
    this.stat = stat;
    this.endTime = endTime;
    this.transTime = transTime;
    if (showSteadyState) {
      startTime = transTime;
    }
    clickedTime = -1.0;
    this.repaint();
  }

  public void setBatches(int count, double length) {
    batches = count;
    batchLength = length;
  }

  public void showTransientTime(boolean show) {
    showTransientTime = show;
    this.repaint();
  }

  public void showTotalMean(boolean show) {
    showTotalMean = show;
    this.repaint();
  }

  public void showBatches(boolean show) {
    showBatches = show;
    this.repaint();
  }

  public void showAllReplications(boolean show) {
    showAllReplications = show;
    this.repaint();
  }

  public void showAnnotations(boolean show) {
    showAnnotations = show;
    this.repaint();
  }

  private int getIntegerDigitCount(double value) {
    int count = 1;
    int v = (int)value;
    while ((v / 10) != 0) {
      count++;
      v /= 10;
    }
    return count;
  }

  public double getTimeClicked(int x, int y) {
    if ((x <= 100) || (x > panelWidth-50) || (y < 50) || (y > panelHeight-40)) {
      clickedTime = -1.0;
      this.repaint();
    } else {
      clickedTime = getUnscaledX(x);
      this.repaint();
    }
    return clickedTime;
  }

  public void rightClick(int x, int y) {
    if ((x > 100) && (x <= panelWidth-50) && (y >= 50) && (y <= panelHeight-40)) {
      annotationTime = getUnscaledX(x);
      annotationValue = getUnscaledY(y);
      AnnotationDialog dialog = new AnnotationDialog(parent, this);
    }
  }

  // Returns the annotation's text for the clicked point
  public void addAnnotation(String text) {
    if (!text.equals("")) {
      stat.addAnnotation(measure, new Object[] {new Double(annotationTime), new Double(annotationValue), text});
      parent.selectAnnotations();
      this.repaint();
    }
  }

  // Sets the text of the currently selected annotation
  public void setAnnotation(String text) {
    if (text.equals("")) {
      removeAnnotation();
    } else {
      List annotations = stat.getAnnotations(measure);
      if (annotations != null) {
        int annotations_size = annotations.size();
        for (int i=0; i < annotations_size; i++) {
          Object[] aData = (Object[])annotations.get(i);
          if ((annotationTime == ((Double)aData[0]).doubleValue()) && (annotationValue == ((Double)aData[1]).doubleValue())) {
            aData[2] = text;
            break;
          }
        }
      }
    }
  }

  // Removes the currently selected annotation
  public void removeAnnotation() {
    List annotations = stat.getAnnotations(measure);
    if (annotations != null) {
      int annotations_size = annotations.size();
      for (int i=0; i < annotations_size; i++) {
        Object[] aData = (Object[])annotations.get(i);
        if ((annotationTime == ((Double)aData[0]).doubleValue()) && (annotationValue == ((Double)aData[1]).doubleValue())) {
          annotations.remove(i);
          break;
        }
      }
    }
    this.repaint();
  }

  public boolean findAnnotation(int x, int y) {
    if (!showAnnotations || showAllReplications) {
      return true;
    }
    List annotations = stat.getAnnotations(measure);
    if (annotations != null) {
      int annotations_size = annotations.size();
      for (int i=0; i < annotations_size; i++) {
        Object[] aData = (Object[])annotations.get(i);
        annotationTime = ((Double)aData[0]).doubleValue();
        annotationValue = ((Double)aData[1]).doubleValue();
        int aX = getScaledX(annotationTime);
        int aY = getScaledY(annotationValue);
        if ((x >= aX) && (x <= aX+18) && (y >= aY) && (y <= aY+18)) {
          // Annotation found
          AnnotationDialog dialog = new AnnotationDialog(parent, this, (String)aData[2]);
          return false;
        }
      }
    }
    return true;
  }

  public Image getGraphImage() {
    return staticImage;
  }

}
