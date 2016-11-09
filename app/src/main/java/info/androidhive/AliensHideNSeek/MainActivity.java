package info.androidhive.AliensHideNSeek;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity implements OnClickListener {
	public final static String HANDLE_MESSAGE = "handle";
	public final static String TAGLINE_MESSAGE = "tagline";

	private Button btnJson, btnString, btnImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btnString = (Button) findViewById(R.id.btnStringRequest);
		btnJson = (Button) findViewById(R.id.btnJsonRequest);

		// button click listeners
		btnString.setOnClickListener(this);
		btnJson.setOnClickListener(this);
	}

	public void createGame(View view){
		//Log.d(TAG, EXTRA_MESSAGE);
		Intent intent = new Intent(this, CreateGameActivity.class);
		EditText handleText = (EditText) findViewById(R.id.handle);
		String handleMessage = handleText.getText().toString();
		EditText taglineText = (EditText) findViewById(R.id.tagline);
		String taglineMessage = taglineText.getText().toString();
		intent.putExtra(TAGLINE_MESSAGE, taglineMessage);
		intent.putExtra(HANDLE_MESSAGE, handleMessage);
		startActivity(intent);
	}
	public void joinGame(View view){
		//Log.d(TAG, EXTRA_MESSAGE);
		Intent intent = new Intent(this, JoinGameActivity.class);
		EditText handleText = (EditText) findViewById(R.id.handle);
		String handleMessage = handleText.getText().toString();
		EditText taglineText = (EditText) findViewById(R.id.tagline);
		String taglineMessage = taglineText.getText().toString();
		intent.putExtra(TAGLINE_MESSAGE, taglineMessage);
		intent.putExtra(HANDLE_MESSAGE, handleMessage);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnStringRequest:
			startActivity(new Intent(MainActivity.this,
					StringRequestActivity.class));
			break;
		case R.id.btnJsonRequest:
			startActivity(new Intent(MainActivity.this,
					JsonRequestActivity.class));
			break;

		default:
			break;
		}
	}

}
