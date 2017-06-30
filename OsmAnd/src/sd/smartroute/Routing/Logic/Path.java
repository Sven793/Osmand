package sd.smartroute.Routing.Logic;

import java.util.LinkedList;
import java.util.List;

//@linggi all

public class Path {

    private List<Long> path;
    private double length;
    private double weight;

    public Path(double lengthIn, double weightIn, List<Long> pathIn){
        path = new LinkedList<Long>();
        length = lengthIn;
        weight = weightIn;
        path   = pathIn;
    }

    public void changePath(double lengthIn, double weightIn, List<Long> pathIn){
        length = lengthIn;
        weight = weightIn;
        path   = pathIn;
    }

    public double getWeight(){
        return weight;
    }

    public double getLength(){
        return length;
    }

    public List<Long> getPath(){
        return path;
    }
}
