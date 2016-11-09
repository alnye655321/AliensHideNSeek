package info.androidhive.AliensHideNSeek;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class JoinGameActivity extends Activity {

    private String handleMessage;
    private String taglineMessage;
    public final static String HANDLE_MESSAGE = "handle";
    public final static String TAGLINE_MESSAGE = "tagline";
    public final static String GAME_MESSAGE = "gameMessage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);

        Intent intent = getIntent();
        handleMessage = intent.getStringExtra(MainActivity.HANDLE_MESSAGE);
        taglineMessage = intent.getStringExtra(MainActivity.TAGLINE_MESSAGE);

        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(handleMessage); //text views not working in linear layout
        Log.d("tagger", handleMessage);

        TextView textView1 = new TextView(this);//text views not working in linear layout
        textView1.setTextSize(40);
        textView1.setText(taglineMessage);
        Log.d("tagger", taglineMessage);

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_join_game);
        layout.addView(textView);
        layout.addView(textView1);

    }//close onCreate()

}// close class
