package de.bsd.plottercontrol;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static de.bsd.plottercontrol.PenState.DOWN;
import static de.bsd.plottercontrol.PenState.UP;

/**
 * @author hrupp
 */
public class PlotSVG {

    private final PlotterComm pc;
    private boolean isDryRun = false;
    private PenState penState;

    private long minX = Long.MAX_VALUE;
    private long maxX = Long.MIN_VALUE;
    private long minY = Long.MAX_VALUE;
    private long maxY = Long.MIN_VALUE;

    double globalHeight = 0;
    double globalWidth = 0;

    long xSteps = 0;
    long ySteps = 0;

    BigDecimal transform;

    BigDecimal PLOT_X_WIDTH = BigDecimal.valueOf(200); // mm
    BigDecimal PLOT_Y_WIDTH = BigDecimal.valueOf(200); // mm
    // The plotter can do 160 steps per mm
    BigDecimal PLOTTER_STEPS_PER_MM = BigDecimal.valueOf(160); //Default for plotter
    // For each px we send , it does 20 steps = 1/8mm
    static BigDecimal STEPS_PER_MM = BigDecimal.valueOf(10);
    BigDecimal FACTOR = PLOTTER_STEPS_PER_MM.divide(STEPS_PER_MM,RoundingMode.HALF_DOWN);

    MathContext mc = MathContext.DECIMAL64;

    public PlotSVG(PlotterComm pc) {
        this.pc = pc;
    }

    public static void main(String[] argv) throws Exception {

        PlotterComm pc = new PlotterComm();

        PlotSVG plotSVG = new PlotSVG(pc);
//        plotSVG.run("/Users/hrupp/Desktop/bla.svg");
//        plotSVG.run("/Users/hrupp/Desktop/bla3.svg");
        plotSVG.run("/Users/hrupp/downloads/plotter.svg");
        pc.close();
    }

    private BigDecimal currX = BigDecimal.ZERO;
    private BigDecimal currY = BigDecimal.ZERO;

    private void run(String path) throws IOException, XMLStreamException, InterruptedException {


        InputStream is = new FileInputStream(path);
        doTheWork(is, true);
        System.out.println("Height: " + globalHeight);
        System.out.println("Width : " + globalWidth);
        System.out.println("minX  : " + minX + ", maxX = " + maxX);
        System.out.println("minY  : " + minY + ", maxY = " + maxY);

        BigDecimal xTransform = PLOT_X_WIDTH.divide(BigDecimal.valueOf(globalWidth),mc);
        BigDecimal yTransform = PLOT_Y_WIDTH.divide(BigDecimal.valueOf(globalHeight),mc);

        transform = xTransform.min(yTransform);
        transform = transform.multiply(FACTOR);
        transform = transform.round(mc);

        System.out.println("Transform factor : " + transform  );
        is.close();

        // Now we can plot with scaling to fit the paper
        pc.sendStartCommand(isDryRun,STEPS_PER_MM.longValue());
        forcePenUp();

        is = new FileInputStream(path);
        doTheWork(is, false);

        forcePenUp();
        // return to where we started.
        pc.send("X" + -xSteps + " Y" + -ySteps + "\n");
        pc.sendEnd();

        System.out.println("Step difference x:" + xSteps + ", y: " + ySteps );
    }

    private void doTheWork(InputStream is, boolean getDimensions) throws XMLStreamException, IOException, InterruptedException {
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
        while (reader.hasNext()) {
            int event = reader.next();
            switch (event) {
                case XMLEvent.START_ELEMENT:

                    String localName = reader.getLocalName();
                    switch (localName) {
                        case "svg":
                            if (getDimensions) {
                                globalHeight = getPxValue(reader,"height");
                                globalWidth =  getPxValue(reader,"width");
                                // TODO get viewBox to later shift origin
                                // The following commented out code reads from the header,
                                // which may be much too large.
//                                minX = 0;
//                                minY = 0;
//                                if (getAttributePos(reader,"height")!=-1) {
//                                    return; // we are done here
//                                }
                            }
                            break;
                        case "rect":
                            // TODO
                            // <rect x="1" y="1" width="398" height="398"
                            //        fill="none" stroke="blue" />

                            break;
                        case "line":
                            // TODO
                            break;
                        case "polygon":
                            // https://www.w3.org/TR/SVGTiny12/shapes.html#PolygonElement
                            String points = getStringValue(reader,"points");
                            doPolygon(points, getDimensions);
                            break;
                        case "path":
                            String d = getStringValue(reader,"d");
                            doPath(d, getDimensions);
                            break;
                        default:
                            System.out.println(localName);
                            if (reader.getAttributeCount() > 0) {
                                for (int i = 0; i < reader.getAttributeCount(); i++) {
                                    System.out.println("   " + reader.getAttributeLocalName(i) + " --> " + reader.getAttributeValue(i));
                                }
                        }
                    }
                    break;
                case XMLEvent.CHARACTERS:
                case XMLEvent.COMMENT:
                    // We do nothing
                    break;
                case XMLEvent.END_ELEMENT:
                    // Nothing yet
                    break;
                case XMLEvent.END_DOCUMENT:
                    // Nothing yet
                    break;
                default:
                    System.out.println("Type " + event + " not known for " + reader.getName());
            }
        }
        if (getDimensions) {
            globalHeight = maxX - minX;
            globalWidth = maxY - minY;
        }

    }

