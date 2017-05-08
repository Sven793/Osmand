package js.myroute.Routing.RouteGeneratorTemp.InterfaceToRouteGen;


import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import js.myroute.Routing.Logic.Vertex;

/* //@linggi all
 * holds a function how a route should be cleaned (e.g. remove loops)
 */
public interface RouteCleaner {
    ArrayList<GeoPoint> cleanUp(List<Vertex> route);
}
