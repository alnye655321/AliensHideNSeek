package info.androidhive.AliensHideNSeek;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.view.View.OnClickListener;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import info.androidhive.AliensHideNSeek.app.AppController;
import info.androidhive.AliensHideNSeek.utils.Const;

public class GameEngineActivity extends Activity implements OnClickListener {

    //private String TAG = JsonRequestActivity.class.getSimpleName();
    private String TAG = "tagger";
    private Button btnJsonObj;
    private TextView msgResponse;
    //private ProgressDialog pDialog;

    // These tags will be used to cancel the requests
    private String tag_json_obj = "jobj_req", tag_json_arry = "jarray_req";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_engine);

        btnJsonObj = (Button) findViewById(R.id.btnJsonObj);
        btnJsonObj.setOnClickListener(this);


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
        makeJsonObjReq();
    }

    /**
     * Making json object request
     * */
    private void makeJsonObjReq() {
// Tag used to cancel the request
        Intent intent = getIntent();
        String gameMessage = intent.getStringExtra(CreateGameActivity.GAME_MESSAGE);
        String handleMessage = intent.getStringExtra(CreateGameActivity.HANDLE_MESSAGE);
        String taglineMessage = intent.getStringExtra(CreateGameActivity.TAGLINE_MESSAGE);
        String tag_json_obj = "json_obj_req";

        String url = "http://node.nyedigital.com/game";

        Map<String, String> params = new HashMap();// object values
        params.put("name", gameMessage);
        params.put("handle", handleMessage);
        params.put("tagline", taglineMessage);

        JSONObject parameters = new JSONObject(params);

        //ProgressDialog pDialog = new ProgressDialog(this);
        //pDialog.setMessage("Loading...");
        //pDialog.show();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, parameters,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        //pDialog.hide();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                //pDialog.hide();
            }
        }) {

//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("name", "Androidhive");
//                //params.put("email", "abc@androidhive.info");
//                //params.put("password", "password123");
//
//                return params;
//            }

        };

// Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnJsonObj:
                makeJsonObjReq();
                break;
            case R.id.btnJsonArray:
                //makeJsonArryReq();
                break;
        }

    }

}
