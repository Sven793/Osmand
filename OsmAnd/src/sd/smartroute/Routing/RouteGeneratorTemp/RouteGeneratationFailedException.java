package sd.smartroute.Routing.RouteGeneratorTemp;

/* //@linggi all
 * a custom exception which needs to be caught by a user using the route generator
 */
public class RouteGeneratationFailedException extends Exception {

    /**
     * creates a new RouteGeneratationFailedException with a cause and a message
     * @param cause the cause provided
     * @param message the message provided
     */
    public RouteGeneratationFailedException(FAILURE_CAUSE cause, String message) {
        super(cause + ": " + message);
    }

    /**
     * creates a new RouteGeneratationFailedException with a only a cause
     * @param cause the cause provided
     */
    public RouteGeneratationFailedException(FAILURE_CAUSE cause) {
        super(cause.toString());
    }

    public enum FAILURE_CAUSE {
        TIMEOUT("TIMEOUT"), //for timeouts while routing
        ASSERTION_ERROR("ASSERTION_ERROR"), //for violation of assertions
        GRAPH_ERROR("GRAPH_ERROR"), //for errors in the graph, e.g missing vertices
        DATABASE_ERROR("DATABASE_ERROR");   //for errors in the db, e.g sql errors or missing entries

        private final String repr;    //the string representation of the enum

        FAILURE_CAUSE(String repr) {
            this.repr = repr;
        }

        @Override
        public String toString() {
            return repr;
        }
    }

}
