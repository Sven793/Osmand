package sd.smartroute.Routing.Logic;

//License: GPL. Copyright 2009 by Stefan Zeller

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/* //@linggi all
 * This class encapsulates a Point2D.Double and provide access
 * via <tt>lat</tt> and <tt>lon</tt>.
 *
 * @author Jan Peter Stotz adapted by me
 *
 */
public class Coordinate implements Serializable {
    private double lat;
    private double lon;

    public Coordinate(double latIn, double lonIn) {
        lat = latIn;
        lon = lonIn;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double latIn) {
        lat = latIn;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lonIn) {
        lon = lonIn;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(lon);
        out.writeObject(lat);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        lon = (Double) in.readObject();
        lat = (Double) in.readObject();
    }

    public String toString() {
        return "Coordinate[" + lat + ", " + lon + "]";
    }
}
