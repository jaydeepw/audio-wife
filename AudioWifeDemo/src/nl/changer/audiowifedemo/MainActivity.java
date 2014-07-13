package nl.changer.audiowifedemo;

import nl.changer.audiowife.AudioWife;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private static final int INTENT_PICK_AUDIO = 1;
	
	private Context mContext;

	private View mPlayMedia;
	private View mPauseMedia;
	private SeekBar mMediaSeekBar;
	private TextView mPlaybackTime;

	private Uri mUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mContext = MainActivity.this;
		
		View pickAudio = findViewById(R.id.pickAudio);

		pickAudio.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				pickAudio();
			}
		});

		// initialize the player controls
		mPlayMedia = findViewById(R.id.play);
		mPauseMedia = findViewById(R.id.pause);
		mMediaSeekBar = (SeekBar) findViewById(R.id.media_seekbar);
		mPlaybackTime = (TextView) findViewById(R.id.playback_time);

		mPlayMedia.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (mUri == null)
					Toast.makeText(mContext,
							"Pick an audio file before playing",
							Toast.LENGTH_LONG).show();
			}
		});
	}

	private void pickAudio() {
		Intent mediaChooser = new Intent(Intent.ACTION_GET_CONTENT);
		// comma-separated MIME types
		mediaChooser.setType("audio/*");
		startActivityForResult(mediaChooser, INTENT_PICK_AUDIO);
	}

	@Override
	protected void onActivityResult(int requestCode, int resuleCode,
			Intent intent) {
		super.onActivityResult(requestCode, resuleCode, intent);

		if (resuleCode == Activity.RESULT_OK) {
			if (requestCode == INTENT_PICK_AUDIO) {
				Uri uri = intent.getData();

				mUri = uri;

				AudioWife.getInstance().init(mContext, uri)
						.setPlayView(mPlayMedia)		// AudioWife takes care of click handler for play button
						.setPauseView(mPauseMedia)		// AudioWife takes care of click handler for pause button
						.setSeekBar(mMediaSeekBar)
						.setPlaytime(mPlaybackTime);
				
				AudioWife.getInstance().addOnCompletionListener( new MediaPlayer.OnCompletionListener() {
					
					@Override
					public void onCompletion(MediaPlayer mp) {
						Toast.makeText(getBaseContext(), "Completed", Toast.LENGTH_SHORT)
							 .show();
						// do you stuff.
					}
				});
				
				AudioWife.getInstance().addOnPlayClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Toast.makeText(getBaseContext(), "Play", Toast.LENGTH_SHORT)
							 .show();
						// get-set-go. Lets dance.
					}
				});
				
				AudioWife.getInstance().addOnPauseClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Toast.makeText(getBaseContext(), "Pause", Toast.LENGTH_SHORT)
							 .show();
						// Your on audio pause stuff.
					}
				});
			}
		} else {
			Log.w(TAG, "Audio file not picked up");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		// when done playing, release the resources
		AudioWife.getInstance().release();
		mUri = null;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.default_player:
			Intent i = new Intent(MainActivity.this, DefaultPlayerActivity.class);
			startActivity(i);
			break;

		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
}
