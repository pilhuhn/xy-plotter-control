package de.bsd.plottercontrol;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author hrupp
 */
public class KochKurve {

  StringBuilder sb;
  InputStream is;
  OutputStream outs;
  int tokenCount;
  private PlotterComm pc;

  public KochKurve(PlotterComm pc) {
    this.pc = pc;
  }

  public static void main(String[] args) throws Exception {


    PlotterComm pc = new PlotterComm("/dev/cu.usbmodem14522441");

    System.out.println("Press RETURN to start");
    System.in.read();

    try {
      KochKurve kk = new KochKurve(pc);
      pc.penDown();
      pc.send("r 1\n");
      kk.paint(5);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      pc.penUp();
      pc.send("r 160\n");
      pc.disconnect();
    }
  }

  private void paint(int level) throws Exception {

    sb = new StringBuilder();



    pc.sendStartCommand(false);


    System.out.println("Starting Kurve");
    tokenCount = 0;

    double x1=10, y1=310, angle=-Math.PI/3;
    for (int i=0; i<3; i++) {
      drawCurve(x1, y1, angle, 100, level);


      System.out.println("=== X1: " + x1 + ", Y1: " + y1);

      // Flush remaining data
      sb = pc.flushRemainingData(sb);

      x1 += 350*Math.cos(angle);
      y1 += 350*Math.sin(angle);
      angle += 2*Math.PI/3;

    }

    pc.sendEnd();

//    pc.readNDisplay(true);





    System.out.println("Sent " + tokenCount + " tokens");

  }


  private void drawCurve(double x1, double y1,
                         double angle1, double sideLength, int level) throws Exception {

      // (x1,y1), (x2,y2), (x3,y3), (x4,y4) = Startpunkte für die Teilstrecken
      // sideLength = Länge einer Teilstrecke
      // angle = Winkel zwischen den Teilstrecken
      // Jede Teilstrecke wird eindeutig durch einen Startpunkt,
      // einen Winkel und eine Länge definiert
      // Anzahl der Teilstrecken = 3*4^(level-1), level 1 = gleichseitiges Dreieck

      double x2, y2, angle2, x3, y3, angle3, x4, y4;

      if (level>1) {
        // Übergebene Teilstrecke in vier neue Teilstrecken zerlegen

        sideLength /= 3;
        level -= 1;

        // erste Teilstrecke
        drawCurve(x1, y1, angle1, sideLength, level);

        // zweite Teilstrecke
        x2 = x1+sideLength*Math.cos(angle1);
        y2 = y1+sideLength*Math.sin(angle1);
        angle2 = angle1-Math.PI/3;
        drawCurve(x2, y2, angle2, sideLength, level);

        // dritte Teilstrecke
        x3 = x2+sideLength*Math.cos(angle2);
        y3 = y2+sideLength*Math.sin(angle2);
        angle3 = angle1+Math.PI/3;
        drawCurve(x3, y3, angle3, sideLength, level);

        // vierte Teilstrecke
        x4 = x3+sideLength*Math.cos(angle3);
        y4 = y3+sideLength*Math.sin(angle3);
        // angle4 = angle1
        drawCurve(x4, y4, angle1, sideLength, level);
      }
      else {
        // Teilstrecke zeichnen
//        g.drawLine((int)x1,(int)y1,
//            (int)(x1+sideLength*Math.cos(angle1)),(int)(y1+sideLength*Math.sin(angle1)));
        long dx = (long) (sideLength*Math.cos(angle1)*160);
        long dy = (long) (sideLength*Math.sin(angle1)*160);

        String s = "X" + dx + " Y" + dy ;
        sb.append(s);
        tokenCount++;

        sb = pc.flushToPlotterIfNeeded(sb);

      }
  }


}
