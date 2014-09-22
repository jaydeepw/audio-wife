/***
 * The MIT License (MIT)

 * Copyright (c) 2014 Jaydeep

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nl.changer.audiowife;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

/***
 * A simple audio player wrapper for Android
 ***/
public class AudioWife {

	private static final String TAG = AudioWife.class.getSimpleName();

	/***
	 * Keep a single copy of this in memory unless required to create a new instance explicitly.
	 ****/
	private static AudioWife mAudioWife;

	/****
	 * Playback progress update time in milliseconds
	 ****/
	private static final int AUDIO_PROGRESS_UPDATE_TIME = 100;

	// TODO: externalize the error messages.
	private static final String ERROR_PLAYVIEW_NULL = "Play view cannot be null";
	private static final String ERROR_PLAYTIME_CURRENT_NEGATIVE = "Current playback time cannot be negative";
	private static final String ERROR_PLAYTIME_TOTAL_NEGATIVE = "Total playback time cannot be negative";

	private Handler mProgressUpdateHandler;

	private MediaPlayer mMediaPlayer;

	private SeekBar mSeekBar;
	private TextView mPlaybackTime;
	private View mPlayButton;
	private View mPauseButton;

	/***
	 * Indicates the current run-time of the audio being played
	 */
	private TextView mRunTime;

	/***
	 * Indicates the total duration of the audio being played.
	 */
	private TextView mTotalTime;

	/***
	 * Set if AudioWife is using the default UI provided with the library.
	 * **/
	private boolean mHasDefaultUi;

	/****
	 * Array to hold custom completion listeners
	 ****/
	private ArrayList<OnCompletionListener> mCompletionListeners = new ArrayList<MediaPlayer.OnCompletionListener>();

	private ArrayList<View.OnClickListener> mPlayListeners = new ArrayList<View.OnClickListener>();

	private ArrayList<View.OnClickListener> mPauseListeners = new ArrayList<View.OnClickListener>();

	/***
	 * Audio URI
	 ****/
	private static Uri mUri;

	public static AudioWife getInstance() {

		if (mAudioWife == null) {
			mAudioWife = new AudioWife();
		}

		return mAudioWife;
	}

	private Runnable mUpdateProgress = new Runnable() {

		public void run() {

			if (mSeekBar == null) {
				return;
			}

			if (mProgressUpdateHandler != null && mMediaPlayer.isPlaying()) {
				mSeekBar.setProgress((int) mMediaPlayer.getCurrentPosition());
				int currentTime = mMediaPlayer.getCurrentPosition();
				updatePlaytime(currentTime);
				updateRuntime(currentTime);
				// repeat the process
				mProgressUpdateHandler.postDelayed(this, AUDIO_PROGRESS_UPDATE_TIME);
			} else {
				// DO NOT update UI if the player is paused
			}
		}
	};

	/***
	 * Starts playing audio file associated. Before playing the audio, visibility of appropriate UI
	 * controls is made visible. Calling this method has no effect if the audio is already being
	 * played.
	 ****/
	public void play() {

		// if play button itself is null, the whole purpose of AudioWife is
		// defeated.
		if (mPlayButton == null) {
			throw new IllegalStateException(ERROR_PLAYVIEW_NULL);
		}

		if (mUri == null) {
			throw new IllegalStateException("Uri cannot be null. Call init() before calling this method");
		}

		if (mMediaPlayer == null) {
			throw new IllegalStateException("Call init() before calling this method");
		}

		if (mMediaPlayer.isPlaying()) {
			return;
		}

		mProgressUpdateHandler.postDelayed(mUpdateProgress, AUDIO_PROGRESS_UPDATE_TIME);

		// enable visibility of all UI controls.
		setViewsVisibility();

		mMediaPlayer.start();

		setPausable();
	}

	/**
	 * Ensure the views are visible before playing the audio.
	 */
	private void setViewsVisibility() {

		if (mSeekBar != null) {
			mSeekBar.setVisibility(View.VISIBLE);
		}

		if (mPlaybackTime != null) {
			mPlaybackTime.setVisibility(View.VISIBLE);
		}

		if (mRunTime != null) {
			mRunTime.setVisibility(View.VISIBLE);
		}

		if (mTotalTime != null) {
			mTotalTime.setVisibility(View.VISIBLE);
		}

		if (mPlayButton != null) {
			mPlayButton.setVisibility(View.VISIBLE);
		}

		if (mPauseButton != null) {
			mPauseButton.setVisibility(View.VISIBLE);
		}
	}

