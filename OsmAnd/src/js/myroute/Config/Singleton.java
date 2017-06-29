package js.myroute.Config;

import js.myroute.Routing.Logic.MyRoad;
import js.myroute.Routing.Logic.Vertex;
import js.myroute.Routing.RouteGeneratorTemp.RouteGeneratorTemp;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

/* //@linggi, created by linggi, main part by me, only few lines by linggi
 * save stuff for all activities to use
 */
public class Singleton {
    private static Singleton ourInstance = null;
    public static Singleton getInstance() {
        if (ourInstance == null)
            ourInstance = new Singleton();
        return ourInstance;
    }
    private Singleton() {
    }

    private RouteGeneratorTemp routeGenerator;
    public RouteGeneratorTemp getRouteGenerator() {
        return routeGenerator;
    }
    public void setRouteGenerator(@NotNull RouteGeneratorTemp routeGenerator) {
        this.routeGenerator = routeGenerator;
    }

    private int length = 2000;
    public int getLengthIn() {return length;}
    public void setLengthIn(int len){
        this.length = len;
    }

    private Vertex start;
    public Vertex getStartVertex() {
        return start;
    }
    public void setStartVertex(@NotNull Vertex start) {
        this.start = start;
    }

    private boolean startSet = false;
    public void setstartSet(){startSet = true;}
    public void removeStart(){startSet = false;}
    public boolean getstartSet(){return startSet;}

    private Vertex end;
    public Vertex getEndVertex() {
        return end;
    }
    public void setEndVertex(@NotNull Vertex end) {
        endSet = true;
        this.end = end;
    }

    private boolean endSet = false;
    public void setEndSet(){endSet = true;}
    public void removeEnd(){endSet = false;}
    public boolean getEndSet(){return endSet;}

    private boolean boolFollow = false;
    public void setFollow(){boolFollow=true;}
    public void removeFollow(){boolFollow=false;}
    public boolean getFollow(){return boolFollow;}
    private boolean followOrientation = false;
    public void setFollowOrientation(){followOrientation=true;}
    public void removeFollowOrientation(){followOrientation=false;}
    public boolean getFollowOrientation(){return followOrientation;}

    private Vertex currPos = new Vertex(47.3764545, 8.5481666); //ETH Coordinates
    public void setCurrPos(Vertex pos){currPos = pos;}
    public Vertex getCurrPos(){return currPos;}

    private boolean switchMultiWindow = false;
    public void setSwitchMultiWindow(boolean switchMultiWindow) {this.switchMultiWindow = switchMultiWindow;}
    public boolean getSwitchMultiWindow() {return switchMultiWindow;}

    private boolean setupComplete = false;
    public boolean isSetupComplete() {return setupComplete;}
    public void setSetupComplete(boolean setupComplete) {this.setupComplete = setupComplete;}

    private int environmentImportance = 1;
    public int getEnvironmentImportance() {
        return environmentImportance;
    }
    public void setEnvironmentImportance(int environmentImportance) {
        this.environmentImportance = environmentImportance;
    }
    private int elevationImportance = 0;
    public int getElevationImportance() {
        return elevationImportance;
    }
    public void setElevationImportance(int elevationImportance) {
        this.elevationImportance = elevationImportance;
    }
    private int viewImportance = 1;
    public int getViewImportance() {
        return viewImportance;
    }
    public void setViewImportance(int viewImportance) {
        this.viewImportance = viewImportance;
    }
    private int typeOfActivity = 1;
    public int getTypeOfActivity() {
        return typeOfActivity;
    }
    public void setTypeOfActivity(int typeOfActivity) {
        this.typeOfActivity = typeOfActivity;
    }

    private boolean singleTapActivated = false;
    public boolean getSingleTapActivated() {
        return singleTapActivated;
    }
    public void setSingleTapActivated(boolean set) {
        singleTapActivated = set;
    }

    private boolean chooseEnd = false;
    public boolean getChooseEnd() {
        return chooseEnd;
    }
    public void setChooseEnd(boolean set) { chooseEnd = set; }

    private boolean chooseStart = false;
    public boolean getChooseStart() {
        return chooseStart;
    }
    public void setChooseStart(boolean set) { chooseStart = set; }

    private boolean endToCurrentPosition = true;
    public void setEndToCurrentPosition(boolean endToCurrentPosition) {
        this.endToCurrentPosition = endToCurrentPosition;
    }
    public boolean getEndToCurrentPosition() {
        return endToCurrentPosition;
    }

    private boolean startToCurrentPosition = true;
    public void setStartToCurrentPosition(boolean startToCurrentPosition) {
        this.startToCurrentPosition = startToCurrentPosition;
    }
    public boolean getStartToCurrentPosition() {
        return startToCurrentPosition;
    }

    private boolean GPS = false;
    public boolean getGPS() { return GPS; }
    public void setGPS(boolean set) {
        this.GPS = set;
    }



    /*private boolean bAlg1Checked = true;
    private boolean bAlg2Checked = false;
    private boolean bAlg3Checked = false;
    public void setbAlg1Checked(){
        bAlg1Checked = true;
        bAlg2Checked = false;
        bAlg3Checked = false;
    }

    public void setbAlg2Checked(){
        bAlg1Checked = false;
        bAlg2Checked = true;
        bAlg3Checked = false;
    }

    public void setbAlg3Checked(){
        bAlg1Checked = false;
        bAlg2Checked = false;
        bAlg3Checked = true;
    }

    public boolean getAlg1Checked(){ return bAlg1Checked; }
    public boolean getAlg2Checked(){ return bAlg2Checked; }
    public boolean getAlg3Checked(){ return bAlg3Checked; }*/

    public enum state {
        INIT, INITSETSTART, INITSETSTARTTOCURRPOS, INIT2, INITSETEND, INITSETLENGTH,
        SETSTART, SETSTARTTOCURRPOS, SETEND, SETLENGTH, FINISHEDINIT, ROUTESETTINGS
    }
    public state initState = state.INIT;

    private float lastOrientation = 0.0f;
    public void setLastOrientation(float orientation){
        lastOrientation = orientation;
    }

    public float getLastOrientation(){
        return lastOrientation;
    }

    public float ScreenOrientationMod = 0;

    public MyRoad route;
    public ArrayList<GeoPoint> routeRun = new ArrayList<GeoPoint>();;
}
