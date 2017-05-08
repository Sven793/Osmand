package js.myroute.Routing.Logic;

import android.util.Log;

import js.myroute.Helpers.Tuple;
import org.jetbrains.annotations.NotNull;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/*
 * //@linggi, adapted a few lines
 */

public class MyRoad extends Road{
    public List<Vertex> route;
    private Tuple<Double, Double> altitudeProfile;
    private static final String TAG = "MyRoad";

    public MyRoad(ArrayList<GeoPoint> route){
        super(route);
        if (this.route == null)
            this.route = new LinkedList<>();
        for (GeoPoint g : route)
            this.route.add(new Vertex(g));
        init();
    }


    private void init() {
        this.mLength = calculateDistanceOfRoute(route); //own version probably more accurate
        altitudeProfile = calculateAltitudeProfileOfRoute(route);
    }


    public int getLength(){
        return (int) this.mLength;
    }

    public Tuple<Double, Double> getAltitudeProfile() {
        return altitudeProfile;
    }

    /**
     * @return the altitude total (sum of incline and decline in absolutes)
     */
    public double getAltitudeTotal() {
        return calculateAltitudeTotal(altitudeProfile.first, altitudeProfile.second);
    }


    /**
     * @return altitude difference between two GeoPoints in meters
     * (negative means g2 is lower than g1)
     */
    private double calculateAltitudeDifference(@NotNull GeoPoint g1, @NotNull GeoPoint g2) {

        return g2.getAltitude() - g1.getAltitude();
    }


    /**
     * @return the altitude total (sum of incline and decline in absolutes)
     */
    private double calculateAltitudeTotal(double up, double down) {
        return Math.abs(up) + Math.abs(down);
    }

    /**
     * @param p list of vertices in route
     * @return tuple which encodes altitude difference of a route in meters
     * first element indicates incline, second element indicates decline
     * both values of the tuple are non-negative
     */
    private @NotNull Tuple<Double, Double> calculateAltitudeProfileOfRoute(@NotNull List<Vertex> p) {
        Tuple<Double, Double> profile = new Tuple<>(0.0, 0.0);
        for (int i = 0; i < p.size() - 1; i++) {
            GeoPoint g1 = p.get(i);
            GeoPoint g2 = p.get(i + 1);
            double diff = calculateAltitudeDifference(g1, g2);
            if (diff > 0)
                profile.first += diff;
            else
                profile.second += -diff;
        }
        return profile;
    }

    /**
     * @return length of the route in meters
     */
    private double calculateDistanceOfRoute(@NotNull List<Vertex> p) {
        double d = 0;
        if (p.size() < 2) {
            Log.d(TAG, "Distance: List contains less than 2 points");
            return 0;
        }
        for (int i = 0; i < p.size() - 1; i++) {
            Vertex g1 = p.get(i);
            Vertex g2 = p.get(i + 1);
            if(!Double.isNaN(g1.distanceToDouble(g2)))
                d += g1.distanceToDouble(g2);
            //else
                //System.out.println("NaN: " + g1 + " " + g2);
        }
        return d;
    }

    public void setRoute(List<Vertex> route) {
        this.route = route;
    }
}
