package js.myroute.Helpers;

import org.jetbrains.annotations.NotNull;

/* //@linggi
 * because java doesn't have pairs
 */
public class Tuple<F,S>{
    public F first;
    public S second;
    private final String TAG = "Tuple";


    public Tuple(@NotNull F f, @NotNull S s){
        first = f;
        second = s;
    }

    @Override
    public String toString() {
        return ("First: " + first + " Second: " + second);
    }
}
