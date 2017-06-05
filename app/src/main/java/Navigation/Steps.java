package Navigation;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by rushikesh on 28/12/16.
 */

public class Steps {

    public Distance dist;
    public Duration dur;
    public LatLng startLoc;
    public LatLng endLoc;
    public String instruction;


    public Steps(Distance dist,Duration dur,LatLng start,LatLng end,String inst)
    {
        this.dist=dist;
        this.dur=dur;
        this.startLoc=start;
        this.endLoc=end;

        inst=inst.replaceAll("<(.*?)*>", "");
        inst=inst.replaceAll("rd|Rd","Road");
        inst=inst.replaceAll("hwy|Hwy","highway");
        this.instruction=inst;

    }



}
