package rsen.com.fbglobalhacks;

import android.content.Intent;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.renderscript.Sampler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.File;
import java.util.Timer;

import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;

public class MainActivity extends AppCompatActivity {
     CircleProgressView progress;
     RecyclerView recyclerView;
     FloatingActionButton fab;
    String lastSong = null;
    Firebase firebase;
    boolean dontKillService = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Firebase.setAndroidContext(this);
        firebase = new Firebase("https://fbglobalhacks.firebaseio.com/");

        progress = (CircleProgressView) findViewById(R.id.circleView);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                fab.setEnabled(false);
                progress.setValueAnimated(5, 0, 5000);
                progress.setTextMode(TextMode.TEXT);
                progress.setText("Recording...");
                CountDownTimer timer = new CountDownTimer(5000, 5000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        progress.setShowTextWhileSpinning(true);
                        progress.spin();
                        progress.setText("Processing...");
                    }
                };
                timer.start();
                startRecording();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        dontKillService = false;
        firebase.child("song").addValueEventListener(songEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        firebase.child("song").removeEventListener(songEventListener);
        if (!dontKillService)
        {
            stopService(new Intent(this, RecordAudioService.class));
        }
    }


    private ValueEventListener songEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String song = dataSnapshot.getValue(String.class);
            if (lastSong == null)
            {
                lastSong = song;
            }
            else if (!song.equals(lastSong))
            {
                dontKillService = true;
                Intent i = new Intent(MainActivity.this, LyricsActivity.class);
                i.putExtra("song", song);
                startActivity(i);
            }
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    };
    private void startRecording()
    {
        startService(new Intent(this, RecordAudioService.class));

    }


}
