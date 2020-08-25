/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.bsd.plottercontrol;

import java.io.IOException;

/**
 * @author hrupp
 */
public class HilbertKurve {

  private PlotterComm pc;

  public HilbertKurve(PlotterComm pc) {
    this.pc = pc;
  }

  public static void main(String[] args) {
    PlotterComm pc = new PlotterComm("/dev/cu.usbmodem14522441" +
                                         "");
    try {
      HilbertKurve kk = new HilbertKurve(pc);
      kk.paint();
    } catch (Exception e) {
      e.printStackTrace();
      pc.disconnect();
    }
  }


  Point a, b; // Verbindungspunkte eines Einzelschritts

  int lengthF = 6;     // Schrittlänge Startwert
  double direction;      // Richtung in Grad
  double rotation = 90;  // Drehung in Grad

  int depth = 6;         // Iterationen Startwert

  StringBuilder sb = new StringBuilder();
  int tokenCount;



  private void paint() throws IOException, InterruptedException {



    System.out.println("Press RETURN to start");
    System.in.read();

    pc.penDown();
    pc.sendStartCommand(false);

    // Start-Punkt
    int aXY = (int) Math.pow(2,8-depth);
    a = new Point(aXY,aXY);

    direction = 90; // Start-Richtung in Grad

    turtleGraphic( "X", depth);

    // Flush remaining data
    sb = pc.flushRemainingData(sb);

    pc.sendEnd();

    pc.readNDisplay(true);

    pc.penUp();
    pc.disconnect();

    System.out.println("Sent " + tokenCount + " tokens");

  }


  private void turtleGraphic(String instruction, int depth) throws IOException, InterruptedException {

    if (depth==0) {
      return;
    }
    depth -= 1;

    Point aMark = new Point(0,0);
    double directionMark = 0;
    // Dummy-Werte

    int i;
    char c;

    for (i=0;i<instruction.length();i++) {

      c = instruction.charAt(i);

      // Produktionsregeln iterieren, solange Tiefe nicht erreicht ist
      if (c=='X') {
        turtleGraphic("-YF+XFX+FY-", depth);
      } else if (c=='Y') {
        turtleGraphic("+XF-YFY-FX+", depth);
      }

      // Schritt Vorwärts
      else if (c=='F') {

        double rad = 2*Math.PI/360 * direction; // Grad -> Radiant

        int p = (int) (lengthF * Math.cos(rad));
        int q = (int) (lengthF * Math.sin(rad));

        b = new Point(a.x+p, a.y+q);


//        gBuffer.drawLine(a.x, a.y, b.x ,b.y);
        int dx = b.x - a.x;
        int dy = b.y - a.y;
        String s = "X" + dx + " Y" + dy;
             sb.append(s);
             tokenCount++;

        sb = pc.flushToPlotterIfNeeded(sb);


        a = b; // Neuer Startpunkt
      }

      // Drehung links herum
      else if (c=='+') {
        direction += rotation;
      }

        // Drehung rechts herum
      else if (c=='-') {
        direction -= rotation;
      }

        // Position und Richtung speichern
      else if (c != '[') {
        if (c==']') {
          a = aMark;
          direction = directionMark;
        }
      }

      // Zurück zu gespeicherter Position und Richtung
      else {
        aMark = a;
        directionMark = direction;
      }

    }
  }

  static class Point {

    final int x;
    final int y;

    public Point(int x, int y) {
      this.x = x;
      this.y = y;
    }
  }
}
