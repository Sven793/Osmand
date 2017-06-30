package sd.smartroute.Routing.RouteGeneratorTemp.InterfaceToRouteGen;


import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import sd.smartroute.Routing.Logic.Vertex;

/* //@linggi all
 * holds a function how a route should be cleaned (e.g. remove loops)
 */
public interface RouteCleaner {
    ArrayList<GeoPoint> cleanUp(List<Vertex> route);
}
