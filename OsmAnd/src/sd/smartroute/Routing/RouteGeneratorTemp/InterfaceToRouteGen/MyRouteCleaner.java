package sd.smartroute.Routing.RouteGeneratorTemp.InterfaceToRouteGen;

import sd.smartroute.Routing.Logic.Vertex;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/* //@linggi all
 * the custom route cleaner
 */
public class MyRouteCleaner implements RouteCleaner {
    private final String TAG = "MyRouteCleaner";
    @Override public ArrayList<GeoPoint> cleanUp(List<Vertex> route) {
        ArrayList<GeoPoint> ret = new ArrayList<>();
        for (Vertex v : route)
            ret.add(v);
        return ret;
    }
}
