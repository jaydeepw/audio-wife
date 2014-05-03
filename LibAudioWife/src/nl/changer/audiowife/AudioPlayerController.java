package nl.changer.audiowife;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

/****
 * Reference
 * http://www.tutorialspoint.com/android/android_mediaplayer.htm
 * ***/

public class AudioPlayerController {
	
	/****
	 * in millis
	 ****/
	private static final int AUDIO_PROGRESS_UPDATE_TIME = 100;
	
	private static Handler mHandler;
	
	private static int mCurrentPlayTime = 0;
	private static SeekBar mSeekBar;
	private static TextView mPlaybackTime;
	private static View mPlayButton;
	private static View mPauseButton;

	private static MediaPlayer mMediaPlayer;
	/***
	 * Maybe audio uri
	 ****/
	private static Uri mUri;
	
	public static void play(Context ctx) {
		
		if(mPlayButton != null) mPlayButton.setVisibility(View.GONE);
		if(mPauseButton != null) mPauseButton.setVisibility(View.VISIBLE);
		
		mMediaPlayer = new MediaPlayer();
		mHandler = new Handler();
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
        }
        
        try {
            mMediaPlayer.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        mMediaPlayer.start();
        long finalTime = mMediaPlayer.getDuration();
        mSeekBar.setMax((int) finalTime);
        
        int currentTime = mMediaPlayer.getCurrentPosition();
        
        mSeekBar.setProgress((int)currentTime);
        
        mHandler.postDelayed(mUpdateTime, AUDIO_PROGRESS_UPDATE_TIME);
        mPlayButton.setEnabled(true);
        
        mMediaPlayer.seekTo(mCurrentPlayTime);
        
        mMediaPlayer.start();
	}
	
	private static Runnable mUpdateTime = new Runnable() {
	      public void run() {
	    	  
	    	  long duration = 0;
	    	  int currentTime = 0;
	    	  
	    	  if(mMediaPlayer != null) {
	    		  try {
		    		  duration = mMediaPlayer.getDuration();
		    		  currentTime = mMediaPlayer.getCurrentPosition();
		    	  } catch (IllegalStateException e) {
		    		  e.printStackTrace();
		    	  }
	    		  
	    		  // set UI when audio finished playing
	    		  if(currentTime == duration) {
	    			  mCurrentPlayTime = 0;
	    			  setPlayable();
	    		  }
	    	  }
	    	  
	    	  StringBuilder playbackStr = new StringBuilder();
	    	  
	    	  // set the current time
	    	  // its ok to show 00:00 in the UI
    		  playbackStr.append(String.format("%02d:%02d",
	            TimeUnit.MILLISECONDS.toMinutes((long) currentTime),
	            TimeUnit.MILLISECONDS.toSeconds((long) currentTime) - 
	            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
	            toMinutes((long) currentTime)))
	         );	    		  
	    	  
	    	  playbackStr.append("/");
	    	  
	    	  // set total time as the audio is being played
	    	  if(duration != 0) {
	    		  playbackStr.append(String.format("%02d:%02d",
  		            TimeUnit.MILLISECONDS.toMinutes((long) duration),
  		            TimeUnit.MILLISECONDS.toSeconds((long) duration) - 
  		            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
  		            toMinutes((long) duration)))
  		          );  
	    	  } else
	    		  DebugLog.w("Something strage this audio track duration in zero");
	    	  
	    	  mPlaybackTime.setText(playbackStr);
	    	  
	    	  mSeekBar.setProgress((int) currentTime);
	         
	    	  if(mHandler != null)
	    		  mHandler.postDelayed(this, AUDIO_PROGRESS_UPDATE_TIME);
	      }
	};
	
	public static void pause() {
      mMediaPlayer.pause();
      mCurrentPlayTime = mSeekBar.getProgress();
      setPlayable();
	}
	
	private static void setPlayable() {
		if(mPlayButton != null) mPlayButton.setVisibility(View.VISIBLE);
	    if(mPauseButton != null) mPauseButton.setVisibility(View.GONE);
	}
 
	public static void initAudioPlayer(Uri uri, SeekBar seekBar, View playBtn, View pauseBtn, TextView playTime) {
		mUri = uri;
		mSeekBar = seekBar;
		mPlayButton = playBtn;
		mPauseButton = pauseBtn;
		mPlaybackTime = playTime;
		
		initMediaSeekBar();
	}
	
	private static void initMediaSeekBar() {
		mSeekBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
			
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

	public static void release() {
		
		if(mMediaPlayer != null ) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mHandler = null;	
		}
	
	}
}
