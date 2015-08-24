package mipt.media;

import mipt.media.MediaPlayerHandler.TYPE;
import android.view.SurfaceView;

public interface MediaPlayerHandlerIfc {

	public void setDisplay(SurfaceView view);

	public void start(String... params);

	public void release();

	public void pause();

	public void stop();

	public void continuePlay();

	public void seekTo(int msec, int delay);

	public boolean changeType(TYPE type);

	public void testError();

	public void setMediaStatusCallback(MediaStatusCallback callback);
}
