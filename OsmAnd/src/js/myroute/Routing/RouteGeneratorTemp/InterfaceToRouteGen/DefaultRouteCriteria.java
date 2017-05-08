package js.myroute.Routing.RouteGeneratorTemp.InterfaceToRouteGen;

import js.myroute.Routing.Logic.MyRoad;

/* //@linggi all
 * default route criteria
 */
public class DefaultRouteCriteria extends RouteCriteria {
    /**
     * creates a new route criteria with 1 requested route
     */
    public DefaultRouteCriteria(){
        super(1);
    }

    /**
     * all the routes are equally good
     * @param route the route whose value is determined
     * @return 0
     */
    @Override
    public double getRouteValue(MyRoad route) {
        return 0;
    }
}