	/***
	 * Pause the audio being played. Calling this method has no effect if the audio is already
	 * paused
	 */
	public void pause() {

		if (mMediaPlayer == null) {
			return;
		}

		if (mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
			setPlayable();
		}
	}

	@Deprecated
	private void updatePlaytime(int currentTime) {

		if (mPlaybackTime == null) {
			return;
		}

		if (currentTime < 0) {
			throw new IllegalArgumentException(ERROR_PLAYTIME_CURRENT_NEGATIVE);
		}

		StringBuilder playbackStr = new StringBuilder();

		// set the current time
		// its ok to show 00:00 in the UI
		playbackStr.append(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) currentTime), TimeUnit.MILLISECONDS.toSeconds((long) currentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) currentTime))));

		playbackStr.append("/");

		// show total duration.
		long totalDuration = 0;

		if (mMediaPlayer != null) {
			try {
				totalDuration = mMediaPlayer.getDuration();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// set total time as the audio is being played
		if (totalDuration != 0) {
			playbackStr.append(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) totalDuration), TimeUnit.MILLISECONDS.toSeconds((long) totalDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) totalDuration))));
		} else {
			Log.w(TAG, "Something strage this audio track duration in zero");
		}

		mPlaybackTime.setText(playbackStr);

		// DebugLog.i(currentTime + " / " + totalDuration);
	}

	private void updateRuntime(int currentTime) {

		if (mRunTime == null) {
			// this view can be null if the user
			// does not want to use it. Don't throw
			// an exception.
			return;
		}

		if (currentTime < 0) {
			throw new IllegalArgumentException(ERROR_PLAYTIME_CURRENT_NEGATIVE);
		}

		StringBuilder playbackStr = new StringBuilder();

		// set the current time
		// its ok to show 00:00 in the UI
		playbackStr.append(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) currentTime), TimeUnit.MILLISECONDS.toSeconds((long) currentTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) currentTime))));

		mRunTime.setText(playbackStr);

		// DebugLog.i(currentTime + " / " + totalDuration);
	}

	private void setTotalTime() {

		if (mTotalTime == null) {
			// this view can be null if the user
			// does not want to use it. Don't throw
			// an exception.
			return;
		}

		StringBuilder playbackStr = new StringBuilder();
		long totalDuration = 0;

		// by this point the media player is brought to ready state
		// by the call to init().
		if (mMediaPlayer != null) {
			try {
				totalDuration = mMediaPlayer.getDuration();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (totalDuration < 0) {
			throw new IllegalArgumentException(ERROR_PLAYTIME_TOTAL_NEGATIVE);
		}

		// set total time as the audio is being played
		if (totalDuration != 0) {
			playbackStr.append(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) totalDuration), TimeUnit.MILLISECONDS.toSeconds((long) totalDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) totalDuration))));
		}

		mTotalTime.setText(playbackStr);
	}

	/***
	 * Changes audiowife state to enable play functionality.
	 */
	private void setPlayable() {
		if (mPlayButton != null) {
			mPlayButton.setVisibility(View.VISIBLE);
		}

		if (mPauseButton != null) {
			mPauseButton.setVisibility(View.GONE);
		}
	}

	/****
	 * Changes audio wife to enable pause functionality.
	 */
	private void setPausable() {
		if (mPlayButton != null) {
			mPlayButton.setVisibility(View.GONE);
		}

		if (mPauseButton != null) {
			mPauseButton.setVisibility(View.VISIBLE);
		}
	}

	/***
	 * Initialize the audio player. This method should be the first one to be called before starting
	 * to play audio using {@link AudioWife}
	 * 
	 * @param ctx
	 *            {@link Activity} Context
	 * @param uri
	 *            Uri of the audio to be played.
	 ****/
	public AudioWife init(Context ctx, Uri uri) {

		if (uri == null) {
			throw new IllegalArgumentException("Uri cannot be null");
		}

		if (mAudioWife == null) {
			mAudioWife = new AudioWife();
		}

		mUri = uri;

		mProgressUpdateHandler = new Handler();

		initPlayer(ctx);

		return this;
	}

	/***
	 * Sets the audio play functionality on click event of this view. You can set {@link Button} or
	 * an {@link ImageView} as audio play control
	 * 
	 * @see AudioWife#addOnPauseClickListener(android.view.View.OnClickListener)
	 ****/
	public AudioWife setPlayView(View play) {

		if (play == null) {
			throw new NullPointerException("PlayView cannot be null");
		}

		if (mHasDefaultUi) {
			Log.w(TAG, "Already using default UI. Setting play view will have no effect");
			return this;
		}

		mPlayButton = play;

		initOnPlayClick();
		return this;
	}

	private void initOnPlayClick() {
		if (mPlayButton == null) {
			throw new NullPointerException(ERROR_PLAYVIEW_NULL);
		}

		// add default click listener to the top
		// so that it is the one that gets fired first
		mPlayListeners.add(0, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				play();
			}
		});

		// Fire all the attached listeners
		// when the play button is clicked
		mPlayButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				for (View.OnClickListener listener : mPlayListeners) {
					listener.onClick(v);
				}
			}
		});
	}

	/***
	 * Sets the audio pause functionality on click event of the view passed in as a parameter. You
	 * can set {@link Button} or an {@link ImageView} as audio pause control. Audio pause
	 * functionality will be unavailable if this method is not called.
	 * 
	 * @see AudioWife#addOnPauseClickListener(android.view.View.OnClickListener)
	 ****/
	public AudioWife setPauseView(View pause) {

		if (pause == null) {
			throw new NullPointerException("PauseView cannot be null");
		}

		if (mHasDefaultUi) {
			Log.w(TAG, "Already using default UI. Setting pause view will have no effect");
			return this;
		}

		mPauseButton = pause;

		initOnPauseClick();
		return this;
	}

	private void initOnPauseClick() {
		if (mPauseButton == null) {
			throw new NullPointerException("Pause view cannot be null");
		}

		// add default click listener to the top
		// so that it is the one that gets fired first
		mPauseListeners.add(0, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				pause();
			}
		});

		// Fire all the attached listeners
		// when the pause button is clicked
		mPauseButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				for (View.OnClickListener listener : mPauseListeners) {
					listener.onClick(v);
				}
			}
		});
	}

	/***
	 * @deprecated Use {@link AudioWife#setRuntimeView(TextView)} and
	 *             {@link AudioWife#setTotalTimeView(TextView)} instead. <br/>
	 *             Sets current and total playback time. Use this if you have a playback time
	 *             counter in the UI.
	 ****/
	public AudioWife setPlaytime(TextView playTime) {

		if (mHasDefaultUi) {
			Log.w(TAG, "Already using default UI. Setting play time will have no effect");
			return this;
		}

		mPlaybackTime = playTime;

		// initialize the playtime to 0
		updatePlaytime(0);
		return this;
	}

	/***
	 * Sets current playback time view. Use this if you have a playback time counter in the UI.
	 * 
	 * @see AudioWife#setTotalTimeView(TextView)
	 ****/
	public AudioWife setRuntimeView(TextView currentTime) {

		if (mHasDefaultUi) {
			Log.w(TAG, "Already using default UI. Setting play time will have no effect");
			return this;
		}

		mRunTime = currentTime;

		// initialize the playtime to 0
		updateRuntime(0);
		return this;
	}

	/***
	 * Sets the total playback time view. Use this if you have a playback time counter in the UI.
	 * 
	 * @see AudioWife#setRuntimeView(TextView)
	 ****/
	public AudioWife setTotalTimeView(TextView totalTime) {

		if (mHasDefaultUi) {
			Log.w(TAG, "Already using default UI. Setting play time will have no effect");
			return this;
		}

		mTotalTime = totalTime;

		setTotalTime();
		return this;
	}

	public AudioWife setSeekBar(SeekBar seekbar) {

		if (mHasDefaultUi) {
			Log.w(TAG, "Already using default UI. Setting seek bar will have no effect");
			return this;
		}

		mSeekBar = seekbar;
		initMediaSeekBar();
		return this;
	}

	/****
	 * Add custom playback completion listener. Adding multiple listeners will queue up all the
	 * listeners and fire them on media playback completes.
	 */
	public AudioWife addOnCompletionListener(MediaPlayer.OnCompletionListener listener) {

		// add default click listener to the top
		// so that it is the one that gets fired first
		mCompletionListeners.add(0, listener);

		return this;
	}

	/****
	 * Add custom play view click listener. Calling this method multiple times will queue up all the
	 * listeners and fire them all together when the event occurs.
	 ***/
	public AudioWife addOnPlayClickListener(View.OnClickListener listener) {

		mPlayListeners.add(listener);

		return this;
	}

	/***
	 * Add custom pause view click listener. Calling this method multiple times will queue up all
	 * the listeners and fire them all together when the event occurs.
	 ***/
	public AudioWife addOnPauseClickListener(View.OnClickListener listener) {

		mPauseListeners.add(listener);

		return this;
	}

	/****
	 * Initialize and prepare the audio player
	 ****/
	private void initPlayer(Context ctx) {

		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

		try {
			mMediaPlayer.setDataSource(ctx, mUri);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			mMediaPlayer.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		mMediaPlayer.setOnCompletionListener(mOnCompletion);
	}

	private MediaPlayer.OnCompletionListener mOnCompletion = new MediaPlayer.OnCompletionListener() {

		@Override
		public void onCompletion(MediaPlayer mp) {
			// set UI when audio finished playing
			int currentPlayTime = 0;
			mSeekBar.setProgress((int) currentPlayTime);
			updatePlaytime(currentPlayTime);
			updateRuntime(currentPlayTime);
			setPlayable();
			// ensure that our completion listener fires first.
			// This will provide the developer to over-ride our
			// completion listener functionality

			fireCustomCompletionListeners(mp);
		}
	};

	private void initMediaSeekBar() {

		if (mSeekBar == null)
			return;

		// update seekbar
		long finalTime = mMediaPlayer.getDuration();
		mSeekBar.setMax((int) finalTime);

		mSeekBar.setProgress(0);

		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mMediaPlayer.seekTo(seekBar.getProgress());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

			}
		});
	}

	private void fireCustomCompletionListeners(MediaPlayer mp) {
		for (OnCompletionListener listener : mCompletionListeners) {
			listener.onCompletion(mp);
		}
	}

	/****
	 * Sets the default audio player UI as a child of the parameter container view.
	 * 
	 * <br/>
	 * This is the simplest way to get AudioWife working for you. If you are using the default
	 * player provided by this method, calling method {@link AudioWife#setPlayView(View)},
	 * {@link AudioWife#setPauseView(View)}, {@link AudioWife#setSeekBar(SeekBar)},
	 * {@link AudioWife#setPlaytime(TextView)} will have no effect. <br/>
	 * <br/>
	 * The default player UI consists of:
	 * 
	 * <ul>
	 * <li>Play view</li>
	 * <li>Pause view</li>
	 * <li>Seekbar</li>
	 * <li>Playtime</li>
	 * <ul>
	 * <br/>
	 * 
	 * @param playerContainer
	 *            View to integrate default player UI into.
	 ****/
	public AudioWife useDefaultUi(ViewGroup playerContainer, LayoutInflater inflater) {
		if (playerContainer == null)
			throw new NullPointerException("Player container cannot be null");

		if (inflater == null)
			throw new NullPointerException("Inflater cannot be null");

		View playerUi = inflater.inflate(R.layout.aw_player, playerContainer);

		// init play view
		View playView = playerUi.findViewById(R.id.play);
		setPlayView(playView);

		// init pause view
		View pauseView = playerUi.findViewById(R.id.pause);
		setPauseView(pauseView);

		// init seekbar
		SeekBar seekBar = (SeekBar) playerUi.findViewById(R.id.media_seekbar);
		setSeekBar(seekBar);

		// init playback time view
		TextView playbackTime = (TextView) playerUi.findViewById(R.id.playback_time);
		setPlaytime(playbackTime);

		// this has to be set after all the views
		// have finished initializing.
		mHasDefaultUi = true;
		return this;
	}

	/***
	 * Releases the allocated resources.
	 * 
	 * <p>
	 * Call {@link #init(Context, Uri, SeekBar, View, View, TextView)} before calling
	 * {@link #play()}
	 * </p>
	 * */
	public void release() {

		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mProgressUpdateHandler = null;
		}
	}
}
