package Navigation;



import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.rushikesh.primus.NavigationFinal;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by rushikesh on 24/12/16.
 */

public class URLDirections {

    private String buffereddata;
    public Route route;
    public NavigationFinal n;

    public void startServices(Location src, String dest)
    {
        String URL=createURL(src,dest);
        try {
            new DownloadRawData().execute(URL);
        }catch (Exception e){e.printStackTrace();}


    }

    public URLDirections(NavigationFinal n)
    {
        this.n=n;
    }


    public String createURL(Location src,String dest)
    {
        String start;


        String directions_api="https://maps.googleapis.com/maps/api/directions/json?";
        String api_key="AIzaSyCOy3M86MiaQ5gq_dkXnAgUBnXmF71rkIs";
        if(src!=null) {
            start = Double.toString(src.getLatitude()) + "," + Double.toString(src.getLongitude());
        }
        else {
            start = "Bund+Garden";
        }
        dest=dest.replaceAll(" ","+");

        return directions_api+"origin="+start+"&destination="+dest+"&mode=walking&key="+api_key;


    }

    public void parseJson(String buffer)
    {
        if(buffer==null)
        {
            return;
        }
        try {
            JSONObject json_data = new JSONObject(buffer);
            route=new Route();
            Steps temp;
            Distance temp_dist;
            Duration temp_dur;
            JSONObject temp_start;
            JSONObject temp_end;
            JSONArray route_array=json_data.getJSONArray("routes");
            JSONObject json_route=route_array.getJSONObject(0);
            JSONArray legs=json_route.getJSONArray("legs");
            JSONObject json_dist=legs.getJSONObject(0).getJSONObject("distance");
            JSONObject json_dur=legs.getJSONObject(0).getJSONObject("duration");
            JSONObject json_startloc=legs.getJSONObject(0).getJSONObject("start_location");
            JSONObject json_endloc=legs.getJSONObject(0).getJSONObject("end_location");
            JSONArray json_steps=legs.getJSONObject(0).getJSONArray("steps");

            route.distance=new Distance(json_dist.getString("text"),json_dist.getInt("value"));
            route.duration1=new Duration(json_dur.getString("text"),json_dur.getInt("value"));
            route.startAddress=legs.getJSONObject(0).getString("start_address");
            route.endAddress=legs.getJSONObject(0).getString("end_address");
            route.startLocation=new LatLng(json_startloc.getDouble("lat"),json_startloc.getDouble("lng"));
            route.endLocation=new LatLng(json_endloc.getDouble("lat"),json_endloc.getDouble("lng"));
            route.polyline=decodePolyLine(json_route.getJSONObject("overview_polyline").getString("points"));

            route.steps=new ArrayList<Steps>();
            for(int i=0;i<json_steps.length();i++)
            {
                JSONObject step=json_steps.getJSONObject(i);
                temp_dist=new Distance(step.getJSONObject("distance").getString("text"),step.getJSONObject("distance").getInt("value"));
                temp_dur=new Duration(step.getJSONObject("duration").getString("text"),step.getJSONObject("distance").getInt("value"));
                temp_start=step.getJSONObject("start_location");
                temp_end=step.getJSONObject("end_location");
                temp=new Steps(temp_dist,temp_dur,new LatLng(temp_start.getDouble("lat"),temp_start.getDouble("lng")),new LatLng(temp_end.getDouble("lat"),temp_end.getDouble("lng")),step.getString("html_instructions"));
                route.steps.add(temp);
            }

        }catch (JSONException e){e.printStackTrace();}
    }


    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }




    private class DownloadRawData extends AsyncTask<String,Void,String>
    {
        @Override
        protected String doInBackground(String... params) {
            String link=params[0];
            try {
                URL url = new URL(link);
                InputStream is=url.openConnection().getInputStream();

                BufferedReader br=new BufferedReader(new InputStreamReader(is));
                StringBuilder buffer=new StringBuilder();

                String line;

                while((line=br.readLine())!=null)
                {
                    buffer.append(line+"\n");
                }

                return buffer.toString();


            }catch (Exception e){e.printStackTrace();}

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("url",s);
            parseJson(s);

            n.onParseComplete();

        }
    }


}
