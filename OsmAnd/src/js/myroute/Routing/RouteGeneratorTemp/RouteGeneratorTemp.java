package js.myroute.Routing.RouteGeneratorTemp;


import org.openstreetmap.gui.jmapviewer.mCoordinate;
import org.osmdroid.util.GeoPoint;

import js.myroute.Routing.Logic.Edge;
import js.myroute.Routing.Logic.MyRoad;
import js.myroute.Routing.Logic.Vertex;
import js.myroute.Routing.RouteGeneratorTemp.InterfaceToRouteGen.DefaultRouteCleaner;
import js.myroute.Routing.RouteGeneratorTemp.InterfaceToRouteGen.RouteCleaner;
import js.myroute.Routing.RouteGeneratorTemp.InterfaceToRouteGen.RouteCriteria;
import js.myroute.Routing.RouteGeneratorTemp.RouteGeneratationFailedException.FAILURE_CAUSE;
import js.myroute.ServerCommunicaton.ServerCommunicator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * interface to the user
 */
public class RouteGeneratorTemp {
    public final ServerCommunicator serverCommunicator;
    private final String TAG = "RouteGenerator";

    public RouteGeneratorTemp(ServerCommunicator serverCommunicator) {
        this.serverCommunicator = serverCommunicator;
    }

    /**
     * generates a directional route, interface to the user
     *
     * @param start         the starting point
     * @param end           the end point
     * @param length        the target length of the route
     * @param routeCriteria the route criteria, which determines the best route and how many routes
     *                      should be calculated or null if the default route criteria should be
     *                      used
     * @param routeCleaner  the object which holds the function for cleanup or null if the default
     *                      route cleaner should be used
     * @return the route generated, or null if no route was found
     * @throws RouteGeneratationFailedException
     */
    public MyRoad generateRoute(Vertex start, Vertex end, int length, RouteCriteria routeCriteria, RouteCleaner routeCleaner) throws RouteGeneratationFailedException {
        //use defaults for criteria and cleaner if they're null
        //if (routeCriteria == null)
        //    routeCriteria = new DefaultRouteCriteria();
        if (routeCleaner == null)
            routeCleaner = new DefaultRouteCleaner();

        //looking for path of specified length
        LinkedList<mCoordinate> cp = serverCommunicator.getRoutes(start, end, length);

        if(cp!=null) {
            ArrayList<GeoPoint> route = new ArrayList<GeoPoint>();
            for (int i = 0; i < cp.size(); i++) {
                //System.out.println(cp.get(i));
                route.add(new GeoPoint(cp.get(i).getLat(), cp.get(i).getLon()));
            }
            return new MyRoad(route);
        }
        else{
            return null;
        }
    }

    /**
     * cleans up a route according to the function provided
     *
     * @param edges        the edges of the route
     * @param routeCleaner the object containing cleaner function
     * @return the cleaned route
     */
    private MyRoad cleanUp(List<Edge> edges, RouteCleaner routeCleaner) throws RouteGeneratationFailedException {
        List<Vertex> route = edgesToVertices(edges);
        return new MyRoad(routeCleaner.cleanUp(route));
    }


    /**
     * converts a list of edges to a corresponding list of geopoints in the right order
     *
     * @param edges the list to be converted
     * @return the resulting list of vertices
     * @throws RouteGeneratationFailedException
     */
    private List<Vertex> edgesToVertices(List<Edge> edges) throws RouteGeneratationFailedException {
        List<Vertex> ret = new ArrayList<>();
        if (edges.size() == 0) {
            return ret;
        }
        if (edges.size() == 1) {
            Edge e = edges.get(0);
            ret.add(e.getSource());
            ret.add(e.getTarget());
            return ret;
        }

        Edge cur;
        Edge next = edges.get(0);
        Vertex shared;
        for (int i = 0; i < edges.size() - 1; i++) {
            cur = next;
            next = edges.get(i + 1);
            shared = cur.sharedVertex(next);
            if (shared == null) {
                throw new RouteGeneratationFailedException(FAILURE_CAUSE.GRAPH_ERROR, "disconnected path");
            }
            ret.add(cur.getOtherVertex(shared));
        }


        //add remaining two vertices
        Edge secondToLast = edges.get(edges.size() - 2);
        Edge last = edges.get(edges.size() - 1);
        shared = last.sharedVertex(secondToLast);
        if (shared == null) {
            throw new RouteGeneratationFailedException(FAILURE_CAUSE.GRAPH_ERROR, "disconnected path");
        }
        ret.add(shared);
        ret.add(last.getOtherVertex(shared));

        if (ret.size() != edges.size() + 1)
            throw new RouteGeneratationFailedException(FAILURE_CAUSE.GRAPH_ERROR, "wrong number of vertices: " + ret.size() + ", " + edges.size());

        return ret;
    }

}
