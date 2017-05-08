package js.myroute.Config;


import android.location.LocationManager;

/* //@linggi, most of it created by linggi
 * config file for app which contains some useful information
 */
public class Config {
    public static final int EARTH_RADIUS = 6378137; //meters
    public static final String LOCATION_TAG = "LOCATION_TAG";
    public static final double CLOSE_VERTICES_TOLERANCE = 0.000001;  //to be considered the same (in degrees)
    public static final double REACHED_VERTEX_TOLERANCE = 25;  //tolerence under which the new vertex can be seen as reached (in meters)
    public static final String COMPLETION_ACTION = "COMPLETION_ACTION";
    public static final String COMPLETION_ROUTE_TAG = "COMPLETION_ROUTE_TAG";
    public static final int COLOR_REMAINING = 0xFF004DFF; //blue
    public static final int COLOR_NEXT = 0xFFFFA500; //orange
    public static final float PATH_WIDTH = 5;
    public static final String UPDATE_OVERLAY_ACTION = "UPDATE_OVERLAY_ACTION";
    public static final String UPDATE_OVERLAY_REMAINING_TAG = "UPDATE_OVERLAY_REMAINING_TAG";
    public static final String UPDATE_OVERLAY_COMPLETED_TAG = "UPDATE_OVERLAY_COMPLETED_TAG";
    public static final String UPDATE_REMAINING_LENGTH_ACTION = "UPDATE_REMAINING_LENGTH_ACTION";
    public static final String UPDATE_REMAINING_LENGTH_TAG = "UPDATE_REMAINING_LENGTH_TAG";
    public static final String USE_CURRENT_POSITION_ACTION = "USE_CURRENT_POSITION_ACTION";
    public static final String USE_CURRENT_POSITION_TAG = "USE_CURRENT_POSITION_TAG";
    public static final double ROUTE_SPLIT_SIZE = 25;   //ths size of the parts, the route gets split in
    public static final int FAR_AWAY_TOLERANCE = 80;   //tolerance above which a new route is calculated
    public static String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;
    public static final int VERTEX_LOOKAHEAD = 5;   //how many vertices the update handler looks ahead to determine if the user is closer to another vertex


    //public static double MAP_LAT_TAG = 47.3764545;
    //public static double MAP_LON_TAG = 8.5481666;

}
