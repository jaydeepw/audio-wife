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
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class DefaultPlayerActivity extends FragmentActivity {

	private static final String TAG = DefaultPlayerActivity.class.getSimpleName();

	private static final int INTENT_PICK_AUDIO = 1;

	private Context mContext;
	private ViewGroup mPlayerContainer;

	private Uri mUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.default_player);
		mContext = DefaultPlayerActivity.this;
		
		View pickAudio = findViewById(R.id.pickAudio);

		pickAudio.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				pickAudio();
			}
		});

		// this will act as container for default
		// audio player UI.
		mPlayerContainer = (ViewGroup) findViewById(R.id.player_layout);
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

				// mPlayerContainer = View to integrate default player UI into.
				AudioWife.getInstance().init(mContext, uri)
						.useDefaultUi(mPlayerContainer, getLayoutInflater());
				
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
}
