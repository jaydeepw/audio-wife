Audio Wife
==========

A simple themable audio player library for Android. Helps you have an Audio Controller
for your Audio Player UI. Have your own UI and pass the instance of UI controls like
Play button, Pause button, Seekbar etc to AudioWife and rest is taken care of.

![Audio Player Paused](images/paused.JPG)

![Audio Player Playing](images/playing.JPG)


Why this project?
====================
1. A simple native audio player API wrapper
2. Others found were complex & provided no way to be embed as library
3. Some even involved compilation using Android NDK


Including in your project
=========================

AudioWife is presented as an [Android library project][1].

You can include this project by [referencing it as a library project][2] in
Eclipse or ant.

This project has NO DEPENDENCIES.


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

// initialize AudioWife
// and play
AudioWife.getInstance()
		.init(mContext, mUri)
		.setPlayView(mPlayMedia)
		.setPauseView(mPauseMedia)
		.setSeekBar(mMediaSeekBar)
		.setPlaytime(mPlaybackTime)
		.play();

// to pause
AudioWife.getInstance().pause();


// when done playing, release the resources
AudioWife.getInstance().release();

```

Permission required to play audio

```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

Why the name 'AudioWife'?
=========================
This relates with another Android AudioRecorder library project that is coming soon. 
The name AudioWife comes from an analogy of a married couple where the wife is an active Player, hence AudioWife
for Audio Player and husband being a Listener hence AudioHusband for Audio Recorder.

Contributing
=========================

Please fork this repository and contribute back using
[pull requests](https://github.com/jaydeepw/audio-wife/pulls).

Please follow Android coding [style guide](https://source.android.com/source/code-style.html)


Developed by
============

 * Jaydeep Wagh - <jaydeep.w@gmail.com>

Credits
==========

[Official Android MediaPlayer Dev Docs](http://developer.android.com/reference/android/media/MediaPlayer.html)

[Android MediaPlayer Tutorial](http://www.tutorialspoint.com/android/android_mediaplayer.htm)


License
=======

	The MIT License (MIT)

	Copyright (c) 2014 Jaydeep

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.


[1]: http://developer.android.com/guide/developing/projects/projects-eclipse.html
[2]: http://developer.android.com/guide/developing/projects/projects-eclipse.html#ReferencingLibraryProject