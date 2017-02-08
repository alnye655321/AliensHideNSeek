package info.androidhive.AliensHideNSeek;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import info.androidhive.AliensHideNSeek.app.AppController;

public class Human extends Player {

    private String type;
    private double lat;
    private double lon;

    private double distance = 66666;
    private String[] alienLocations = new String[10]; //should set to user defined game size !!!
    int checkAlienWin = -1;

    // human constructor
    Human(String type, String name, String tagline, double playerStartLat, double playerStartLon, double lat, double lon) {
        super(name, tagline, playerStartLat, playerStartLon, lat, lon);
        this.type = type;
    }

    public String[] getAlienLocations() {
        return alienLocations;
    }

    public void setAlienLocations(String[] alienLocations) {
        this.alienLocations = alienLocations;
    }

    public int getCheckAlienWin() {
        return checkAlienWin;
    }

    public void setCheckAlienWin(int checkAlienWin) {
        this.checkAlienWin = checkAlienWin;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    //JSON POST Req - Create New Game with Host Player
    public void createNewGame(String gameMessage, String handleMessage, String taglineMessage) {

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
                            //Log.d("MYINT", "value: " + gameId);

                            setGameId(gameId); //define player properties from server response
                            setPlayerId(playerId);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error", "Error: " + error.getMessage());
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
    } //close JSON POST Req

    //JSON POST Req - Host Continuous Update
    public void updateReq(String lat, String lon) {

        String tag_json_obj = "update_json_obj_req";

        String url = "http://node.nyedigital.com/update/human";

        Map<String, String> params = new HashMap();// object payload values
        params.put("id", "3");
        params.put("lat", lat);
        params.put("lon", lon);
        params.put("checkStart", getCheckStart());
        params.put("gameId", "2");

        JSONObject parameters = new JSONObject(params);//create object payload

        JsonObjectRequest updateObjReq = new JsonObjectRequest(Request.Method.POST,
                url, parameters,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            // Parsing json object response
                            // response will be a json object
                            String startStatus = response.getString("startStatus"); //set response values
                            String updateStatus = response.getString("updateStatus");

                            if (startStatus == "complete"){ //check for a complete response, then set object to false to stop sending request !!! refactor to using private boolean
                                setCheckStart("false");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error", "Error: " + error.getMessage());
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
    } //JSON POST Req - Human Continuous Update


    //alien enemy location call continuous update - for host
    public void alienArrayRequest(int gameId) {

        String urlJsonArry = "http://node.nyedigital.com/aliens/" + gameId;


        JsonArrayRequest alienReq = new JsonArrayRequest(urlJsonArry,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("TAGme", response.toString());

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

                                //add to alienLocation array for circle draws
                                alienLocations[i] = Double.toString(latResAlien) + "," + Double.toString(lonResAlien);

                                double tempDistance = calcDistance(getLat(), getLon(), latResAlien, lonResAlien); //get distance from alien
                                if(tempDistance < distance && tempDistance != 0){
                                    distance = tempDistance * 1000;

                                    //check if alien has captured human - ie. occupy same gps location --> Game Over
                                    checkAlienWin = gameWinnerCheck(getId(), idResAlien, getLat(), getLon(), latResAlien, lonResAlien);
                                    Log.d("MYINT", "WinnerCheck: " + checkAlienWin);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error", "Error: " + error.getMessage());
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(alienReq);
    } // close request

}