    private void doPath(String d, boolean getDimensions) throws IOException, InterruptedException {
        // Path is tricky elements are split by spaces,
        // but there is no need to repeat L or M letters
        // and there may be a space between L/M and the coordinates
        String[] parts = d.split(" ");
        double x = 0.0;
        double y = 0.0;

        int pos = 0 ;
        while (pos < parts.length) {
            String item = parts[pos];
            OP op;
            if (item.length()==0) {
                ; // nothing
            }
            else if (item.length()==1) { // Single M or L
                op = getOp(item);
                pos++;
                String coord = parts[pos];
                String[] coords = coord.split(",");
                x = Double.parseDouble(coords[0]);
                y = Double.parseDouble(coords[1]);
                operate(op,x,y, getDimensions);
            }
            else {
                op = getOp(item.substring(0,1));
                String coord = item.substring(1);
                String[] coords = coord.split(",");
                x = Double.parseDouble(coords[0]);
                y = Double.parseDouble(coords[1]);
                operate(op,x,y, getDimensions);
            }
            pos++;
        }
        penUp();
    }

    private void operate(OP op, double xIn, double yIn, boolean getDimensions) throws IOException, InterruptedException {
        System.out.println(op + " : " + xIn + ", " + yIn);
        System.out.flush();

        BigDecimal dx = BigDecimal.ZERO;
        BigDecimal dy = BigDecimal.ZERO;
        if (getDimensions) {
            long x = (long) xIn;
            long y = (long) yIn;

            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        } else {
            // We do the work

            // Translate into the plotters view
            dx = BigDecimal.valueOf(xIn);
            dx = dx.subtract(BigDecimal.valueOf(minX));
            dy = BigDecimal.valueOf(yIn);
            dy = dy.subtract(BigDecimal.valueOf(minY));
            dx = dx.multiply(transform);
            dy = dy.multiply(transform);

            System.out.println("   +- curr  -> " + currX + ", " + currY);
            System.out.println("   +- new p -> " + dx + ", " + dy);

            long ddx = dx.subtract(currX).setScale(0, RoundingMode.HALF_DOWN).longValue();
            long ddy = dy.subtract(currY).setScale(0, RoundingMode.HALF_DOWN).longValue();

            // Only do something when we are moving position
            if (ddx != 0 || ddy != 0) {
                if (op == OP.MOVE_ABS) {
                    penUp();
                } else {
                    penDown();
                }

                pc.send("X" + ddx + " Y" + ddy + " ");
                xSteps += ddx;
                ySteps += ddy;
                pc.flush();
            }
        }

        currX = dx;
        currY = dy;

    }

    private OP getOp(String item) {
        OP op;
        switch (item) {
            case "M": op = OP.MOVE_ABS; break;
            case "L": op = OP.LINE_ABS; break;
            case "m": op = OP.MOVE_REL; break;
            case "l": op = OP.LINE_REL; break;
            default:
                throw new IllegalStateException("Unknown operation: >" + item + "<");
        }
        return op;
    }

    private void doPolygon(String pointsString, boolean getDimensions) throws IOException, InterruptedException {
        // Move to the first point in the list
        penUp();
        String[] points = pointsString.split(" +");
        String[] firstPoint = points[0].split(",");
        operate(OP.MOVE_ABS, Double.parseDouble(firstPoint[0]), Double.parseDouble(firstPoint[1]), getDimensions);

        // Start drawing with a line to the 2nd point in the list
        penDown();
        for(int i = 1; i< points.length ; i++) {
            String[] aPoint = points[i].split(",");
            operate(OP.LINE_ABS,Double.parseDouble(aPoint[0]), Double.parseDouble(aPoint[1]), getDimensions);

        }
        // Close the path with a line to the 1st point
        operate(OP.LINE_ABS,Double.parseDouble(firstPoint[0]), Double.parseDouble(firstPoint[1]), getDimensions);
        penUp();
    }

    private void penDown() throws IOException {
        if (penState == UP) {
            pc.penDown();
            penState = DOWN;
        }
    }

    private void penUp() throws IOException {
        if (penState == DOWN) {
            pc.penUp();
            penState = UP;
        }
    }

    private void forcePenUp() throws IOException {
        pc.penUp();
        penState = UP;
    }

    private int getAttributePos(XMLStreamReader reader, String attributeName) {
        int num = reader.getAttributeCount();
        for (int i = 0; i < num ; i++) {
            if (reader.getAttributeLocalName(i).equals(attributeName)) {
                return i;
            }
        }
        return -1;
    }

    private String getStringValue(XMLStreamReader reader, String attributeName) {
        int num = getAttributePos(reader,attributeName);
        if (num!=-1) {
            String val = reader.getAttributeValue(num);
            return val;
        } else {
            throw new IllegalArgumentException("No such attribute: " + attributeName);
        }
    }

    private float getPxValue(XMLStreamReader reader, String attributeName) {
        String val = getStringValue(reader,attributeName);
        if (val.endsWith("px")) {
            val = val.substring(0,val.length()-2);
        }
        return Float.parseFloat(val);
    }

    private float getFloatValue(XMLStreamReader reader, String attributeName) {
        String val;
        try {
            val = getStringValue(reader, attributeName);
            return Float.parseFloat(val);
        }
        catch (IllegalArgumentException iae) {
            return 0.0f;
        }
    }

    private enum OP {
        MOVE_ABS (true),
        MOVE_REL (true),
        LINE_REL (false),
        LINE_ABS (false);

        private boolean isAbsoluteMovement;

        OP(boolean isAbsoluteMovement) {
            this.isAbsoluteMovement = isAbsoluteMovement;
        }

        public boolean isAbsoluteMovement() {
            return isAbsoluteMovement;
        }
    }
}
