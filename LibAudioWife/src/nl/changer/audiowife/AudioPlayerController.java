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

public class AudioPlayerController {
	
	/****
	 * in millis
	 ****/
	private static final int AUDIO_PROGRESS_UPDATE_TIME = 100;
	
	private static Handler mHandler = new Handler();;
	
	/***Initially the audio starts from the beginning.
	So initialized to 0*/
	private static int mCurrentPlayTime = 0;
	private static SeekBar mSeekBar;
	private static TextView mPlaybackTime;
	private static View mPlayButton;
	private static View mPauseButton;

	private static MediaPlayer mMediaPlayer = new MediaPlayer();;
	/***
	 * Audio URI
	 ****/
	private static Uri mUri;
	
	private static Runnable mUpdateTime = new Runnable() {
	      public void run() {
	    	  
	    	  long totalDuration = 0;
	    	  int currentTime = 0;
	    	  
	    	  if(mMediaPlayer != null) {
	    		  try {
		    		  totalDuration = mMediaPlayer.getDuration();
		    		  currentTime = mMediaPlayer.getCurrentPosition();
		    	  } catch (IllegalStateException e) {
		    		  e.printStackTrace();
		    	  } catch (Exception e) {
		    		  e.printStackTrace();
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
	    	  if(totalDuration != 0) {
	    		  playbackStr.append(String.format("%02d:%02d",
		            TimeUnit.MILLISECONDS.toMinutes((long) totalDuration),
		            TimeUnit.MILLISECONDS.toSeconds((long) totalDuration) - 
		            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
		            toMinutes((long) totalDuration)))
		          );  
	    	  } else
	    		  DebugLog.w("Something strage this audio track duration in zero");
	    	  
	    	  mPlaybackTime.setText(playbackStr);
	         
	    	  DebugLog.i("current: " + currentTime + " total: " + totalDuration);
	    	  
	    	  if(mHandler != null && mMediaPlayer.isPlaying() ) {
	    		  mSeekBar.setProgress((int) currentTime);
	    		  mHandler.postDelayed(this, AUDIO_PROGRESS_UPDATE_TIME);
	    	  } else {
	    		  // DO NOT update UI if the player is paused
	    	  }
	      }
	};
	

	
	public static void play(Context ctx) {
        
        int currentTime = mMediaPlayer.getCurrentPosition();
        mSeekBar.setProgress((int)currentTime);
        
        mHandler.postDelayed(mUpdateTime, AUDIO_PROGRESS_UPDATE_TIME);
        
        mMediaPlayer.seekTo(mCurrentPlayTime);
        
        mMediaPlayer.start();
        
        setPausable();
	}

	public static void pause() {
      mMediaPlayer.pause();
      mCurrentPlayTime = mSeekBar.getProgress();
      setPlayable();
	}
	
	private static void setPlayable() {
		if(mPlayButton != null)
			mPlayButton.setVisibility(View.VISIBLE);
		
	    if(mPauseButton != null)
	    	mPauseButton.setVisibility(View.GONE);
	}
	
	private static void setPausable() {
		if(mPlayButton != null)
			mPlayButton.setVisibility(View.GONE);
		if(mPauseButton != null) 
			mPauseButton.setVisibility(View.VISIBLE);
	}
 
	/***
	 * Initialize the audio player
	 * 
	 ****/
	public static void init(Context ctx, Uri uri, SeekBar seekBar, View playBtn, View pauseBtn, TextView playTime) {
		mUri = uri;
		mSeekBar = seekBar;
		mPlayButton = playBtn;
		mPauseButton = pauseBtn;
		mPlaybackTime = playTime;
		
		initPlayer(ctx);
        
		initMediaSeekBar();
	}
	
	/****
	 * Initialize and prepare the audio player
	 * 
	 ****/
	private static void initPlayer(Context ctx) {
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
        
        mMediaPlayer.setOnCompletionListener( new MediaPlayer.OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
    		  	// set UI when audio finished playing
				DebugLog.i("Finished playing audio");
    			mCurrentPlayTime = 0;
    			mSeekBar.setProgress((int) mCurrentPlayTime);
    			setPlayable();
			}
		});
	}

	private static void initMediaSeekBar() {
		
		// update seekbar
        long finalTime = mMediaPlayer.getDuration();
        mSeekBar.setMax((int) finalTime);
        
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
