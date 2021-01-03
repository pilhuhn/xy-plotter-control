
package de.bsd.plottercontrol;

import gnu.io.NRSerialPort;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author hrupp
 */
public class PlotterComm {

  private static final long DEFAULT_STEPS_PER_MM = 160; // See plotter firmware
  private OutputStream outputStream;
  private InputStream inputStream;

  private NRSerialPort serial;
  private boolean started = false;

  PlotterComm() {

    String port = getPort();
    start(port);
  }

  PlotterComm(String port) {
    start(port);
  }


  private String getPort() {
    // Next code is on macOS. Will be different on Linux/Windows
    File file = new File("/dev");
    if (!file.isDirectory()) {
      throw new IllegalStateException("/dev is not a directory");
    }
    String[] files = file.list(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.startsWith("cu.usbmodem");
      }
    });
    if (files.length!=1) {
      throw new IllegalStateException("Can't determine the correct port");
    }
    String port = "/dev/" + files[0];
    return port;
  }


  private void start(String port)  {

    int baudRate = 115200;

    serial = new NRSerialPort(port, baudRate);
    serial.connect();
    System.out.println("Is serial connected: " + serial.isConnected());

    if (!serial.isConnected()) {
      throw new RuntimeException("Can't talk to plotter");
    }

    outputStream = serial.getOutputStream();
    inputStream = serial.getInputStream();
    try {
      int count = inputStream.available();
      if (count>0) {
        System.out.println("Got reply " + readLineFromStream(inputStream));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    started = true;
  }

  void penDown() throws IOException {
    if (!started) {
      send("d\n");
    } else {
      send("D\n");
    }
    try {
      readNDisplay(true);
    } catch (InterruptedException e) {
      e.printStackTrace();  // TODO: Customise this generated block
    }
  }

  void penUp() throws IOException {

    try {
      if (!started) {
        send("u\n");
      } else {
        send("U\n");
      }
      readNDisplay(true);
      Thread.sleep(200);
    } catch (Exception e) {
      e.printStackTrace();  // TODO: Customise this generated block
    }
  }

  void send(String string, String prefix) throws IOException {
    if (string.isEmpty()) {
      throw new RuntimeException("Trying to send empty string");
    }

    if (prefix.isEmpty()) {
      prefix = "||-> ";
    }

    if (string.endsWith("\n")) {
      System.out.print(prefix + string);
    } else {
      System.out.println(prefix + string);
    }
    System.out.flush();
    outputStream.write(string.getBytes());
    outputStream.flush();

  }

  void send(String string) throws IOException {
    send(string, "|--> ");
  }

  /**
   * Read input from plotter and show it.
   * @param searchOk If set to true, does not return until OK is found in the results,
   *                 which indicates that the plotter found the end of the output of
   *                 a command
   */
  void readNDisplay(boolean searchOk) throws IOException, InterruptedException {
    boolean okFound = !searchOk;
    Thread.sleep(100);
    do {
      while (inputStream.available() <= 0) {
        Thread.sleep(50);
      }
      while (inputStream.available() > 0) {
        String line = readLineFromStream(inputStream);
        line = line.trim();
        if (line.length()>0) {
          System.out.println("<-- " + line );
          System.out.flush();
        }
        if (line.startsWith("OK")) {
          okFound = true;
        }
      }
    } while (!okFound);
  }


  private String readLineFromStream(InputStream stream) throws IOException {
       StringBuilder sb = new StringBuilder(100);
       boolean crFound=false;
       while(!crFound) {
           int read = stream.read();
           crFound = (read == 10 || read == 13);
           if (read >= 0) {
               sb.append((char) read);
           }
       }
       return sb.toString();
   }

  public void disconnect() {

    serial.disconnect();
  }

  public void sendStart(boolean dryRun) throws IOException {
    if (dryRun) {
      send("Cdry\n");
    } else {
      send("C\n");
    }
  }

  public void sendEnd() throws IOException {
    send("-END\n");
  }

  void sendStartCommand(boolean dryRun) throws IOException, InterruptedException {
    sendStartCommand(dryRun,DEFAULT_STEPS_PER_MM);
  }

  public void sendStartCommand(boolean dryRun, long resStepsPerMM) throws IOException, InterruptedException {
    readNDisplay(true);
    send("r"+resStepsPerMM);
    readNDisplay(true);

    sendStart(dryRun);

    readNDisplay(true);

  }


  StringBuilder flushRemainingData(StringBuilder sb) throws IOException, InterruptedException {
    if (sb.length()>0) {
      if (sb.charAt(sb.length()-1) == '|') {
        sb.deleteCharAt(sb.length()-1);
      }
      send(sb.toString());
      sb = new StringBuilder();
      readNDisplay(true);
    }
    return sb;
  }

  StringBuilder flushToPlotterIfNeeded(StringBuilder buffer) throws IOException, InterruptedException {
    if (buffer.length()>80) {
      // we have enough data, so lets flush it
      buffer.append("\n");
      send(buffer.toString(), "||->> ");
      buffer = new StringBuilder();

      readNDisplay(true);
    } else {
      buffer.append('|');
    }
    return buffer;
  }

  public void flush() throws IOException, InterruptedException {
    send("\n");
    readNDisplay(true);
  }

  public void close() throws IOException {
    outputStream.flush();
    outputStream.close();
    inputStream.close();
    serial.disconnect();
  }

}
