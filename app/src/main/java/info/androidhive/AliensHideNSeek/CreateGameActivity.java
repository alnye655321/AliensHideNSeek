package info.androidhive.AliensHideNSeek;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

public class CreateGameActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);

        Intent intent = getIntent();
        String handleMessage = intent.getStringExtra(MainActivity.HANDLE_MESSAGE);
        String taglineMessage = intent.getStringExtra(MainActivity.TAGLINE_MESSAGE);

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


}
