/**
 * Copyright © 2014 videoHj. All rights reserved.
 * @Title: AndroidMediaPlayer.java
 * @Prject: softEncodeMedia
 * @Package: mipt.media.net
 * @Description: TODO
 * @author: Administrator
 * @date: 2014-10-24 下午3:51:14
 * @version: V1.0
 */

package mipt.media.net;

import java.io.IOException;

import android.media.MediaPlayer;

/**
 * @ClassName: AndroidMediaPlayer
 * @Description: TODO
 * @author: Administrator
 * @date: 2014-10-24 下午3:51:14
 */

public class AndroidMediaPlayer extends MediaPlayer implements MediaPlayerIfc {

	@Override
	public void setDataSource(String... params) throws IOException, IllegalArgumentException, SecurityException,
			IllegalStateException {
		super.setDataSource(params[0]);

	}

}
