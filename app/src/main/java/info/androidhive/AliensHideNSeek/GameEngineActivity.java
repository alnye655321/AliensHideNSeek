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

import info.androidhive.AliensHideNSeek.app.AppController;
import info.androidhive.AliensHideNSeek.utils.Const;

public class GameEngineActivity extends Activity implements OnClickListener, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    //private boolean alien = false; //determine which game engine to run based on previous activity - alien || human player
    public boolean alienStatus; //global truth of player type
    public double distance = 666; //global distance to/from human/alien - set to default high number will lower on first updateReq()
    Human player1 = new Human("Military","Colonel Hicks","Kickass",0,0,0,0);
    Alien player2 = new Alien("Crawler","Xenomorph","Humans R Tasty",0,0,0,0);
    Environment game1 = new Environment("Default",300000,8); // 300000ms = 5mins !!! 8 = max players --> should be user set
    private boolean gameActive = true; //!!! active game state - controls engine loop at bottom !!!
    public Handler handler;
    public ProgressBar progressBar; //no longer using from thread example
    public double lat; //local gps update variables, used in game engine thread
    public double lon;
    private TextView game_clock;//create game clock TextView --> in xml

    //soundpool tracking beeps
    public SoundPool soundpool;
    public HashMap<Integer, Integer> soundsMap;
    //close soundpool tracking beeps

    //motion tracker animation settings
    private ImageView mTapScreenTextAnimImgView;
    private final int[] mTapScreenTextAnimRes = {R.drawable.human001, R.drawable.human003,
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
            R.drawable.human001, R.drawable.human001};
    private final int mTapScreenTextAnimDuration = 30;
    private final int mTapScreenTextAnimBreak = 1000;
    //close motion tracker animation settings

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
    private Button btnJsonObj;
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
        //close soundpool tracking beeps

        //start motion tracker animation
        mTapScreenTextAnimImgView = (ImageView) findViewById(R.id.imageView);
        new SceneAnimation(mTapScreenTextAnimImgView, mTapScreenTextAnimRes, mTapScreenTextAnimDuration, mTapScreenTextAnimBreak);

        if(alienStatus) {
            createNewAlien(); //adds to players database
            Log.d("MYSTR", "Alien Created!");
        }
        else {
            createNewGame(); //only run as human
        }
        //location settings------------------------------------------------------------------------
        // Locate the UI widgets.
        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);
        distanceTextView = (TextView) findViewById(R.id.distance_text);
        gameOverTextView = (TextView) findViewById(R.id.game_over);
        startButton = (Button) findViewById(R.id.start_button);
        stopButton = (Button) findViewById(R.id.stop_button);

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
    } //close on create

    //soundpool tracking beeps
    public void playSound(int sound, float fSpeed) {
        AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
        float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = streamVolumeCurrent / streamVolumeMax;
        soundpool.play(soundsMap.get(sound), volume, volume, 1, -1, fSpeed); // -1 triggers infinite loop here
    }
    public void playSingleSound(int sound) {
        soundpool.stop(soundsMap.get(sound));
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

     //JSON POST Req - Create New Game with Host Player---------run in onCreate()-------------------
    private void createNewGame() {

        Intent intent = getIntent(); //get host submitted values from previous activity --> CreateGameActivity
        String gameMessage = intent.getStringExtra(CreateGameActivity.GAME_MESSAGE);
        String handleMessage = intent.getStringExtra(CreateGameActivity.HANDLE_MESSAGE);
        String taglineMessage = intent.getStringExtra(CreateGameActivity.TAGLINE_MESSAGE);
        String tag_json_obj = "json_obj_req";

        String url = "http://node.nyedigital.com/game";

        Map<String, String> params = new HashMap();// object payload values
        params.put("name", gameMessage);
        params.put("handle", handleMessage);
        params.put("tagline", taglineMessage);

        JSONObject parameters = new JSONObject(params);//create object payload String type, int id, int timeLimit, int players

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, parameters,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.d(TAG, response.toString());
                        try {
                            int gameId = response.getInt("gameId");
                            int playerId = response.getInt("playerId");
                            //TAGINT
                            //Log.d("MYINT", "value: " + gameId);
                            //Log.d("MYINT", "value: " + playerId);
                            player1.setGameId(gameId); //define player properties from server response
                            player1.setPlayerId(playerId);
                            game1.setGameId(gameId);
                            Log.d("MYINT", "value: " + game1.getGameId());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        }) {

//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("name", "Android");

//                return params;
//            }
        };

// Add req to queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    } //JSON POST Req - Create New Game with Host Player--------------------------------------------

    //JSON POST Req - Human Continuous Update-------------------------------------------------------------
    private void updateReq(String lat, String lon) {
// Tag used to cancel the request

        String tag_json_obj = "update_json_obj_req";

        String url = "http://node.nyedigital.com/update/human";

        Map<String, String> params = new HashMap();// object payload values
        params.put("id", "3");
        params.put("lat", lat);
        params.put("lon", lon);
        params.put("checkStart", player1.getCheckStart());
        params.put("gameId", "2");

        JSONObject parameters = new JSONObject(params);//create object payload

        JsonObjectRequest updateObjReq = new JsonObjectRequest(Request.Method.POST,
                url, parameters,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.d(TAG, response.toString());
                        Log.d(TAG, response.toString());

                        try {
                            // Parsing json object response
                            // response will be a json object
                            String startStatus = response.getString("startStatus"); //set response values
                            String updateStatus = response.getString("updateStatus");
                            Log.d(TAG, startStatus);
                            Log.d(TAG, updateStatus);
                            if (startStatus == "complete"){ //check for a complete response, then set object to false to stop sending request !!! refactor to using private boolean
                                player1.setCheckStart("false");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        }) {
// optional params
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("name", "Android");

//                return params;
//            }
        };

// Add req to queue
        AppController.getInstance().addToRequestQueue(updateObjReq, tag_json_obj);
    } //JSON POST Req - Human Continuous Update-----------------------------------------------------

    //JSON POST Req - Create Alien Player----------run in onCreate()--------------------------------
    private void createNewAlien() {

        Intent intent = getIntent(); //get alien submitted values from previous activity --> JoinGameActivity
        String handleMessage = intent.getStringExtra(JoinGameActivity.HANDLE_MESSAGE);
        String taglineMessage = intent.getStringExtra(JoinGameActivity.TAGLINE_MESSAGE);
        String tag_json_obj = "json_obj_req";
        String gameIdMessage = "2"; //manual entry !!! need to set from previous alien lobby screen

        String url = "http://node.nyedigital.com/alien";

        Map<String, String> params = new HashMap();// object payload values
        params.put("handle", handleMessage);
        params.put("tagline", taglineMessage);
        params.put("gameId", gameIdMessage);

        JSONObject parameters = new JSONObject(params);//create object payload String type, int id, int timeLimit, int players

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, parameters,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.d(TAG, response.toString());
                        try {
                            int gameId = 2; //manual entry !!! need to set from previous alien lobby screen
                            int playerId = response.getInt("playerId");
                            //TAGINT
                            //Log.d("MYINT", "value: " + gameId);
                            //Log.d("MYINT", "value: " + playerId);
                            player2.setGameId(gameId); //define player properties from server response
                            player2.setPlayerId(playerId);
                            game1.setGameId(gameId);
                            Log.d("MYINT", "AlienPlayerId Value: " + playerId);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        }) {

//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("name", "Android");

//                return params;
//            }
        };

// Add req to queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    } //JSON POST Req - Create New Alien Player-----------------------------------------------------

    //JSON POST Req - Alien Continuous Update-------------------------------------------------------
    private void updateAlienReq(String lat, String lon) {

        String tag_json_obj = "json_obj_req";

        String url = "http://node.nyedigital.com/update/alien";
        String gameIdString = Integer.toString(player2.getGameId());

        Map<String, String> params = new HashMap();// object payload values
        params.put("id", "2");
        params.put("lat", lat);
        params.put("lon", lon);
        params.put("checkStart", player2.getCheckStart());
        params.put("gameId", gameIdString);

        JSONObject parameters = new JSONObject(params);//create object payload

        JsonObjectRequest updateObjReq = new JsonObjectRequest(Request.Method.POST,
                url, parameters,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.d(TAG, response.toString());
                        Log.d(TAG, response.toString());

                        try {
                            // Parsing json object response
                            // response will be a json object
                            String startStatus = response.getString("startStatus"); //set response values
                            String updateStatus = response.getString("updateStatus");
                            Double humanLatRes = response.getDouble("humanLat");
                            Double humanLonRes = response.getDouble("humanLon");
                            //Log.d(TAG, startStatus);
                            //Log.d(TAG, updateStatus);
                            Log.d("MYINT", "Human Lat Response: " + humanLatRes);
                            Log.d("MYINT", "Human Lon Response: " + humanLonRes);
                            if (startStatus == "complete"){ //check for a complete response, then set object to false to stop sending request !!! refactor to using private boolean
                                player2.setCheckStart("false");
                            }

                            double tempDistance = game1.getDistance(humanLatRes, humanLonRes, player2.getLat(), player2.getLon()); //get distance from human
                            distance = tempDistance * 1000; //update distance to send to display

                            //soundpool tracking beeps
                            if(distance > 150){
                                playSound(1, 0.5f);
                            }
                            else if(distance > 125){
                                playSound(1, 0.7f);
                            }
                            else if(distance > 100){
                                playSound(1, 0.9f);
                            }
                            else if(distance > 75){
                                playSound(1, 1.0f);
                            }
                            else if(distance > 50){
                                playSound(1, 1.2f);
                            }
                            else if(distance > 25){
                                playSound(1, 1.4f);
                            }
                            else if(distance > 0){
                                playSound(1, 1.6f);
                            }
                            //close soundpool tracking beeps

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        }) {
// optional params
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("name", "Android");

//                return params;
//            }
        };

// Add req to queue
        AppController.getInstance().addToRequestQueue(updateObjReq, tag_json_obj);
    } //JSON POST Req - Alien Continuous Update-----------------------------------------------------

    //alien enemy location call continuous update - for host ---------------------------------------
    //create array beforehand - set for
    private void alienArrayRequest(int gameId) {

        String urlJsonArry = "http://node.nyedigital.com/aliens/" + gameId;


        JsonArrayRequest alienReq = new JsonArrayRequest(urlJsonArry,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());

                        try {
                            // Parsing json array response, loop through each json object

                            for (int i = 0; i < response.length(); i++) {

                                JSONObject alien = (JSONObject) response
                                        .get(i);

                                int idResAlien = alien.getInt("id");
                                String handleResAlien = alien.getString("handle");
                                String taglineResAlien = alien.getString("tagline");
                                boolean humanResAlien = alien.getBoolean("human");
                                double latResAlien = alien.getDouble("lat");
                                double lonResAlien = alien.getDouble("lon");
                                double latStartResAlien = alien.getDouble("latstart");
                                double lonStartResAlien = alien.getDouble("lonstart");
                                int  gameIdResAlien = alien.getInt("game_id");

                                double tempDistance = game1.getDistance(player1.getLat(), player1.getLon(), latResAlien, lonResAlien); //get distance from alien
                                if(tempDistance < distance) { //if the distance is closer, update distance to send to display
                                    distance = tempDistance * 1000;

                                    //soundpool tracking beeps
                                    if(distance > 150){
                                        playSound(1, 0.5f);
                                    }
                                    else if(distance > 125){
                                        playSound(1, 0.7f);
                                    }
                                    else if(distance > 100){
                                        playSound(1, 0.9f);
                                    }
                                    else if(distance > 75){
                                        playSound(1, 1.0f);
                                    }
                                    else if(distance > 50){
                                        playSound(1, 1.2f);
                                    }
                                    else if(distance > 25){
                                        playSound(1, 1.4f);
                                    }
                                    else if(distance > 0){
                                        playSound(1, 1.6f);
                                    }
                                    //close soundpool tracking beeps
                                }

                                //check if alien has captured human - ie. occupy same gps location --> Game Over
                                int checkAlienWin = game1.gameWinnerCheck(player1.getId(), idResAlien, player1.getLat(), player1.getLon(), latResAlien, lonResAlien);
                                Log.d("MYINT", "WinnerCheck: " + checkAlienWin);
                                if (checkAlienWin != -1){
                                    Log.d("MYINT", "AlienGameWinnerIS: " + checkAlienWin);
                                    game1.setActive(false);
                                    gameOverTextView.setVisibility(View.VISIBLE);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(alienReq);
    }
    //alien enemy location call continuous update for host-s-----------------------------------------

    public void onClick(View v) { //for json obj buttons
        switch (v.getId()) {
            case R.id.btnJsonObj:
                createNewGame();
                break;
            case R.id.btnJsonArray:
                //makeJsonArryReq();
                break;
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the connection to GoogleApiClient intact.  Here, we resume receiving location updates if the user has requested them
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        gameActive = true;//start game loop
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
        gameActive = false; //stop game loop
        mTapScreenTextAnimImgView.setImageResource(0); //reset animation image
        soundpool.autoPause();
        //soundpool = null; //reset soundpool
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        gameActive = false;//stop game loop
        mTapScreenTextAnimImgView.setImageResource(0); //reset animation image
        soundpool.autoPause();
        super.onStop();
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
                            Log.d(TAG, latString);
                            //Log.d(TAG, lonString);
                            updateAlienReq(latString, lonString);
                            Log.d(TAG, Double.toString(lat));
                            //Log.d(TAG, Double.toString(player1.getLat()));

                        }
                        else{ // human/host game
                            Log.d("MYSTR", "alienStatus: false");
                            lat = mCurrentLocation.getLatitude();
                            lon = mCurrentLocation.getLongitude();
                            player1.setLat(lat);
                            player1.setLon(lon);
                            String latString = Double.toString(player1.getLat());
                            String lonString = Double.toString(player1.getLon());
                            //Log.d(TAG, latString);
                            //Log.d(TAG, lonString);
                            //Log.d(TAG, Double.toString(lat));
                            //Log.d(TAG, Double.toString(player1.getLat()));
                            updateReq(latString, lonString);
                            alienArrayRequest(2);// !!! pass in real gameID here !!!
                        }
                    }//end run
                });
            }
        }
    }

}//close game engine activity
