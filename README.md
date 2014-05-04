Audio Wife
==========

A simple themable audio player library for Android. Helps you have an Audio Controller
for your Audio Player UI. Have your own UI and pass the instance of UI controls like
Play button, Pause button, Seekbar to AudioWife and rest is taken care of.


![Audio Player Playing](images/playing.JPG)

![Audio Player Paused](images/paused.JPG)


Credits
==========

[Android MediaPlayer](http://developer.android.com/reference/android/media/MediaPlayer.html)

[Android MediaPlayer Tutorial](http://www.tutorialspoint.com/android/android_mediaplayer.htm)


Why this project?
====================
1. A simple native audio player API wrapper
2. Others found were complex & provided no way to be embed as library
3. Some even involved compilation using Android NDK


Getting started
====================
```java

// inflate your audio player view or have one in the existing UI already.
ViewGroup yourAudioPlayerView = (ViewGroup) mLayoutInflator.inflate(R.layout.playback_audio, mMediaPlayerContainer);

// initialize the player contols
Button mPlayMedia = (Button) yourAudioPlayerView.findViewById(R.id.play);
Button mPauseMedia = (Button) yourAudioPlayerView.findViewById(R.id.pause);
SeekBar mMediaSeekBar = (SeekBar) yourAudioPlayerView.findViewById(R.id.mediaSeekBar);
TextView mPlaybackTime = (TextView) yourAudioPlayerView.findViewById(R.id.playback_time);

AudioWife.getInstance()
		.init(mContext, mUri)
		.setPlayView(mPlayMedia)
		.setPauseView(mPauseMedia)
		.setSeekBar(mMediaSeekBar)
		.setPlaytime(mPlaybackTime)
		.play();
```

Why the name 'AudioWife'?
=========================
This relates with yet another Android AudioRecorder library project that is coming soon. 
So thought of it as analogous to a married couple where the wife is an active Player, hence AudioWife
for Audio Player and husband being a listener, hence AudioHusband for Audio Recorder.


Contributing
=========================

Please fork this repository and contribute back using
[pull requests](https://github.com/jaydeepw/audio-wife/pulls).

Please follow Android coding [style guide](https://source.android.com/source/code-style.html)