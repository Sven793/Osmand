package sd.smartroute.Routing.Logic;


import sd.smartroute.Config.Config;
import org.jetbrains.annotations.NotNull;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import java.io.Serializable;

/* //@linggi all
 * logic for vertices
 */
public class Vertex extends GeoPoint implements Serializable {

    public Vertex(GeoPoint aGeopoint) {
        super(aGeopoint);
    }

    public Vertex(double aLatitude, double aLongitude) {
        super(aLatitude, aLongitude);
    }

    public Vertex(double aLatitude, double aLongitude, int aAltitude) {
        super(aLatitude, aLongitude, aAltitude);
    }

    private final String TAG = "Vertex";

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vertex))
            return false;
        Vertex v = (Vertex) obj;
        return v.getLatitudeE6() == this.getLatitudeE6() && v.getLongitudeE6() == this.getLongitudeE6();
    }

    @Override
    public String toString() {
        return this.getLatitude() + ", " + this.getLongitude();
    }

    /**
     * get center between two nodes
     */
    public @NotNull Vertex getCenter(@NotNull Vertex other) {
        double newLat = (this.getLatitude() + other.getLatitude()) / 2;
        double newLon = (this.getLongitude() + other.getLongitude()) / 2;
        return new Vertex(newLat, newLon);
    }

    public double distanceToDouble(IGeoPoint other) {
        Vertex otherVertex = new Vertex(other.getLatitude(), other.getLongitude());
        if (this.equals(otherVertex)) //bug in superclass if the same vertex is queried
            return 0;

        final double a1 = DEG2RAD * this.getLatitudeE6() / 1E6;
        final double a2 = DEG2RAD * this.getLongitudeE6() / 1E6;
        final double b1 = DEG2RAD * other.getLatitudeE6() / 1E6;
        final double b2 = DEG2RAD * other.getLongitudeE6() / 1E6;

        final double cosa1 = Math.cos(a1);
        final double cosb1 = Math.cos(b1);

        final double t1 = cosa1 * Math.cos(a2) * cosb1 * Math.cos(b2);

        final double t2 = cosa1 * Math.sin(a2) * cosb1 * Math.sin(b2);

        final double t3 = Math.sin(a1) * Math.sin(b1);

        final double tt = Math.acos(t1 + t2 + t3);
        return (RADIUS_EARTH_METERS * tt);
    }

    /**
     * returns a new vertex in the direction specified by the input vertex
     *
     * @param distance how far away the new vertex is from this
     */
    public @NotNull Vertex vertexInDirection(@NotNull Vertex direction, double distance) {
        double d = this.distanceToDouble(direction);
        if (d == 0)
            return this;
        double factor = distance / d;
        double dLat = factor * (direction.getLatitude() - this.getLatitude());
        double dLon = factor * (direction.getLongitude() - this.getLongitude());

        double newLat = this.getLatitude() + dLat;
        double newLon = this.getLongitude() + dLon;
        return new Vertex(newLat, newLon);
    }

    /**
     * returns is other is close enough with this to be considered the same vertex
     */
    public boolean almostTheSame(@NotNull Vertex other) {

        double dLat = this.getLatitude() - other.getLatitude();
        double dLon = this.getLongitude() - other.getLongitude();
        return dLat < Config.CLOSE_VERTICES_TOLERANCE && dLon < Config.CLOSE_VERTICES_TOLERANCE;
    }


    /**
     * returns if other is close to this
     */
    public boolean reached(@NotNull Vertex other) {
        return this.distanceToDouble(other) < Config.REACHED_VERTEX_TOLERANCE;
    }

}
