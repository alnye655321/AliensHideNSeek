package info.androidhive.AliensHideNSeek;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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

import java.util.HashMap;
import java.util.Map;
import java.text.DateFormat;
import java.util.Date;

import info.androidhive.AliensHideNSeek.app.AppController;
import info.androidhive.AliensHideNSeek.utils.Const;

public class GameEngineActivity extends Activity implements OnClickListener, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
    private boolean alien = false; //determine which game engine to run based on previous activity - alien || human player
    Intent alienIntent = getIntent(); //figure out if we are dealing with a alien or human player
    String alienStatus = alienIntent.getStringExtra(JoinGameActivity.ALIEN_PLAYER);

    Human player1 = new Human("Military","Colonel Hicks","Kickass",0,0,0,0);
    Alien player2 = new Alien("Crawler","Xenomorph","Humans R Tasty",0,0,0,0);
    Environment game1 = new Environment("Default",300000,8); // 300000ms = 5mins !!! 8 = max players --> should be user set
    private boolean gameActive = true; //!!! active game state - controls engine loop at bottom !!!
    public Handler handler;
    public ProgressBar progressBar; //no longer using from thread example
    public double lat; //local gps update variables, used in game engine thread
    public double lon;
//location settings---------------------------------------------------------------------------------
//protected static final String TAG = "location-updates-sample";

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    // UI Widgets.
    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;
    protected TextView mLastUpdateTimeTextView;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;

    // Labels.
    protected String mLatitudeLabel;
    protected String mLongitudeLabel;
    protected String mLastUpdateTimeLabel;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;
//end location settings-----------------------------------------------------------------------------

    //private String TAG = JsonRequestActivity.class.getSimpleName();
    private String TAG = "tagger";
    private int TAGINT = 1;
    private Button btnJsonObj;
    private TextView msgResponse;
    //private ProgressDialog pDialog;

    // These tags will be used to cancel the requests
    private String tag_json_obj = "jobj_req", tag_json_arry = "jarray_req";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_engine);

        handler = new Handler(); //for new thread
        progressBar = (ProgressBar) findViewById(R.id.progressBar1); //for new thread

        Intent intent = getIntent();

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

        if(alienStatus != "alien") {
            createNewGame(); //only run as human !!! test
        }
        //location settings------------------------------------------------------------------------
        // Locate the UI widgets.
        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);

        // Set labels.
        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_time_label);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();
        //close location settings------------------------------------------------------------------
    } //close on create

    //new test thread start
    public void startProgress(View view) { //currently executed on xml button click
        new Thread(new Engine()).start();
    }
    //close new test thread

    //location methods------------------------------------------------------------------------------
    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
                setButtonsEnabledState();
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Handles the Start Updates button and requests start of location updates. Does nothing if
     * updates have already been requested.
     */
    public void startUpdatesButtonHandler(View view) {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            setButtonsEnabledState();
            startLocationUpdates();
        }
    }

    /**
     * Handles the Stop Updates button, and requests removal of location updates. Does nothing if
     * updates were not previously requested.
     */
    public void stopUpdatesButtonHandler(View view) {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            setButtonsEnabledState();
            stopLocationUpdates();
        }
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }

    /**
     * Ensures that only one button is enabled at any time. The Start Updates button is enabled
     * if the user is not requesting location updates. The Stop Updates button is enabled if the
     * user is requesting location updates.
     */
    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }

    /**
     * Updates the latitude, the longitude, and the last location time in the UI.
     */
    private void updateUI() {
        mLatitudeTextView.setText(String.format("%s: %f", mLatitudeLabel,
                mCurrentLocation.getLatitude()));
        mLongitudeTextView.setText(String.format("%s: %f", mLongitudeLabel,
                mCurrentLocation.getLongitude()));
        mLastUpdateTimeTextView.setText(String.format("%s: %s", mLastUpdateTimeLabel,
                mLastUpdateTime));

//        String playaName = player1.getName();
//        Log.i(TAG, playaName);
        //Log.i(TAG, "Handler Update Init");

    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

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

    //JSON POST Req - Alien Continuous Update-------------------------------------------------------
    private void updateAlienReq(String lat, String lon) {

        Intent intent = getIntent(); //get alien submitted values from previous activity --> JoinGameActivity
        String handleMessage = intent.getStringExtra(JoinGameActivity.HANDLE_MESSAGE);
        String taglineMessage = intent.getStringExtra(JoinGameActivity.TAGLINE_MESSAGE);
        String tag_json_obj = "json_obj_req";

        String url = "http://node.nyedigital.com/update/alien";

        Map<String, String> params = new HashMap();// object payload values
        params.put("id", "2");
        params.put("lat", lat);
        params.put("lon", lon);
        params.put("checkStart", player2.getCheckStart());
        params.put("gameId", "2");
        params.put("handle", handleMessage);
        params.put("tagline", taglineMessage);

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
                                player2.setCheckStart("false");
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
                            // Parsing json array response
                            // loop through each json object
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

                                //check if alien has captured human - ie. occupy same gps location --> Game Over
                                int checkAlienWin = game1.gameWinnerCheck(player1.getId(), idResAlien, player1.getLat(), player1.getLon(), latResAlien, lonResAlien);
                                Log.d("MYINT", "WinnerCheck: " + checkAlienWin);
                                if (checkAlienWin != -1){
                                    Log.d("MYINT", "AlienGameWinnerIS: " + checkAlienWin);
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
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
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

    /**
     * Callback that fires when the location changes.
     */
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
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    /**
     * Stores activity data in the Bundle.
     */
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

                        if(alienStatus == "alien") { //alien game
                            lat = mCurrentLocation.getLatitude();
                            lon = mCurrentLocation.getLongitude();
                            player2.setLat(lat);
                            player2.setLon(lon);
                            String latString = Double.toString(player2.getLat());
                            String lonString = Double.toString(player2.getLon());
                            Log.d(TAG, latString);
                            Log.d(TAG, lonString);
                            //Log.d(TAG, Double.toString(lat));
                            //Log.d(TAG, Double.toString(player1.getLat()));

                        }
                        else{ // human/host game
                            lat = mCurrentLocation.getLatitude();
                            lon = mCurrentLocation.getLongitude();
                            player1.setLat(lat);
                            player1.setLon(lon);
                            String latString = Double.toString(player1.getLat());
                            String lonString = Double.toString(player1.getLon());
                            Log.d(TAG, latString);
                            Log.d(TAG, lonString);
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
