package com.christopherhield.newsreader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class Service extends android.app.Service {

    private static final String TAG = "MyService";
    private boolean running = true;
    private SampleReceiver sampleReceiver;
    private String storySource="";

    public Service() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

       // sendMessage("Service Started");
        //recieve broadcast info
        sampleReceiver = new SampleReceiver();

        IntentFilter filter1 = new IntentFilter(SampleReceiver.ACTION_MSG_TO_SERVICE);
        registerReceiver(sampleReceiver, filter1);

        //Creating new thread for my service
        //Always write your long running tasks in a separate thread, to avoid ANR

        new Thread(new Runnable() {
            @Override
            public void run() {

               while(running) {

                        while(storySource.isEmpty())
                        {
                            try {
                                Thread.sleep(250);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                             Log.d(TAG, "run: story list is empty");

                        }
                   NewsArticleDownloader articleDownloader=new NewsArticleDownloader(storySource,Service.this);
                   articleDownloader.execute();
                   storySource="";




                   // sendMessage("Service Broadcast Message " + Integer.toString(i+1));
                }
               // sendMessage("Service Done Sending Broadcasts");

                Log.d(TAG, "run: Ending loop");
            }
        }).start();

        
        return android.app.Service.START_STICKY;
    }

    public void onArticlesDownloadComplete(ArrayList<ArticleInfo>articleInfos)
    {
        Intent intent = new Intent();
        intent.setAction(MainActivity.ARTICLE_BROADCAST_TYPE);
        intent.putParcelableArrayListExtra(MainActivity.SERVICE_DATA, articleInfos);
        sendBroadcast(intent);
    }


    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
        running = false;
        super.onDestroy();
    }
    private class SampleReceiver extends BroadcastReceiver {

        static final String ACTION_MSG_TO_SERVICE="ACTION_MSG_TO_SERVICE";
        private static final String DATA_EXTRA1 ="serviceData" ;

        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getAction()) {
                case ACTION_MSG_TO_SERVICE:

                        if (intent.hasExtra(DATA_EXTRA1))
                            storySource = intent.getStringExtra(DATA_EXTRA1);
                    break;


            }
        }

    }
}
