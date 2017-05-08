package js.myroute.Utils;

import android.util.Log;

import js.myroute.Config.Config;
import js.myroute.Routing.Logic.Edge;
import js.myroute.Routing.Logic.Vertex;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/* //@linggi
 * some misc functions
 */
public class Utils {
    private static final String TAG = "Utils";

    /**
     * converts meters to degrees
     */
    public static double metersToDegrees(double meters) {
        //TODO make better
        double circ = Math.PI * Config.EARTH_RADIUS * 2;  //360 degrees
        return 360 * (meters / circ);
    }

    /**
     * converts degrees to meters
     */
    public static double degreesToMeters(double degrees) {
        Vertex g1 = new Vertex(0.0, 0.0);
        Vertex g2 = new Vertex(degrees, 0.0);
        return g1.distanceToDouble(g2);
    }

    /**
     * @return length of the route in meters
     */
    public static double getDistanceOfRoute(@NotNull List<GeoPoint> p) {
        double d = 0;
        if (p.size() < 2) {
            Log.d(TAG, "Distance: List contains less than 2 points");
            return 0;
        }
        for (int i = 0; i < p.size() - 1; i++) {
            GeoPoint g1 = p.get(i);
            GeoPoint g2 = p.get(i + 1);
            if (g1 instanceof Vertex && g2 instanceof Vertex){  //distanceToDouble is more accurate
                d += ((Vertex) g1).distanceToDouble(g2);
            }
            else {
                d += g1.distanceTo(g2);
            }
        }
        return d;
    }


    /**
     * converts a list of edges to a corresponding list of geopoints in the right order
     */
    public static @NotNull ArrayList<Vertex> edgesToVertices(@NotNull List<Edge> edges) {
        ArrayList<Vertex> ret = new ArrayList<>();
        if (edges.size() == 0) {
            return ret;
        }

        Edge first = edges.get(0);
        if (edges.size() == 1) {
            //no other option than to hope source is correct
            ret.add(first.getSource());
            ret.add(first.getTarget());
            return ret;
        }

        Edge second = edges.get(1);
        Vertex startVertex;
        //find common vertex of the two edges
        if (first.getSource().equals(second.getTarget()) || first.getSource().equals(second.getSource())) {
            startVertex = first.getTarget();
        } else if (first.getTarget().equals(second.getTarget()) || first.getTarget().equals(second.getSource())) {
            startVertex = first.getSource();
        } else {
            throw new AssertionError("disconnected graph in setup");
        }

        ret.add(startVertex);

        Vertex last;    //vertex last added to list
        if (startVertex.equals(first.getSource()))
            last = first.getTarget();
        else
            last = first.getSource();

        ret.add(last);

        edges.remove(0); //remove first edge because already added to list

        Vertex s;
        Vertex t;
        for (Edge e : edges) {
            s = e.getSource();
            t = e.getTarget();
            if (s.equals(last)) {
                //s already added as last
                ret.add(t);
                last = t;
            } else if (t.equals(last)) {
                //t already added as last
                ret.add(s);
                last = s;
            } else {
                throw new AssertionError("disconnected graph");
            }
        }
        return ret;
    }

    /**
     * @return vertex, the two edges share or null if they don't share one
     */
    public static @Nullable
    Vertex sharedVertex(@NotNull Edge e1, @NotNull Edge e2) {
        if (e1.getSource().equals(e2.getSource()) ||
                e1.getSource().equals(e2.getTarget()))
            return e1.getSource();

        if (e1.getTarget().equals(e2.getSource()) ||
                e1.getTarget().equals(e2.getTarget()))
            return e1.getTarget();
        return null;
    }

    /**
     * calculates the angle between the two edges or throws an assertion error if they don't share a
     * vertex
     *
     * @return the angle between the two edges in the range [0,PI]
     */
    public static double getAngle(@NotNull Edge e1, @NotNull Edge e2) {
        Vertex shared = sharedVertex(e1, e2);
        if (shared == null) throw new AssertionError();    //TODO

        double a = e1.getLength();
        double b = e2.getLength();

        Vertex other1 = e1.getSource().equals(shared) ? e1.getTarget() : e1.getSource();
        Vertex other2 = e2.getSource().equals(shared) ? e2.getTarget() : e2.getSource();

        double c = other1.distanceToDouble(other2);

        double angle = Math.acos(((a * a) + (b * b) - (c * c)) / (2 * a * b));    //law of cosines

        //Log.d(TAG, angle + ", " + a +", " + b + ", " + c);

        if (angle != 0)
            return angle;

        //angle == 0 is a special case, since it could be either 0 or 180 degrees
        double t = other1.distanceToDouble(other2);
        if (t == a + b)
            return Math.PI; //180 degrees
        return 0;
    }


    /**
     * @return the altitude total (sum of incline and decline in absolutes)
     */
    public static double getAltitudeTotal(double up, double down) {
        return Math.abs(up) + Math.abs(down);
    }

}
