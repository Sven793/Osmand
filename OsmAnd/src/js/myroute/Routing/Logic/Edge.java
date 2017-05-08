package js.myroute.Routing.Logic;


import js.myroute.Utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* //@linggi all
 * logic of edges for the graph
 */
public class Edge {
    private Vertex source;
    private Vertex target;
    private final String TAG = "Edge";

    public Edge(Vertex source, Vertex target) {
        this.source = source;
        this.target = target;
    }

    public Vertex getSource() {
        return source;
    }

    public void setSource(Vertex source) {
        this.source = source;
    }

    public Vertex getTarget() {
        return target;
    }

    public void setTarget(Vertex target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "Source: " + source + ", Target: " + target;
    }

    public @Nullable Vertex getOtherVertex(@NotNull Vertex v) {
        /**
         * get the vertex of this edge which isn't v
         * @return other vertex or null if v isn't part of this edge
         */
        if (this.getSource().equals(v))
            return this.getTarget();
        if (this.getTarget().equals(v))
            return this.getSource();
        return null;
    }

    public double getLength() {
        return source.distanceToDouble(target);
    }

    /**
     * determines the shared Vertex between this and another edge
     *
     * @param other the other edge
     * @return the shared Vertex or null if no such MyLocation exists
     */
    public Vertex sharedVertex(Edge other) {
        if (this.getSource().equals(other.getSource()) || this.getSource().equals(other.getTarget()))
            return this.getSource();

        if (this.getTarget().equals(other.getSource()) || this.getTarget().equals(other.getTarget()))
            return this.getTarget();
        return null;
    }


    /**
     * @return the angle in the range [0,2*PI] (clockwise)
     */
    public double getAngle(@NotNull Edge other) {
        double angle = Utils.getAngle(this, other);    //returns angle in [0,PI]
        if (angle == 0 || angle == Math.PI)
            return angle;

        //Log.d(TAG, "angle: " + angle);

        //now, we need to determine if the point is above or below the line determined by this
        Vertex shared = Utils.sharedVertex(this, other);
        if (shared == null)
            throw new AssertionError(); //TODO
        Vertex otherVertexThis = this.getSource().equals(shared) ? this.getTarget() : this.getSource();
        Vertex otherVertexOther = other.getSource().equals(shared) ? other.getTarget() : other.getSource();

        double x1 = otherVertexThis.getLongitude();
        double y1 = otherVertexThis.getLatitude();

        double x2 = shared.getLongitude();
        double y2 = shared.getLatitude();

        double vx = otherVertexOther.getLongitude();
        double vy = otherVertexOther.getLatitude();

        double crossProduct = (x2 - x1) * (y2 - vy) - (y2 - y1) * (x2 - vx);

        if (crossProduct > 0)   //left side
            return 2 * Math.PI - angle;
        else if (crossProduct < 0)  //right side
            return angle;
        else    //on the same line
            return angle;
    }
}
