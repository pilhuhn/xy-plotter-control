package de.bsd.plottercontrol;

import java.util.Locale;

/**
 * @author hrupp
 */
public class SinusTabelle {


  public static void main(String[] args) throws Exception {

//    for (int i = 0 ; i <= 90 ; i++) {
//      double rad = Math.toRadians(i);
////      System.out.printf(Locale.US, "%d -> %2.2f -> %s%n", i, 100*Math.sin(rad), Math.cos(rad));
//      System.out.printf(Locale.US, "  %2.0f, // %d %n",  10000*Math.sin(rad), i);
//    }

//    final int stepsPerMM = 160;
//    int radius = 10;
//    double ox = 0;
//    double oy = 0;
//    for (int i = 0; i < 10 ; i++) {
//      double rad = Math.toRadians(i);
//
//      double x = Math.cos(rad) * radius ;
//      double y = Math.sin(rad) * radius ;
//      System.out.printf(Locale.US, "%d  %f   %f ->  %f %f %n", i, x, y, (x-ox), (y-oy)
//                        );
//      ox = x;
//      oy = y;
//    }

    System.out.println(Math.sin(Math.toRadians(5)));
    System.out.println(Math.cos(Math.toRadians(5)));
    System.out.println("--");

    System.out.println(Math.sin(Math.toRadians(175)));
    System.out.println(Math.sin(Math.toRadians(90- (175 % 90))));
    System.out.println("--");
    System.out.println(Math.cos(Math.toRadians(175)));
    System.out.println(- Math.cos(Math.toRadians(90- (175 % 90))));
    System.out.println("----");

    System.out.println(Math.sin(Math.toRadians(185)));
    System.out.println(- Math.sin(Math.toRadians(185 % 90)));
    System.out.println("--");
    System.out.println(Math.cos(Math.toRadians(185)));
    System.out.println(- Math.cos(Math.toRadians(185 % 90)));
    System.out.println("----");

    System.out.println(Math.sin(Math.toRadians(355)));
    System.out.println(- Math.sin(Math.toRadians(90 - (355 % 90))));
    System.out.println("--");
    System.out.println(Math.cos(Math.toRadians(355)));
    System.out.println(Math.cos(Math.toRadians(90 - (355 % 90))));

  }
}
