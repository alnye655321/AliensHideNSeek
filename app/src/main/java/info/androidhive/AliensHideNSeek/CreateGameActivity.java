package info.androidhive.AliensHideNSeek;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CreateGameActivity extends Activity {
    private String handleMessage;
    private String taglineMessage;
    public final static String HANDLE_MESSAGE = "handle";
    public final static String TAGLINE_MESSAGE = "tagline";
    public final static String GAME_MESSAGE = "gameMessage";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);

        Intent intent = getIntent();
        handleMessage = intent.getStringExtra(MainActivity.HANDLE_MESSAGE);
        taglineMessage = intent.getStringExtra(MainActivity.TAGLINE_MESSAGE);

        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(handleMessage);

        TextView textView1 = new TextView(this);
        textView1.setTextSize(40);
        textView1.setText(taglineMessage);

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_create_game);
        layout.addView(textView);
        layout.addView(textView1);

    }

    public void createGame(View view){
        //Log.d(TAG, EXTRA_MESSAGE);
        Intent gameEngineStart = new Intent(this, GameEngineActivity.class);
        EditText gameText = (EditText) findViewById(R.id.gamename);
        String gameMessage = gameText.getText().toString();
        gameEngineStart.putExtra(GAME_MESSAGE, gameMessage);
        gameEngineStart.putExtra(TAGLINE_MESSAGE, taglineMessage);
        gameEngineStart.putExtra(HANDLE_MESSAGE, handleMessage);
        startActivity(gameEngineStart);
    }


}
