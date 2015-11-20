package rsen.com.fbglobalhacks;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import at.grabner.circleprogress.CircleProgressView;

public class LyricsActivity extends AppCompatActivity {
    Firebase firebase;
    /*
    String artist;
    String band_members;
    String description;
    String genre;
    String website;
    String hometown;
    String coverURL;
    String profileURL;
    */
    String[] keys = new String[] {"artist", "genre", "band_members", "hometown", "description", "website", "cover", "picture"};
    String[] info = new String[keys.length];
    RecyclerView recyclerView;
    CircleProgressView circleView;
    MyRecyclerAdapter adapter;
    List<Lyric> lyrics;
    long timestampAdjustment = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .build();
        ImageLoader.getInstance().init(config);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Firebase.setAndroidContext(this);
        firebase = new Firebase("https://fbglobalhacks.firebaseio.com/");
        String song = getIntent().getStringExtra("song");
        getSupportActionBar().setTitle(song);

        updateInfo();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        circleView = (CircleProgressView) findViewById(R.id.circleView);
        circleView.spin();
        firebase.child("lyrics").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Reader reader = new StringReader(dataSnapshot.getValue(String.class));

                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                lyrics = new ArrayList<Lyric>();
                lyrics = Arrays.asList(gson.fromJson(reader, Lyric[].class));

                recyclerView.setAdapter(new MyRecyclerAdapter(LyricsActivity.this, lyrics, timestampAdjustment));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }
    private ValueEventListener timestampEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            timestampAdjustment = dataSnapshot.getValue(Long.class);
            recyclerView.setAdapter(new MyRecyclerAdapter(LyricsActivity.this, lyrics, timestampAdjustment));
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    };
    private class InfoListener implements ValueEventListener {
        int index;
        public InfoListener (int index) {
            this.index = index;
        }
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            info[index] = dataSnapshot.getValue(String.class);
            switch (index) {
                case 0:
                    pullFromFB();
                case 6:
                    break;
                case 7:
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(info[index], (ImageView) findViewById(R.id.profile));
                    break;
                default:
                    infoUpdated();
            }

        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    }

    private void pullFromFB() {
        firebase.child("fbpulled").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.getValue(Boolean.class))
                {
                    //TODO PULL FROM FB
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void infoUpdated() {
        ((TextView) findViewById(R.id.artist)).setText(info[0]);
        String infoText = "";
        for (String key : info)
        {
            infoText += key + "\n";
        }
        ((TextView) findViewById(R.id.info)).setText(infoText);

    }

    private void updateInfo()
    {
        int index = 0;
        for (String key : keys) {
            firebase.child(key).addListenerForSingleValueEvent(new InfoListener(index));
            index++;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        ImageLoader.getInstance().stop();
        firebase.removeEventListener(timestampEventListener);
        stopService(new Intent(this, RecordAudioService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebase.child("timestamp").addValueEventListener(timestampEventListener);
        startService(new Intent(this, RecordAudioService.class));

    }
}
