package com.example.rushikesh.primus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import Navigation.URLDirections;
import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity implements AIListener,NavigationFinal,LocationListener {

    private AIService service;
    private Speech_Output s1;
    LocationManager lm;
    Location l;
    URLDirections u;
    public static int cnt=1;
    Location destination;
    private int battery_level;
    private BluetoothAdapter btadapter;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket btSocket;
    // Insert your server's MAC address
    private static String address = "00:15:83:35:8A:C9";
    private InputStream instream;
    private Handler handler;

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
             battery_level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);

        }
    };


    @Override
    public void onParseComplete() {
        destination=new Location("");
        if(u==null)
        {
            Toast.makeText(getApplicationContext(),"Route finding failed",Toast.LENGTH_LONG).show();
            return;
        }
        else {
            destination.setLatitude(u.route.endLocation.latitude);
            destination.setLongitude(u.route.endLocation.longitude);
            s1.talk(getApplicationContext(), u.route.steps.get(0).instruction);
            Toast.makeText(getApplicationContext(), "Route found", Toast.LENGTH_LONG).show();
            //shortNav();

            try {

            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final AIConfiguration config = new AIConfiguration("878ffc259e2f46b4becc3e5070a25a3a",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        u=new URLDirections(this);
        lm=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        service=AIService.getService(this,config);
        service.setListener(this);
        s1=new Speech_Output();
        btadapter=BluetoothAdapter.getDefaultAdapter();
        this.registerReceiver(this.mBatInfoReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        Button b1=(Button)findViewById(R.id.button);

        b1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                try {
                    handler.removeCallbacksAndMessages(null);
                    btSocket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });


        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {

                Bundle bundle = msg.getData();
                String message = bundle.getString("Message");
                //String[] data=message.split(" ");
                String inst=distProcess(message);
                s1.talk(getApplicationContext(),inst);


            }
        };

    }

    public void listening(View v)
    {

        service.startListening();
    }

    @Override
    public void onResult(final AIResponse response) {

     runOnUiThread(new Runnable() {
         @Override
         public void run() {
             Result result=response.getResult();

             String speech=result.getFulfillment().getSpeech();
             s1.talk(MainActivity.this,speech);
             String action=result.getAction();

             handleAction(action,result);

         }
     });



    }


    @Override
    public void onLocationChanged(Location location) {

        Location temp_loc=new Location("");
        temp_loc.setLatitude(u.route.steps.get(cnt).startLoc.latitude);
        temp_loc.setLongitude(u.route.steps.get(cnt).startLoc.longitude);
        if(location.distanceTo(temp_loc)<=5)
        {
            s1.talk(getApplicationContext(),u.route.steps.get(cnt).instruction);
            Toast.makeText(getApplicationContext(),u.route.steps.get(cnt).instruction,Toast.LENGTH_LONG).show();
            cnt++;

        }
        else if(location.distanceTo(destination)<=5)
        {
            s1.talk(getApplicationContext(),"Destination Reached");
        }

        return;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public void handleAction(String action, Result result)
    {
        switch(action) {

            case "navigation.start":

                String dest = "";
                JsonElement elem = result.getComplexParameter("to");
                JsonObject des1 = elem.getAsJsonObject();
                dest = des1.get("street-address").toString();
                Log.d("Parameters", dest);

                try {
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 100, this);
                    l = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    u.startServices(l, dest);

                }catch(SecurityException e)
                {
                    e.printStackTrace();
                }
                break;


            case "device.battery.check":
                    if(battery_level<25)
                    {
                        s1.talk(getApplicationContext(),"Your battery level is: "+Integer.toString(battery_level)+"% . You should connect your charger ");
                    }
                    else
                    {
                        s1.talk(getApplicationContext(),"Your battery level is: "+Integer.toString(battery_level)+"%");
                    }
                    break;

            case "shortnav.start":
                if(btadapter!=null)
                {
                    btadapter.enable();
                }
                BluetoothDevice device = btadapter.getRemoteDevice(address);


                try {
                    btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                btadapter.cancelDiscovery();

                try {
                    btSocket.connect();
                    instream=btSocket.getInputStream();
                }catch (IOException e)
                {
                    e.printStackTrace();
                }

                Runnable r=new Runnable() {
                    @Override
                    public void run() {
                        byte buffer[]= new byte[1024];
                        int bytes;

                        // while(true) {
                        try {
                            Message msg = new Message();
                            Bundle bundle = new Bundle();
                            bytes = instream.read(buffer);            //read bytes from input buffer
                            String yo = new String(buffer, 0, bytes);

                            bundle.putString("Message", yo);
                            msg.setData(bundle);
                            // send message to the handler
                            handler.sendMessage(msg);
                            handler.postDelayed(this,1000);
                        } catch (Exception e) {
                                e.printStackTrace();
                        }
                        // }//while
                    }//run
                };

                Thread t1=new Thread(r);
                t1.start();
                break;

            case "shortnav.stop":
                try {
                    handler.removeCallbacksAndMessages(null);
                    btSocket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case "time.get":
                SimpleDateFormat d=new SimpleDateFormat("hh:mm a");
                Date d1=new Date(System.currentTimeMillis());
                s1.talk(getApplicationContext(),"The time is "+d.format(d1));
                break;
            case "quit":
                unregisterReceiver(mBatInfoReceiver);
                this.finishAffinity();
                break;

        }

    }
    @Override
    public void onError(AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }

    public String distProcess(String message)
    {
        int steps;
        String instruction=" ";
        message=message.replaceAll("[^\\d.]","");
        if(message.length()!=0) {
            double dist = Double.parseDouble(message);
            if (!Double.isNaN(dist)) {
                steps = (int) dist / 30;
                if(dist==0||steps>5)
                {
                    return " ";
                }
                else {

                    if (steps != 0) {
                        instruction = "Obstacle Detected " + steps + " Steps away";
                    } else {
                        instruction = "Obstacle just ahead";
                    }
                }
            }
        }
        return instruction;

    }


}
