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

/**
 * @author http://www.tutego.de/blog/javainsel/2014/11/turtle-grafik/
 */
import java.awt.AWTEvent;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowEvent;

@SuppressWarnings( "serial" )
public class Turtle extends Frame
{
  private double angle = 0, x = 300, y = 300, oldX = 300, oldY = 300;
  private Graphics graphics;

  // Initialisiert Turtle-Grafik und bringt Frame auf den Schirm
  public Turtle()
  {
    setTitle( "Turtle-Grafil" );
    setSize( 600, 600 );
    enableEvents( AWTEvent.WINDOW_EVENT_MASK );
    setVisible( true );
  }

  public void setGraphics( Graphics graphics )
  {
    this.graphics = graphics;
  }

  // Methoden, die den Stift bewegen

  public void turnright( double degree )
  {
    if ( (angle += degree) > 360 )
      angle %= 360;
  }

  public void turnleft( double degree )
  {
    if ( (angle -= degree) < 0 )
      angle %= 360;
  }

  public void forwd( double step )
  {
    back( -step );
  }

  public void back( double step )
  {
    oldX = x;
    oldY = y;

    x -= step * Math.sin( Math.toRadians( angle ) );
    y += step * Math.cos( Math.toRadians( angle ) );

    graphics.drawLine( (int) x, (int) y, (int) oldX, (int) oldY );
  }

  // Events vom Schlie�en des Fensters abfangen

  @Override
  protected void processWindowEvent( WindowEvent e )
  {
    if ( e.getID() == WindowEvent.WINDOW_CLOSING )
      System.exit( 0 );

    super.processWindowEvent( e );
  }
}