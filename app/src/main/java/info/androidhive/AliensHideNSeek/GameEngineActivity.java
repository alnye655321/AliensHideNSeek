package info.androidhive.AliensHideNSeek;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.View;
import android.view.View.OnClickListener;
import android.location.Location;
import android.widget.Toast;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.text.DateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import info.androidhive.AliensHideNSeek.app.AppController;
import info.androidhive.AliensHideNSeek.utils.Const;

public class GameEngineActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    //private boolean alien = false; //determine which game engine to run based on previous activity - alien || human player
    public boolean alienStatus; //global truth of player type
    public double distance = 666; //global distance to/from human/alien - set to default high number will lower on first updateReq()
    Human player1 = new Human("Military","Colonel Hicks","Kickass",0,0,0,0);
    Alien player2 = new Alien("Crawler","Xenomorph","Humans R Tasty",0,0,0,0);
    Environment game1 = new Environment("Default",300000,8); // 300000ms = 5mins !!! 8 = max players --> should be user set
    //game1.setGameId(2);
    private boolean gameActive = true; //!!! active game state - controls engine loop at bottom !!!
    public Handler handler;
    public ProgressBar progressBar; //no longer using from thread example
    public double lat; //local gps update variables, used in game engine thread
    public double lon;
    private TextView game_clock;//create game clock TextView --> in xml

    //alien player location storage array - for drawing location circles
    //private String[] alienLocations = new String[10]; //should set to user defined game size !!!

    //circle draw
    private static final int FRAME_DELAY = 50; // ms

    private ArrayList<Bitmap> mBitmaps;
    private final AtomicInteger mBitmapIndex = new AtomicInteger();
    private View mView;
    private Thread mThread;
    //close circle draw

    //soundpool tracking beeps
    private SoundPool soundpool;
    private HashMap<Integer, Integer> soundsMap;
    //close soundpool tracking beeps

//location settings---------------------------------------------------------------------------------

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000; //target interval for location updates. Inexact. Updates may be more or less frequent.

    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2; //fastest rate for active location updates. Exact. Updates will never be more frequent than this value

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    protected GoogleApiClient mGoogleApiClient; //entry point to Google Play services

    protected LocationRequest mLocationRequest; //Stores parameters for requests to the FusedLocationProviderApi

    protected Location mCurrentLocation; //Represents a geographical location

    // UI Widgets.
    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;
    protected TextView mLastUpdateTimeTextView;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;
    protected TextView distanceTextView;
    protected TextView gameOverTextView;
    protected Button startButton;
    protected Button stopButton;
    protected Button stopAudioButton;
    protected Button startAudioButton;

    // Labels.
    protected String mLatitudeLabel;
    protected String mLongitudeLabel;
    protected String mLastUpdateTimeLabel;
    protected String distanceLabel;

    protected Boolean mRequestingLocationUpdates; //Tracks the status of the location updates request. Value changes when the user presses the Start Updates and Stop Updates buttons

    protected String mLastUpdateTime; //Time when the location was updated represented as a String
