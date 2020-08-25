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

import gnu.io.NRSerialPort;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author hrupp
 */
public class PlotterComm {

  private final OutputStream outputStream;
  private final InputStream inputStream;

  private final NRSerialPort serial;

  PlotterComm(String port) {

    int baudRate = 115200;

    serial = new NRSerialPort(port, baudRate);
    serial.connect();
    System.out.println("Serial connected: " + serial.isConnected());

    if (!serial.isConnected()) {
      throw new RuntimeException("Can't talk to plotter");
    }

    outputStream = serial.getOutputStream();
    inputStream = serial.getInputStream();

  }

  void penDown() throws IOException {
    send("d\n");
  }

  void penUp() throws IOException {

    try {
      send("u\n");
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

    System.out.println(prefix + string);
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
}
