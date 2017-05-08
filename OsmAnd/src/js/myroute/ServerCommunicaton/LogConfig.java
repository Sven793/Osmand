package js.myroute.ServerCommunicaton;

/* //@linggi all
 * config for log json
 */
public class LogConfig {
    public static final String SESSION_ID = "SESSION_ID";
    public static final String EVENT = "EVENT";
    public static final String LENGTH = "LENGTH";
    public static final String LATS = "LATS";
    public static final String LONS = "LONS";
    public static final String TIMESTAMP = "TIMESTAMP";
    public static final String ROUTE_INDEX = "ROUTE_INDEX";  //id of the route which was actually chosen (if more than one sent)

     /*
    0 -> locations update
    1 -> initial route
    2 -> less
    3 -> any
    4 -> more
    5 -> completion
    6 -> far away
    7 -> no new route
    8 -> new route
    9 -> route id
     */
}
