package com.example.rushikesh.primus;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

public class StartUpActivity extends AppCompatActivity {

    Speech_Output s1;
    Intent r;
    public String user;

    private static final String TAG="Bluetooth";
    protected static final int RESULT_SPEECH=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);
        r=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        r.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,"en-UK");
        s1=new Speech_Output();


        s1.talk(getApplicationContext(),"Would you like to start the navigation app?");


        s1.t1.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {



            }

            @Override
            public void onDone(String utteranceId) {

                try
                {

                    startActivityForResult(r,RESULT_SPEECH);
                }catch(Exception e){}

            }

            @Override
            public void onError(String utteranceId) {

            }
        });






    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode)
        {
            case RESULT_SPEECH:{
                if(resultCode==RESULT_OK && null!=data)
                {
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Toast.makeText(getApplicationContext(),text.get(0),Toast.LENGTH_LONG).show();
                    user=text.get(0);
                    if(user.equalsIgnoreCase("Yes"))
                    {
                        Intent i1=new Intent(this,MainActivity.class);

                        startActivity(i1);
                    }
                    else if(user.equalsIgnoreCase("No"))
                    {
                        this.finishAffinity();
                    }
                    else
                    {
                        s1.talk(getApplicationContext(),"Can you repeat?");
                        startActivityForResult(r,RESULT_SPEECH);
                    }
                }
                break;
            }
        }
    }
}
