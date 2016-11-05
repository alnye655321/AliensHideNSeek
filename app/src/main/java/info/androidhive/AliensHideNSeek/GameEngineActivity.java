package info.androidhive.AliensHideNSeek;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

public class GameEngineActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_engine);

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
    }
}
