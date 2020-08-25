
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
