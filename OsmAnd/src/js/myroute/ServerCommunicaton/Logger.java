package js.myroute.ServerCommunicaton;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import js.myroute.Config.ServerConfig;
import js.myroute.Routing.Logic.Vertex;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/* //@linggi all
 * interface for logging
 */
public class Logger {
    private static final String TAG = "Logger";

    /**
     * send buffered locations to server, i.e. event id 0
     *
     * @param sessionId the current session id
     * @param locations the buffered locations
     */
    public static void logLocations(@NotNull String sessionId, @NotNull List<Vertex> locations) {
        logLocations(sessionId, 0, locations, -1);
    }

    /**
     * send buffered locations to server with arbitrary event
     *
     * @param sessionId the current session id
     * @param locations the buffered locations
     * @param length    the length of the route or -1 if not a route event
     */
    public static void logLocations(@NotNull String sessionId, int event, @NotNull List<Vertex> locations, int length) {
        try {
            JSONArray lats = new JSONArray();
            JSONArray lons = new JSONArray();

            for (Vertex v : locations) {
                if (v == null)
                    continue;
                lats.put(v.getLatitude());
                lons.put(v.getLongitude());
            }

            JSONObject out = new JSONObject();
            out.put(LogConfig.EVENT, event);
            out.put(LogConfig.SESSION_ID, sessionId);
            out.put(LogConfig.LATS, lats);
            out.put(LogConfig.LONS, lons);
            if (length != -1)
                out.put(LogConfig.LENGTH, length);
            sendJSON(out);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * send an event log to the server with two vertices and an eventlbc4
     *
     * @param sessionId the current session id
     * @param event     the event id as defined in the logger config
     */
    public static void logStartEnd(@NotNull String sessionId, int event, Vertex start, Vertex end, int length) {
        List<Vertex> list = new LinkedList<>();
        list.add(start);
        list.add(end);
        logLocations(sessionId, event, list, length);
    }

    /**
     * send an event log to the server
     *
     * @param sessionId the current session id
     * @param event     the event id as defined in the logger config
     */
    public static void logEvent(@NotNull String sessionId, int event) {
        try {
            JSONObject out = new JSONObject();
            out.put(LogConfig.EVENT, event);
            out.put(LogConfig.SESSION_ID, sessionId);
            sendJSON(out);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * send an event log to the server when multiple routes where requested and the used index is logged
     *
     * @param sessionId the current session id
     */
    public static void logIndex(@NotNull String sessionId, int index) {
        try {
            JSONObject out = new JSONObject();
            out.put(LogConfig.ROUTE_INDEX, index);
            out.put(LogConfig.EVENT, 9);
            out.put(LogConfig.SESSION_ID, sessionId);
            sendJSON(out);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * sends the provided json to the server
     */
    private static void sendJSON(final JSONObject out) {
        try {
            if (!out.has(LogConfig.TIMESTAMP))
                out.put(LogConfig.TIMESTAMP, getCurrentTimeStamp());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        class LogTask extends AsyncTask<Void, Void, Void> {

            @Override protected Void doInBackground(Void... params) {
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    String path = ServerConfig.SERVER_BASE_ADRESS + ServerConfig.HTTP_LOG_SUB;
                    HttpPost httpost = new HttpPost(path);

                    StringEntity se = new StringEntity(out.toString());
                    httpost.setEntity(se);
                    httpost.setHeader("Accept", "application/json");
                    httpost.setHeader("Content-type", "application/json");

                    //Handles what is returned from the page
                    HttpResponse response = httpclient.execute(httpost);
                    //Log.d(TAG, "logging done");

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;

            }
        }

        LogTask myLogTask = new LogTask();
        myLogTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");//dd/MM/yyyy
        Date now = new Date();
        return sdfDate.format(now);
    }
}
