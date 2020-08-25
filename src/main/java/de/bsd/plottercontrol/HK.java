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
import java.awt.*;

final class HK extends Turtle
{
  public static void main( String args[] )
  {
    new HK();
  }

  void hilbert( double len, int deep, int direction )
  {
    if ( deep > 0 ) {
      turnleft( direction * 90 );
      hilbert( len, deep - 1, -direction );

      forwd( len );
      turnright( direction * 90 );
      hilbert( len, deep - 1, direction );

      forwd( len );
      hilbert( len, deep - 1, direction );

      turnright( direction * 90 );
      forwd( len );
      hilbert( len, deep - 1, -direction );

      turnleft( direction * 90 );
    }
  }

  public void paint( Graphics g )
  {
    setGraphics( g );
    hilbert( 50, 2, 1 );
  }
}
