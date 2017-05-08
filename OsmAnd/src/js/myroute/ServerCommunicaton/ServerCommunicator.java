package js.myroute.ServerCommunicaton;

import android.util.Log;
import android.widget.Toast;

import net.osmand.plus.activities.MapActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openstreetmap.gui.jmapviewer.mCoordinate;

import java.io.IOException;
import java.util.LinkedList;

import js.myroute.Config.ServerConfig;
import js.myroute.Routing.Logic.Vertex;

/*
 * provides functionality to communicate with server
 */
public class ServerCommunicator {
    private final String TAG = "ServerCommunicator";
    private String sessionId;
    private MapActivity mainActivity;

    /* //@linggi
     * setup communication to server
     */
    public ServerCommunicator(){};


    public void setMainActivity(MapActivity mainActivity) {
        this.mainActivity = mainActivity;
    }


    /* structure by //@linggi, content by me
     * interface for routing requests
     */
    public @Nullable
    LinkedList<mCoordinate> getRoutes(@NotNull Vertex start, @NotNull Vertex end, int length) {
        long startTime = System.nanoTime();
        String routingType = "Angle";

        //check if valid db file is already available
        Log.d(TAG, "querying route with start: " + start + ", end: " + end);

        //make http get to server
        String resp_string;
        try {
            HttpParams httpParameters = new BasicHttpParams();
            // Set the timeout in milliseconds until a connection is established.
            // The default value is zero, that means the timeout is not used.
            int timeoutConnection = 3000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            // Set the default socket timeout (SO_TIMEOUT)
            // in milliseconds which is the timeout for waiting for data.
            int timeoutSocket = 20000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            HttpClient httpclient = new DefaultHttpClient(httpParameters);
            String path = ServerConfig.SERVER_BASE_ADRESS + ServerConfig.HTTP_GET_ROUTES_SUB;

            //setting Param
            String param = "startLat=" + start.getLatitude() + "&" + "startLon="+ start.getLongitude() +"&" + "endLat="+ end.getLatitude() +"&"
                    + "endLon="+ end.getLongitude() +"&" + "length="+length+"&" + "routingType="+routingType+"";
            System.out.println(path +"?"+ param);
            HttpGet httpget = new HttpGet(path +"?"+ param);

            HttpResponse response = httpclient.execute(httpget);
            resp_string = EntityUtils.toString(response.getEntity());
            //System.out.println(resp_string);

            long stopTime = System.nanoTime();

            Log.d(TAG, "elapsed time for server query: " + ((double) (stopTime - startTime)) / 1000000000);

        } catch (IOException e1) {
            e1.printStackTrace();
            showToast("Something went wrong, while querying the server");
            Log.e(TAG, "error in route request");
            return null;
        }

        LinkedList<mCoordinate> newRoute = strToList(resp_string);
        //System.out.println(newRoute);

        if(newRoute.size()==0)
            return null;
        return newRoute;
    }

    private LinkedList<mCoordinate> strToList(String str){
        LinkedList<mCoordinate> cList = new LinkedList<mCoordinate>();
        int i = 0;
        boolean finished = false;
        double lat = 0;
        double lon = 0;
        boolean latRead = false;

        String a = str.substring(i,i+2);
        if(a.equals("[]")){
            showToast("We are sorry, we don't have enough data for this area");
            finished = true;
        }
        a = str.substring(i,i+1);
        if(!a.equals("[")){
            //System.out.println(a);
            showToast("We are sorry, we don't have enough data for this area");
            finished = true;
        }
        while(!finished){
            //System.out.println(i + " / " + str.length());
            int u = i;
            a = str.substring(i,i+2);
            if(a.equals("]]")){
                finished = true;
            }else{
                a = str.substring(i,i+1);
                if(!(a.toString().equals("0")||a.toString().equals("1")||a.toString().equals("2")||a.toString().equals("3")||a.toString().equals("4")||a.toString().equals("5")||a.toString().equals("6")||a.toString().equals("7")||a.toString().equals("8")||a.toString().equals("9"))){
                    i++;
                }else{
                    a = str.substring(u,u+1);
                    while(a.equals(".")||a.equals("0")||a.equals("1")||a.equals("2")||a.equals("3")||a.equals("4")||a.equals("5")||a.equals("6")||a.equals("7")||a.equals("8")||a.equals("9")){
                        u++;
                        a = str.substring(u,u+1);
                    }
                    a = str.substring(i,u);
                    if(!latRead){
                        lat = Double.parseDouble(a);
                        latRead = true;
                    }
                    else{
                        lon = Double.parseDouble(a);
                        latRead = false;
                        cList.add(new mCoordinate(lat,lon));
                    }
                    i = u;
                }
            }
        }
        return cList;
    }

    private void showToast(final String toast)
    {
        if (mainActivity != null) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mainActivity, toast, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
