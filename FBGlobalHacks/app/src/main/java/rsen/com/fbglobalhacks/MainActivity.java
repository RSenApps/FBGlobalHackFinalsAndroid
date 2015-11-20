package rsen.com.fbglobalhacks;

import android.content.Intent;
import android.media.Image;
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
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.File;
import java.util.Timer;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.skyfishjy.library.RippleBackground;

import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;

public class MainActivity extends AppCompatActivity {
     CircleProgressView progress;
     ImageView recordBtn;
    String lastSong = null;
    Firebase firebase;
    boolean dontKillService = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        final LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("public_profile");
        // If using in a fragment

        callbackManager = CallbackManager.Factory.create();

        // Other app specific specialization

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken.setCurrentAccessToken(loginResult.getAccessToken());
                recordBtn.setEnabled(true);

            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        Firebase.setAndroidContext(this);
        firebase = new Firebase("https://fbglobalhacks.firebaseio.com/");
        progress = (CircleProgressView) findViewById(R.id.circleView);
        progress.setTextMode(TextMode.TEXT);
        recordBtn = (ImageView) findViewById(R.id.startRecording);
        AccessToken token = AccessToken.getCurrentAccessToken();
        if (token == null)
        {
            recordBtn.setEnabled(false);
        }

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlphaAnimation animation1 = new AlphaAnimation(0.0f, 1.0f);
                animation1.setDuration(500);

                AlphaAnimation animation2 = new AlphaAnimation(1.0f, 0.4f);
                animation2.setDuration(1000);
                animation2.setFillAfter(true);
                animation2.setFillEnabled(true);
                //recordBtn.startAnimation(animation2);
                progress.setVisibility(View.VISIBLE);
                recordBtn.setEnabled(false);
                progress.startAnimation(animation1);
                ((RippleBackground) findViewById(R.id.content)).startRippleAnimation();
                progress.setValueAnimated(8, 0, 8000);
                CountDownTimer timer = new CountDownTimer(8000, 8000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        progress.setShowTextWhileSpinning(true);
                        progress.spin();
                    }
                };
                timer.start();
                startRecording();
            }
        });


    }
    private CallbackManager callbackManager;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
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
                if (song.length() > 0) {
                    lastSong = song;
                    dontKillService = true;
                    Intent i = new Intent(MainActivity.this, LyricsActivity.class);
                    i.putExtra("song", song);
                    startActivity(i);
                }
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
