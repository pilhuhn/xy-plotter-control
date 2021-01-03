package de.bsd.plottercontrol;

import java.io.IOException;

/**
 * @author hrupp
 */
public class PatternTest {


  private static PlotterComm pc;

  public static void main(String[] args) throws Exception {

    pc = new PlotterComm();

    PatternTest pt = new PatternTest();
    pt.run();
    pc.disconnect();
  }

  private void run() throws IOException, InterruptedException {
    pc.readNDisplay(true);
//    boxes();
//    down20();
//    lines();
//    zickzack();
    circles();
  }


  private void down20() throws IOException, InterruptedException {
    System.err.println("down20");
    System.err.flush();
    pc.sendStart(false);
    pc.readNDisplay(true);
    pc.send("X20");
    pc.readNDisplay(true);
    pc.sendEnd();
    pc.readNDisplay(true);
  }

  private void boxes() throws IOException, InterruptedException {
    System.err.println("boxes");
    System.err.flush();

    StringBuilder sb = new StringBuilder();
    for (int i = 1 ; i < 5 ; i++) {
      sb.append("X").append(i*10).append("|");
      sb.append("Y").append(i*10).append("|");
      sb.append("X-").append(i*10).append("|");
      sb.append("Y-").append(i*10);
      if (i < 4) {
        sb.append("|");
      }
    }
    pc.penDown();
    pc.sendStartCommand(false);
    sb.append("\n");
    pc.send(sb.toString());
    pc.readNDisplay(true);
//    sb = pc.flushRemainingData(sb);
    pc.sendEnd();
    pc.readNDisplay(true);
    pc.penUp();
  }

  private void lines() throws IOException, InterruptedException {
    System.err.println("lines");
    System.err.flush();


    StringBuilder sb = new StringBuilder();
    int x = 0;
    for (int i = 0; i < 10 ; i++) {
      sb.append("D|");
      sb.append("Y50|");
      sb.append("U|");
      sb.append("X5 Y-50");
      sb.append("|");
    }
    sb.append("D|X-50|U\n");
    pc.sendStart(false);
    pc.readNDisplay(true);
    pc.send(sb.toString());
    pc.readNDisplay(true);
//    sb = pc.flushRemainingData(sb);
    pc.sendEnd();
    pc.readNDisplay(true);
  }

  private void bigbox() throws IOException, InterruptedException {
    System.err.println("bigbox");
    System.err.flush();


    StringBuilder sb = new StringBuilder();
    sb.append("D|");
    sb.append("Y200|");
    sb.append("X200|");
    sb.append("Y-200|");
    sb.append("X-200|");
    sb.append("U\n");
    pc.sendStart(false);
    pc.readNDisplay(true);
    pc.send(sb.toString());
    pc.readNDisplay(true);
//    sb = pc.flushRemainingData(sb);
    pc.sendEnd();
    pc.readNDisplay(true);
  }

  private void zickzack() throws IOException, InterruptedException {
    System.err.println("lines");
    System.err.flush();


    StringBuilder sb = new StringBuilder();
    int x = 0;
    for (int i = 0; i < 20 ; i++) {
      sb.append("D|");
      sb.append("X10 Y10|");
      sb.append("X10 Y-10|");
    }
    sb.append("X-200|");
    sb.append("U\n");
    pc.sendStart(false);
    pc.readNDisplay(true);
    pc.send(sb.toString());
    pc.readNDisplay(true);
//    sb = pc.flushRemainingData(sb);
    pc.sendEnd();
    pc.readNDisplay(true);
  }

  private void circles() throws IOException, InterruptedException {

    System.err.println("circles");
    System.err.flush();


    StringBuilder sb = new StringBuilder();
    sb.append("D|");
    for (int i = 2; i < 50; i+=5) {
      sb.append("a").append(i);
      sb.append("|");
    }
    sb.append("U\n");

    pc.sendStart(false);
    pc.send(sb.toString());
    pc.readNDisplay(true);
    pc.sendEnd();
    pc.readNDisplay(true);

  }
}