//end location settings-----------------------------------------------------------------------------

    private String TAG = "tagger";
    private int TAGINT = 1;
    private TextView msgResponse;

    // These tags will be used to cancel the requests
    private String tag_json_obj = "jobj_req", tag_json_arry = "jarray_req";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_engine);

        handler = new Handler(); //for new thread
        progressBar = (ProgressBar) findViewById(R.id.progressBar1); //for new thread

        Intent intent = getIntent();

        alienStatus = getIntent().getBooleanExtra("alienStatus", false);
        Log.d("MYSTR", "Alien Status Create: " + alienStatus);

        String handleMessage = intent.getStringExtra(CreateGameActivity.HANDLE_MESSAGE);
        String taglineMessage = intent.getStringExtra(CreateGameActivity.TAGLINE_MESSAGE);
        String gameMessage = intent.getStringExtra(CreateGameActivity.GAME_MESSAGE);
        String playerCountString = intent.getStringExtra(CreateGameActivity.COUNT_MESSAGE);

        if (!alienStatus) {
            int playerCount = Integer.parseInt(playerCountString); // convert user supplied player count value to integer
        }

        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(handleMessage);

        TextView textView1 = new TextView(this);
        textView1.setTextSize(40);
        textView1.setText(taglineMessage);

        TextView textView2 = new TextView(this);
        textView2.setTextSize(40);
        textView2.setText(gameMessage);

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_game_engine);
        layout.addView(textView);
        layout.addView(textView1);
        layout.addView(textView2);

        game_clock = (TextView) findViewById( R.id.timer_text );//set game clock from xml

        //soundpool tracking beeps
        soundpool = new SoundPool(1, AudioManager.STREAM_MUSIC, 100); //first value only allows 1 audio stream playing at once, frequency updated automatically based on distance
        soundsMap = new HashMap<Integer, Integer>();
        soundsMap.put(1, soundpool.load(this, R.raw.short_beep, 1));
        soundsMap.put(2, soundpool.load(this, R.raw.short_beep, 1));
        //close soundpool tracking beeps

        //start motion tracker animation
        //mTapScreenTextAnimImgView = (ImageView) findViewById(R.id.imageView);
        //new SceneAnimation(mTapScreenTextAnimImgView, mTapScreenTextAnimRes, mTapScreenTextAnimDuration, mTapScreenTextAnimBreak);

        // initial http reqs to create game in API
        if(alienStatus) {
            player2.createNewAlien(handleMessage, taglineMessage); //adds to player to API
        }
        else {
            player1.createNewGame(gameMessage, handleMessage, taglineMessage); //only run as human
        }
        //location settings------------------------------------------------------------------------
        // Locate the UI widgets.
        //mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        //mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);
        distanceTextView = (TextView) findViewById(R.id.distance_text);
        gameOverTextView = (TextView) findViewById(R.id.game_over);
        startButton = (Button) findViewById(R.id.start_button);
        stopButton = (Button) findViewById(R.id.stop_button);
        stopAudioButton = (Button) findViewById(R.id.stopAudio_button);
        startAudioButton = (Button) findViewById(R.id.startAudio_button);

        // Set labels.
        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_time_label);
        distanceLabel = getResources().getString(R.string.distance_label);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        // Start the process of building a GoogleApiClient and requesting the LocationServices API
        buildGoogleApiClient();
        //close location settings------------------------------------------------------------------

        //circle draw loading-----------------------------------------------------------------------
        // load images
        mBitmaps = new ArrayList<Bitmap>();
        for(int resId : new int[]{
                R.drawable.human001, R.drawable.human003,
                R.drawable.human005, R.drawable.human007, R.drawable.human009, R.drawable.human011, R.drawable.human013,
                R.drawable.human015, R.drawable.human017, R.drawable.human019, R.drawable.human021, R.drawable.human023,
                R.drawable.human025, R.drawable.human027, R.drawable.human029, R.drawable.human031, R.drawable.human033,
                R.drawable.human035, R.drawable.human037, R.drawable.human039, R.drawable.human041, R.drawable.human043,
                R.drawable.human045, R.drawable.human047, R.drawable.human049, R.drawable.human051, R.drawable.human053,
                R.drawable.human055, R.drawable.human057, R.drawable.human059, R.drawable.human061, R.drawable.human063,
                R.drawable.human065, R.drawable.human067, R.drawable.human069, R.drawable.human071, R.drawable.human073,
                R.drawable.human075, R.drawable.human077, R.drawable.human079, R.drawable.human081, R.drawable.human083,
                R.drawable.human085, R.drawable.human087, R.drawable.human089, R.drawable.human091, R.drawable.human093,
                R.drawable.human095, R.drawable.human097, R.drawable.human099, R.drawable.human101, R.drawable.human103,
                R.drawable.human105, R.drawable.human107, R.drawable.human109, R.drawable.human111, R.drawable.human113,
                R.drawable.human115, R.drawable.human117, R.drawable.human119, R.drawable.human001, R.drawable.human001,
                R.drawable.human001, R.drawable.human001
        }){
            //mBitmaps.add(BitmapFactory.decodeResource(getResources(), resId, options)); //using options for Bitmap Factory scaling
            mBitmaps.add(BitmapFactory.decodeResource(getResources(), resId)); //without scaling
        }
        final Bitmap bitmapPosDrawable = mBitmaps.get(0); // save reference to example drawable png - all have same dimensions
        final int bitmapCenterX = bitmapPosDrawable.getWidth()/2; // get the center X position in image, px
        final int bitmapCenterY = bitmapPosDrawable.getHeight()/2; // get the center Y position in image, px

        // apply view and start draw
        ViewGroup root = (ViewGroup) findViewById(R.id.game_engine_animation);
        root.addView(mView = new View(this){
            @Override
            public void draw(Canvas canvas) {
                canvas.drawBitmap(mBitmaps.get(Math.abs(mBitmapIndex.get() % mBitmaps.size())), 10, 10, null); //png animation
                super.draw(canvas);

                //circle draws
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.FILL);
                int paintColor = Color.parseColor("#446ef8");
                paint.setColor(paintColor);
                //canvas.drawCircle(bitmapCenterX, bitmapCenterY, 25, paint); //currently x and y are placing in center screen

                // host --> drawing all the alien gps locations
                if(!alienStatus) {
                    String[] alienLocations = player1.getAlienLocations();
                    for (int i = 0; i < alienLocations.length; i++) {
                        if (alienLocations[i] != null) {
                            String gpsString = alienLocations[i];
                            String[] parts = gpsString.split(",");
                            double alienLatitude = Double.parseDouble(parts[0]);
                            double alienLongitude = Double.parseDouble(parts[1]);
                            double[] gpsPtsXY = game1.plotGPSpoint(player1.getLat(), player1.getLon(), alienLatitude, alienLongitude, bitmapCenterX, bitmapCenterY);
                            float xFloat = (float) gpsPtsXY[0];
                            float yFloat = (float) gpsPtsXY[1];
                            canvas.drawCircle(xFloat, yFloat, 20, paint);
                        }
                    }
                }

                //alien --> drawing only host gps location
                if (alienStatus && player1.getLat() != 0 &&  player1.getLon() != 0){
                    double[] gpsPtsXY = game1.plotGPSpoint(player2.getLat(), player2.getLon(), player1.getLat(), player1.getLon(), bitmapCenterX, bitmapCenterY);
                    float xFloat = (float) gpsPtsXY[0];
                    float yFloat = (float) gpsPtsXY[1];
                    canvas.drawCircle(xFloat, yFloat, 20, paint);
                }
            }
        });
        //close circle draw loading-----------------------------------------------------------------
    } //close on create-----------------------------------------------------------------------------

    //soundpool tracking beeps
    private int soundStreamID;
    public void playSound(int sound, float fSpeed) {
        AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
        float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = streamVolumeCurrent / streamVolumeMax;
        soundStreamID = soundpool.play(soundsMap.get(sound), volume, volume, 1, -1, fSpeed); // -1 triggers infinite loop here
    }
    public void playSingleSound(int sound, float fSpeed) {
        soundpool.autoPause();
        soundpool.stop(1);
        AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
        float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = streamVolumeCurrent / streamVolumeMax;
        soundpool.play(soundsMap.get(sound), volume, volume, 1, 0, fSpeed); // 0 is single sound
    }
    // control frequecy of audio beeps based on distance
    private void soundPlayer(double distanceFromTarget){
        if(distanceFromTarget > 150){
            playSound(1, 0.5f);
        }
        else if(distanceFromTarget > 125){
            playSound(1, 0.7f);
        }
        else if(distanceFromTarget > 100){
            playSound(1, 0.9f);
        }
        else if(distanceFromTarget > 75){
            playSound(1, 1.0f);
        }
        else if(distanceFromTarget > 50){
            playSound(1, 1.2f);
        }
        else if(distanceFromTarget > 25){
            playSound(1, 1.4f);
        }
        else if(distanceFromTarget > 0){
            playSound(1, 1.6f);
        }
    }
    //soundpool tracking beeps


    //start game - create new game thread, audio, location updates
    public void startProgress(View view) { //currently executed on xml button click
        startLocationUpdates(); // start google location API update service
        new Thread(new Engine()).start(); //start new game engine thread
        startButton.setVisibility(View.GONE); //switch the buttons
        stopButton.setVisibility(View.VISIBLE);

        //start new game timer - updating every 1000ms
        new CountDownTimer(10*60000, 1000) {

            public void onTick(long millisUntilFinished) {
                game_clock.setText("Time: " +new SimpleDateFormat("mm:ss").format(new Date( millisUntilFinished)));
            }

            public void onFinish() {
                game_clock.setText("Game Complete!");
            }
        }.start();
        //close game timer
        playSound(1, 0.5f); //soundpool tracking beeps - starting at lowest frequency
    }
    //close start game

    // stop audio from activity button click
    public void stopAudio(View view){
        soundpool.stop(soundStreamID); //stop sound - by streamID created by soundpool loop
        stopAudioButton.setVisibility(View.GONE); //switch the buttons
        startAudioButton.setVisibility(View.VISIBLE);
    }

    // start audio from activity button click
    public void startAudio(View view){
        playSound(1, 0.5f); //resume soundpool tracking beeps - starting at lowest frequency
        startAudioButton.setVisibility(View.GONE); //switch the buttons
        stopAudioButton.setVisibility(View.VISIBLE);
    }

    //location methods------------------------------------------------------------------------------
    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
                setButtonsEnabledState();
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();
        }
    }


     //Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    //Sets up the location request - using ACCESS_FINE_LOCATION --> more accurate, using fused location provider API
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. Inexact
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. Exact
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    //Handles the Start Updates button and requests start of location updates. Does nothing if updates have already been requested.
    public void startUpdatesButtonHandler(View view) {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            setButtonsEnabledState();
            startLocationUpdates();
        }
    }


    //Handles the Stop Updates button, and requests removal of location updates. Does nothing if updates were not previously requested.
    public void stopUpdatesButtonHandler(View view) {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            setButtonsEnabledState();
            stopLocationUpdates();
        }
    }

    //Requests location updates from the FusedLocationApi
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }

    //Ensures that only one button is enabled at any time
    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }

    //Updates the latitude, the longitude, and the last location time in the UI
    private void updateUI() {
        mLatitudeTextView.setText(String.format("%s: %f", mLatitudeLabel,
                mCurrentLocation.getLatitude()));
        mLongitudeTextView.setText(String.format("%s: %f", mLongitudeLabel,
                mCurrentLocation.getLongitude()));
        mLastUpdateTimeTextView.setText(String.format("%s: %s", mLastUpdateTimeLabel,
                mLastUpdateTime));
        distanceTextView.setText(String.format("%.2f %s", distance,
                distanceLabel));
    }

    //Removes location updates from the FusedLocationApi
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or stopped state
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
    //end location methods--------------------------------------------------------------------------

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

        //motion tracker animation thread
        mThread = new Thread(){
            @Override
            public void run() {
                // wait and invalidate view until interrupted
                while(true){
                    try {
                        Thread.sleep(FRAME_DELAY);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break; // get out if interrupted
                    }
                    mBitmapIndex.incrementAndGet();
                    mView.postInvalidate();
                }
            }
        };

        mThread.start();
        //close motion tracker animation thread

    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the connection to GoogleApiClient intact.  Here, we resume receiving location updates if the user has requested them
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        gameActive = true;//start game loop
        playSound(1, 0.5f); //resume soundpool tracking beeps - starting at lowest frequency
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
        gameActive = false; //stop game loop
        //soundpool.autoPause();
        soundpool.stop(soundStreamID); //stop sound - by streamID created by soundpool loop
        //soundpool = null; //reset soundpool
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        gameActive = false;//stop game loop
        mThread.interrupt(); // stop motion tracker animation thread
        //soundpool.autoPause();
        soundpool.stop(soundStreamID); //stop sound - by streamID created by soundpool loop
    }

    @Override
    public void onDestroy() {
        super.onDestroy();  // Always call the superclass
        gameActive = false;//stop game loop
        //soundpool.autoPause(); //pause sound
        soundpool.stop(soundStreamID); //stop sound - by streamID created by soundpool loop
        mThread.interrupt(); // stop motion tracker animation thread
        Log.d("MYSTR", "onDestory: true");

        // Stop method tracing that the activity started during onCreate()
        android.os.Debug.stopMethodTracing();
    }

    //Runs when a GoogleApiClient object successfully connects
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        }

        // If the user presses the Start Updates button before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    //Callback that fires when the location changes
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
        Toast.makeText(this, getResources().getString(R.string.location_updated_message),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    //Stores activity data in the Bundle
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    //leave game button --> back to main screen
    public void leaveGame(View view){
        soundpool.autoPause(); //pasue sound
        //soundpool.stop(1); //stop sound - need correct stream id here
        Intent mainActivityStart = new Intent(this, MainActivity.class);
        startActivity(mainActivityStart);
    }

//game loop - run on separate thread
    class Engine implements Runnable {
        @Override
        public void run() {
            while (gameActive) {
                try {
                    Thread.sleep(3000); //delay in ms
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if(alienStatus) { //alien game
                            Log.d("MYSTR", "alienStatus: true");
                            lat = mCurrentLocation.getLatitude();
                            lon = mCurrentLocation.getLongitude();
                            player2.setLat(lat);
                            player2.setLon(lon);
                            String latString = Double.toString(player2.getLat());
                            String lonString = Double.toString(player2.getLon());
                            player1.setLat(player2.getHumanLatRes());
                            player1.setLon(player2.getHumanLonRes());
                            Log.d(TAG, latString);
                            player2.updateAlienReq(latString, lonString, 2);
                            Log.d(TAG, Double.toString(lat));
                            distance = player2.getDistance();
                            soundPlayer(distance); //soundpool tracking beeps

                        }
                        else{ // human/host game
                            Log.d("MYSTR", "alienStatus: false");
                            lat = mCurrentLocation.getLatitude();
                            lon = mCurrentLocation.getLongitude();
                            player1.setLat(lat);
                            player1.setLon(lon);
                            String latString = Double.toString(player1.getLat());
                            String lonString = Double.toString(player1.getLon());
                            player1.updateReq(latString, lonString);
                            player1.alienArrayRequest(2);// !!! pass in real gameID here !!!
                            if(player1.getDistance() > 0){
                                distance = player1.getDistance();
                                soundPlayer(distance); //soundpool tracking beeps

                                if (player1.getCheckAlienWin() != -1){ // check if game is over
                                    Log.d("MYINT", "AlienGameWinnerIS: " + player1.getCheckAlienWin());
                                    game1.setActive(false);
                                    gameOverTextView.setVisibility(View.VISIBLE);
                                }
                            }

                        }
                    }//end run
                });
            }
        }
    }

}//close game engine activity
