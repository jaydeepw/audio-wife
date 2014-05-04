package nl.changer.audiowifedemo;

import nl.changer.audiowife.AudioWife;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private static final int INTENT_PICK_AUDIO = 1;

	private View mPlayMedia;
	private View mPauseMedia;
	private SeekBar mMediaSeekBar;
	private TextView mPlaybackTime;

	private Uri mUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		View pickAudio = findViewById(R.id.pickAudio);

		pickAudio.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				pickAudio();
			}
		});

		// initialize the player contols
		mPlayMedia = findViewById(R.id.play);
		mPauseMedia = findViewById(R.id.pause);
		mMediaSeekBar = (SeekBar) findViewById(R.id.mediaSeekBar);
		mPlaybackTime = (TextView) findViewById(R.id.playback_time);

		mPlayMedia.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				try {
					AudioWife.getInstance().play();
				} catch (IllegalStateException e) {
					Toast.makeText(MainActivity.this,
							"Pick an audio file before playing",
							Toast.LENGTH_LONG).show();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

		mPauseMedia.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				AudioWife.getInstance().pause();
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

				AudioWife.getInstance().init(MainActivity.this, uri)
						.setPlayView(mPlayMedia).setPauseView(mPauseMedia)
						.setSeekBar(mMediaSeekBar).setPlaytime(mPlaybackTime)
						.play();
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
	}
}
