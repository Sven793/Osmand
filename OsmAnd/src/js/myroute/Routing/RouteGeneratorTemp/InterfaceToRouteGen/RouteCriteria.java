package js.myroute.Routing.RouteGeneratorTemp.InterfaceToRouteGen;

import js.myroute.Routing.Logic.MyRoad;

/* //@linggi all
 * interface for the route criteria, used in route generation
 */
public abstract class RouteCriteria {
    private int numberOfRoutes;

    /**
     * creates a new route criteria
     * @param numberOfRoutes the number of routes which will be requested in the route generation has to be at least 1
     */
    RouteCriteria(int numberOfRoutes) {
        if (numberOfRoutes < 1)
            this.numberOfRoutes = 1;
        this.numberOfRoutes = numberOfRoutes;
    }

    /**
     * calculates the value of a route -1 means its very bad, 0 means its neutral, 1 means its perfex
     * @param route the route whose value is determined
     * @return  a value assigned to the route in the interval  [-1,1]
     */
    public abstract double getRouteValue(MyRoad route);

    public int getNumberOfRoutes() {
        return numberOfRoutes;
    }

}
