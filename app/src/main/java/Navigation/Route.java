package Navigation;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by rushikesh on 28/12/16.
 */

public class Route {
    public Distance distance;
    public Duration duration1;
    public List<Steps> steps;
    public String startAddress;
    public LatLng startLocation;
    public String endAddress;
    public LatLng endLocation;
    public List<LatLng> polyline;



}
