package com.prakriti.mediaplayerservice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, RecyclerView.OnItemTouchListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "MainActivity";
    private ImageButton playButton, pauseButton, stopButton, nextButton, previousButton;
    private Intent serviceIntent;
    private boolean isPlaying = false, isServiceBound = false;
    private static final int REQ_CODE = 100;

    private List<String> songTitleList, songArtistList;
    private MusicPlayerAdapter musicAdapter;
    private SeekBar seekBar;
    private AudioManager audioManager;
    private ServiceConnection serviceConnection;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView musicRecyclerView = findViewById(R.id.musicRecyclerView);
        musicAdapter = new MusicPlayerAdapter(this);
        musicRecyclerView.setAdapter(musicAdapter);
        musicRecyclerView.addOnItemTouchListener(this);

        songTitleList = new ArrayList<>();
        songArtistList = new ArrayList<>();

        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);

        TextView txtCurrentProgress = findViewById(R.id.txtCurrentProgress);
        TextView txtSongDuration = findViewById(R.id.txtSongDuration);

        playButton = findViewById(R.id.playButton);
        pauseButton = findViewById(R.id.pauseButton);
        stopButton = findViewById(R.id.stopButton);
        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.previousButton);

        nextButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);
        playButton.setOnClickListener(this);
        pauseButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);

        serviceIntent = new Intent(this, MyMediaService.class);

        setAudioControls();
        checkStoragePermissions();

    }

    private void setAudioControls() {

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE); // cast Service object to AudioManager
        // access max volume of device
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // stream type is Stream Music
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

//        sbAudioVolume.setMax(maxVolume); // set max value
//        sbAudioVolume.setProgress(currentVolume); // set current value
//        // add seekbar listener & notify for changes on seekbar
//
//        sbAudioVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if(fromUser) {
//                    // Toast.makeText(MainActivity.this, "Volume: " + Integer.toString(progress), Toast.LENGTH_SHORT).show();
//                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
//                }
//            }
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {}
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {}
//        });

//        sbAudioPlayer.setMax(mediaPlayer.getDuration()); // set duration of music as max value of seekbar

    }

    private void checkStoragePermissions() {
        if(Build.VERSION.SDK_INT >= 23) {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission is granted");
                displaySongsList();
            }
            else {
                Log.i(TAG, "Permission is not granted");
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_CODE);
            }
        }
        else {
            Log.i(TAG, "Permission is granted");
            displaySongsList();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQ_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + " is " + grantResults[0]);
            displaySongsList();
        }
        else {
            Toast.makeText(this, "Storage access denied\nAllow permissions from Settings to use this app", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Permission is denied");
        }
    }

    private void displaySongsList() {
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.MediaColumns.TITLE}, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if(cursor != null && cursor.getCount()>0) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                Log.i(TAG, "SONG NAME: " + title);
                songTitleList.add(title);
                songArtistList.add(artist);
            }
            musicAdapter.notifyDataSetChanged();
        }
        else {
            Log.i(TAG, "CURSOR EMPTY");

        }

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music";
        File file = new File(path);
        String[] files = file.list();
        for(String str : files) {
            Log.i(TAG, str);
            if(str.endsWith(".mp3") || str.endsWith(".ogg")) {
                songAdapter.add(str);
                songAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playButton:
                if(!isPlaying) {
                    startService(serviceIntent);
                    isPlaying = true;
                }
                else {
                    Toast.makeText(this, "Song is already playing", Toast.LENGTH_SHORT).show();
                }
                /*

                mediaPlayer.start();
                timer = new Timer(); // initialising timer that exists throughout lifetime of program
                // now create new thread
                // seekbar is to be updated along with the music playing every second
                timer.scheduleAtFixedRate(new TimerTask() { // on new thread
                    @Override
                    public void run() {
                        // executed on Main thread, so update UI here
                        sbAudioPlayer.setProgress(mediaPlayer.getCurrentPosition());
                    }
                }, 0, 1000);
                // delay = 0 means starts timer as soon as button is tapped
                // period over which to execute run() task in ms
                break;
                // stop the new thread when not in use
                // cancel timer when music ends, implement OnCompletionListener for audio

                 */
                break;
            case R.id.pauseButton:
                /*
                mediaPlayer.pause();
                timer.cancel();
                 */
                break;
            case R.id.stopButton:
                isPlaying = false;
                stopService(serviceIntent);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String songClicked = mus.getItem(position);
        // send to services
        serviceIntent.putExtra("songName", songClicked);
        startService(serviceIntent);
    }

    // RECYCLER VIEW CONTROLS
    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        return false;
    }
    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
    }
    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }


    // SEEKBAR CONTROLS
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // we use a timer on a new thread, & send responses to UI thread
        // seekbar is to be updated along with the music playing every second
        if (fromUser) {
            // upon interaction with seekbar
            mediaPlayer.seekTo(progress);
            // play the audio file at specified progress time
        }
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
            mediaPlayer.pause();
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
            mediaPlayer.start();
    }
}