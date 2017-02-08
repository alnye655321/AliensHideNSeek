package info.androidhive.AliensHideNSeek;
import android.util.Log;

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

import info.androidhive.AliensHideNSeek.app.AppController;

public class Alien extends Player {
    private String type;
    private double lat;
    private double lon;
    private double humanLatRes;
    private double humanLonRes;
    private double distance;

    // alien constructor
    Alien(String type, String name, String tagline, double playerStartLat, double playerStartLon, double lat, double lon) {
        super(name, tagline, playerStartLat, playerStartLon, lat, lon);
        this.type = type;
    }

    public double getHumanLatRes() {
        return humanLatRes;
    }

    public void setHumanLatRes(double humanLatRes) {
        this.humanLatRes = humanLatRes;
    }

    public double getHumanLonRes() {
        return humanLonRes;
    }

    public void setHumanLonRes(double humanLonRes) {
        this.humanLonRes = humanLonRes;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    //JSON POST Req - Create Alien Player
    public void createNewAlien(String handleMessage, String taglineMessage) {

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
                            setGameId(gameId); //define player properties from server response
                            setPlayerId(playerId);
                            Log.d("MYINT", "AlienPlayerId Value: " + playerId);

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


    //JSON POST Req - Alien Continuous Update
    public void updateAlienReq(String lat, String lon, int gameID) {

        String tag_json_obj = "json_obj_req";

        String url = "http://node.nyedigital.com/update/alien";
        String gameIdString = Integer.toString(gameID);

        Map<String, String> params = new HashMap();// object payload values
        params.put("id", "2");
        params.put("lat", lat);
        params.put("lon", lon);
        params.put("checkStart", getCheckStart());
        params.put("gameId", gameIdString);

        JSONObject parameters = new JSONObject(params);//create object payload

        JsonObjectRequest updateObjReq = new JsonObjectRequest(Request.Method.POST,
                url, parameters,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.d(TAG, response.toString());
                        //Log.d(TAG, response.toString());

                        try {
                            // Parsing json object response
                            // response will be a json object
                            String startStatus = response.getString("startStatus"); //set response values
                            String updateStatus = response.getString("updateStatus");
                            humanLatRes = response.getDouble("humanLat");
                            humanLonRes = response.getDouble("humanLon");
                            if (startStatus == "complete"){ //check for a complete response, then set object to false to stop sending request !!! refactor to using private boolean
                                setCheckStart("false");
                            }

                            double tempDistance = calcDistance(humanLatRes, humanLonRes, getLat(), getLon()); //get distance from human
                            distance = tempDistance * 1000; //update distance to send to display

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //VolleyLog.d(TAG, "Error: " + error.getMessage());
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
    } //close JSON POST Req



}