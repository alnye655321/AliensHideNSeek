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
    public final static String ALIEN_PLAYER = "alienStatus";
    public final static String playerType = "alien";
//need a way to set a gameId here --> pull from a get request of open games and generate join buttons

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);

        Intent intent = getIntent();
        handleMessage = intent.getStringExtra(MainActivity.HANDLE_MESSAGE);
        taglineMessage = intent.getStringExtra(MainActivity.TAGLINE_MESSAGE);
        Log.d("tagger", handleMessage);
        Log.d("tagger", taglineMessage);

    }//close onCreate()

    public void joinGame(View view){
        //Log.d(TAG, EXTRA_MESSAGE);
        Intent gameEngineStart = new Intent(this, GameEngineActivity.class);
        gameEngineStart.putExtra(TAGLINE_MESSAGE, taglineMessage);
        gameEngineStart.putExtra(HANDLE_MESSAGE, handleMessage);
        gameEngineStart.putExtra(ALIEN_PLAYER, playerType);
        startActivity(gameEngineStart);
    }

}// close class
