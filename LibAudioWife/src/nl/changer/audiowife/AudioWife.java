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
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

/***
 * A simple audio player wrapper for Android
 ***/
public class AudioWife {

	private static final String TAG = AudioWife.class.getSimpleName();

	/****
	 * Playback progress update time in milliseconds
	 ****/
	private static final int AUDIO_PROGRESS_UPDATE_TIME = 100;

	private static Handler mHandler;

	private static MediaPlayer mMediaPlayer;

	private static SeekBar mSeekBar;
	private static TextView mPlaybackTime;
	private static View mPlayButton;
	private static View mPauseButton;

	/***
	 * Audio URI
	 ****/
	private static Uri mUri;

	private static Runnable mUpdateProgress = new Runnable() {
		public void run() {

			if (mHandler != null && mMediaPlayer.isPlaying()) {
				mSeekBar.setProgress((int) mMediaPlayer.getCurrentPosition());
				updatePlaytime(mMediaPlayer.getCurrentPosition());
				// repeat the process
				mHandler.postDelayed(this, AUDIO_PROGRESS_UPDATE_TIME);
			} else {
				// DO NOT update UI if the player is paused
			}
		}
	};

	/***
	 * Start playing the audio. Calling this method if the already playing
	 * audio, has no effect.
	 ****/
	public static void play() {

		if (mUri == null)
			throw new IllegalStateException(
					"Uri cannot be null. Call init() before calling play()");

		if (mMediaPlayer == null)
			throw new IllegalStateException("Call init() before calling play()");

		if (mMediaPlayer.isPlaying())
			return;

		mHandler.postDelayed(mUpdateProgress, AUDIO_PROGRESS_UPDATE_TIME);

		mMediaPlayer.start();

		setPausable();
	}

	/***
	 * Pause the audio being played. Calling this method has no effect if the
	 * audio is already paused
	 */
	public static void pause() {

		if (mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
			setPlayable();
		}
	}

	private static void updatePlaytime(int currentTime) {
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

		StringBuilder playbackStr = new StringBuilder();

		// set the current time
		// its ok to show 00:00 in the UI
		playbackStr.append(String.format(
				"%02d:%02d",
				TimeUnit.MILLISECONDS.toMinutes((long) currentTime),
				TimeUnit.MILLISECONDS.toSeconds((long) currentTime)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
								.toMinutes((long) currentTime))));

		playbackStr.append("/");

		// set total time as the audio is being played
		if (totalDuration != 0) {
			playbackStr.append(String.format(
					"%02d:%02d",
					TimeUnit.MILLISECONDS.toMinutes((long) totalDuration),
					TimeUnit.MILLISECONDS.toSeconds((long) totalDuration)
							- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
									.toMinutes((long) totalDuration))));
		} else
			Log.w(TAG, "Something strage this audio track duration in zero");

		mPlaybackTime.setText(playbackStr);

		// DebugLog.i(currentTime + " / " + totalDuration);
	}

	private static void setPlayable() {
		if (mPlayButton != null)
			mPlayButton.setVisibility(View.VISIBLE);

		if (mPauseButton != null)
			mPauseButton.setVisibility(View.GONE);
	}

	private static void setPausable() {
		if (mPlayButton != null)
			mPlayButton.setVisibility(View.GONE);
		if (mPauseButton != null)
			mPauseButton.setVisibility(View.VISIBLE);
	}

	/***
	 * Initialize the audio player. This method should be the first one to be
	 * called before starting to play audio using {@link AudioWife}
	 * 
	 * @param ctx
	 *            {@link Activity} Context
	 * @param uri
	 *            Uri of the audio to be played.
	 ****/
	public static void init(Context ctx, Uri uri, SeekBar seekBar,
			View playBtn, View pauseBtn, TextView playTime) {

		if (uri == null)
			throw new IllegalArgumentException("Uri cannot be null");

		mUri = uri;
		mSeekBar = seekBar;
		mPlayButton = playBtn;
		mPauseButton = pauseBtn;
		mPlaybackTime = playTime;

		mHandler = new Handler();

		initPlayer(ctx);

		initMediaSeekBar();
	}

	/****
	 * Initialize and prepare the audio player
	 ****/
	private static void initPlayer(Context ctx) {

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

		mMediaPlayer
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer mp) {
						// set UI when audio finished playing
						int currentPlayTime = 0;
						mSeekBar.setProgress((int) currentPlayTime);
						updatePlaytime(currentPlayTime);
						setPlayable();
					}
				});
	}

	private static void initMediaSeekBar() {

		// update seekbar
		long finalTime = mMediaPlayer.getDuration();
		mSeekBar.setMax((int) finalTime);

		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mMediaPlayer.seekTo(seekBar.getProgress());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {

			}
		});
	}

	/***
	 * Releases the allocated resources.
	 * 
	 * <p>
	 * Call {@link #init(Context, Uri, SeekBar, View, View, TextView)} before
	 * calling {@link #play()}
	 * </p>
	 * */
	public static void release() {

		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mHandler = null;
		}
	}
}
