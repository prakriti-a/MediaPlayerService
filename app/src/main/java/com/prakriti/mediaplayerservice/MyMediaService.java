package com.prakriti.mediaplayerservice;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;

public class MyMediaService extends Service {

    private final String TAG = "MyMediaService";
    private MediaPlayer mediaPlayer;

    public class MyMediaBinder extends Binder {
        public MyMediaService getService() {
            return MyMediaService.this;
        }
    }
    private Binder binder = new MyMediaBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

        @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "Service Created", Toast.LENGTH_SHORT).show();
        if(mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        if(mediaPlayer != null) {
            String songTitle = intent.getStringExtra("TITLE");
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + "/" + Environment.DIRECTORY_MUSIC + "/"
                        + songTitle + ".mp3");
                mediaPlayer.prepare();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "onStartCommand: run: " + Thread.currentThread().getId());
                        mediaPlayer.seekTo(intent.getIntExtra("PROGRESS", 0));
                        mediaPlayer.start();
                    }
                }).start();
            }
            catch (IOException e) {

            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
        if(mediaPlayer!= null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

}
