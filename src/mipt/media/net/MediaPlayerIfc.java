package mipt.media.net;

import java.io.IOException;

import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnTimedTextListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.view.SurfaceHolder;

public interface MediaPlayerIfc {

	public void setDisplay(SurfaceHolder sh) throws IllegalArgumentException;

	public void setDataSource(String... params) throws IOException, IllegalArgumentException, SecurityException,
			IllegalStateException;

	public void prepareAsync() throws IllegalStateException;

	public void prepare() throws IllegalStateException, IOException;

	public void start() throws IllegalStateException;

	public void stop() throws IllegalStateException;

	public void pause() throws IllegalStateException;

	public void seekTo(int msec) throws IllegalStateException;

	public int getCurrentPosition();

	public int getDuration();

	public void release();

	public void reset();

	public void setScreenOnWhilePlaying(boolean screenOn);

	public int getVideoWidth();

	public int getVideoHeight();

	public boolean isPlaying();

	public void setOnTimedTextListener(OnTimedTextListener listener);

	public void setOnPreparedListener(OnPreparedListener listener);

	public void setOnCompletionListener(OnCompletionListener listener);

	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener);

	public void setOnSeekCompleteListener(OnSeekCompleteListener listener);

	public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener);

	public void setOnErrorListener(OnErrorListener listener);

	public void setOnInfoListener(OnInfoListener listener);

}
