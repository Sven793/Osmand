package sd.smartroute.Routing.RouteGeneratorTemp.InterfaceToRouteGen;


import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import sd.smartroute.Routing.Logic.Vertex;

/* //@linggi all
 * the default route cleaner, which doesn't clean the route
 */
public class DefaultRouteCleaner implements RouteCleaner {
    /**
     * just returns the given route whitout cleaning (done by the server)
     * @param route the route provided
     * @return  the same route as provided
     */
    @Override
    public ArrayList<GeoPoint> cleanUp(List<Vertex> route) {
        ArrayList<GeoPoint> ret = new ArrayList<>();
        for (Vertex v : route)
            ret.add(v);
        return ret;
    }
}
