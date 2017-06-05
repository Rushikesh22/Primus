package com.example.rushikesh.primus;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by rushikesh on 23/4/17.
 */

public class Speech_Output {

    TextToSpeech t1;

    public void talk(final Context context, final String msg) {
        t1 = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(final int status) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        if (status != TextToSpeech.ERROR) {
                            t1.setLanguage(Locale.ENGLISH);
                            HashMap<String, String> map = new HashMap<>();
                            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
                            t1.speak(msg, TextToSpeech.QUEUE_FLUSH, map);

                            //t1.stop();
                            //t1.shutdown();

                        }


                    }
                }).start();

            }
        });
    }

}
