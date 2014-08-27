/*
Copyright (C) 2014  Alexander Sergeev <etc9053@gmail.com>
Copyright (c) 2011, Sony Ericsson Mobile Communications AB
Copyright (c) 2011-2013, Sony Mobile Communications AB
   
   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 3 of the License, or
   (at your option) any later version.
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software Foundation,
   Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of the Sony Ericsson Mobile Communications AB nor the names
  of its contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package su.thinkdifferent.vanilla;

import ch.blinkenlights.android.vanilla.PlaybackService;
import ch.blinkenlights.android.vanilla.Song;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.Dbg;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlTouchEvent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Handler;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.ArrayList;

/**
 * The music control extension allows you to control a music player from an
 * accessory.
 */
public class ControlSmartWatch extends ControlExtension {

    public static final int WIDTH = 128;

    public static final int HEIGHT = 128;

    private static final int STATE_IDLE = 0;

    private static final int STATE_STARTED = 1;

    private static final int STATE_PAUSED = 2;

    private static final int TITLE_Y_POS = HEIGHT - 7;

    private static final int TITLE_WIDTH = WIDTH - 2 * 24;

    private static final int ARTIST_Y_POS = TITLE_Y_POS - 19;

    private static final int ARTIST_WIDTH = WIDTH - 2 * 6;

    private static final int PLAY_PAUSE_X = 39;

    private static final int PLAY_PAUSE_Y = 39;

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;

    private int mState = STATE_IDLE;

    private ArrayList<ControlButton> mButtons = new ArrayList<ControlButton>();

    private Bitmap mBitmap;

    private PlayPauseButton mPlayPauseButton = null;
    
    boolean isRegistered = false;

    private String mCurrentArtist = null;

    private String mCurrentTitle = null;

    private Bitmap mCurrentAlbumArt = null;

    private boolean mCurrentIsPlaying = false;
   
    private Handler mHandler;
    private final int width;
    private final int height;
    
    /**
     * Create sample control.
     *
     * @param hostAppPackageName Package name of host application.
     * @param context The context.
     * @param handler The handler to use
     */
    public ControlSmartWatch(final String hostAppPackageName, final Context context,
            Handler handler) {
        super(context, hostAppPackageName);
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        mHandler = handler;
        width = getSupportedControlWidth(context);
        height = getSupportedControlHeight(context);
    }
  
    public static int getSupportedControlWidth(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.smart_watch_control_width);
    }

    public static int getSupportedControlHeight(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.smart_watch_control_height);
    }
    
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
  	  @Override
  	  public void onReceive(Context context, Intent intent) {
  	    // Extract data included in the Intent
  	    String message = intent.getStringExtra("message");
  	    Log.d("receiver", "Got message: " + message);
  	    updateSongInfo();
  	    updateDisplay(true);
  	  }
  	};
  	
    public void updateSongInfo() {
    	Log.d(SWExtensionService.LOG_TAG, "Updating song info in MN2 activity");
    	
    	//Vars initialisation
		Song song = null;
		Bitmap cover = null;
		
		//Getting songinfo from PlaybackService 
		if (PlaybackService.hasInstance()) {
			PlaybackService service = PlaybackService.get(mContext);
			song = service.getSong(0);
			int state = service.getState();
			cover = song.getCover(mContext);
			mCurrentArtist = song.artist;
			mCurrentTitle = song.title;
			mCurrentAlbumArt = song.getCover(mContext);
			mCurrentIsPlaying = (state & PlaybackService.FLAG_PLAYING) != 0;
		}
		
		//Incase of no cover, set dafault fallback cover
		if (cover == null) {
			cover = BitmapFactory.decodeResource(mContext.getResources(),
                    R.drawable.fallback_cover);
		}
		
		//Finally setting cover bitmap. Fallback or real.
		mCurrentAlbumArt =  cover;
    }

    @Override
    public void onStart() {
    	Log.d(SWExtensionService.LOG_TAG, "Starting MN2 activity");

        mState = STATE_PAUSED;
        
        updateSongInfo();
        
        // Show info about current track on screen
        createButtons();

    }

    @Override
    public void onStop() {
    	Log.d(SWExtensionService.LOG_TAG, "Stopping MN2 activity");

        mState = STATE_IDLE;

        // Clear current bitmap.
        mBitmap = null;
    }

    @Override
    public void onPause() {
        Log.d(SWExtensionService.LOG_TAG, "Pausing MN2 activity");
        try
        {
        	if (isRegistered) {
        	    mContext.unregisterReceiver(mMessageReceiver);
        	    isRegistered = false;
        	}
        }
        catch (Exception e) {
        	e.printStackTrace();
        }

        mState = STATE_PAUSED;
    }

    @Override
    public void onResume() {
    	Log.d(SWExtensionService.LOG_TAG, "Resuming MN2 activity");

        mState = STATE_STARTED;
        
        //Update song info global vars
        updateSongInfo();
        
        //Set broadcast receiver
    	IntentFilter mFilter = new IntentFilter("SMARTWATCH_REFRESH");
    	mContext.registerReceiver(mMessageReceiver, mFilter);
    	isRegistered = true;

        // Update the display with the latest info.
        updateDisplay(true);
    }

    @Override
    public void onDestroy() {
    	//TODO
    }

    @Override
    public void onTouch(final ControlTouchEvent event) {
        Dbg.d("onTouch");

        if (event != null) {
            Dbg.v("action: " + event.getAction() + " x: " + event.getX() + " y: " + event.getY()
                    + " time: " + event.getTimeStamp());
        }
        if (mBitmap == null) {
            // If Music Extension has stopped
            return;
        }
        // Check touch on any buttons
        for (int i = 0; i < mButtons.size(); i++) {
            ControlButton button = mButtons.get(i);
            boolean oldIsPressed = button.isPressed();
            button.checkTouchEvent(event);

            // Press status changed. Update display.
            if (button.isPressed() != oldIsPressed) {
                updateBitmapAndButton(button);
            }
        }
    }

    @Override
    public void onSwipe(int direction) {
    	AudioManager mAudioManager = (AudioManager)mContext.getSystemService(PlaybackService.AUDIO_SERVICE);
        switch (direction) {
            case Control.Intents.SWIPE_DIRECTION_UP:
            	mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                break;
            case Control.Intents.SWIPE_DIRECTION_DOWN:
            	mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                break;
            case Control.Intents.SWIPE_DIRECTION_LEFT:
            	mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                break;
            case Control.Intents.SWIPE_DIRECTION_RIGHT:
            	mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                break;
            default:
                break;
        }
    }

    /**
     * Update the button part of a bitmap. Used to avoid sending large images to
     * accessory.
     *
     * @param button The button to update.
     */
    private void updateBitmapAndButton(ControlButton button) {
        Dbg.d("updateBitmapAndButton.");

        // Get the current background.
        Bitmap bitmap = mBitmap.copy(BITMAP_CONFIG, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        // Draw the button
        canvas.drawBitmap(button.getBitmap(), button.getX(), button.getY(), paint);

        // Create snapshot of part of the bitmap to send.
        Bitmap outBitmap = Bitmap.createBitmap(bitmap, button.getX(), button.getY(),
                button.getWidth(), button.getHeight());
        showBitmap(outBitmap, button.getX(), button.getY());
    }

    /**
     * Send a complete update of the screen to the accessory.
     */
    private void showBitmapAndButtons() {
        Dbg.d("showBitmapAndButtons");

        // Create a copy of the bitmap so that we don't keep the original
        Bitmap bitmap = mBitmap.copy(BITMAP_CONFIG, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        // Add all buttons.
        for (int i = 0; i < mButtons.size(); i++) {
            ControlButton button = mButtons.get(i);
            canvas.drawBitmap(button.getBitmap(), button.getX(), button.getY(), paint);
        }

        showBitmap(bitmap);

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.sonyericsson.extras.liveware.extension.oss.music.player.PlaybackListener
     * #onUpdate()
     */
    public void onUpdate() {
        // Only update the screen if the control is started.
        if (mState != STATE_STARTED) {
            Dbg.w("Update received in state: " + mState);
            return;
        }

        updateDisplay(false);
    }

    /**
     * Update the accessory display with info about the track currently being
     * played and add all buttons on top of it.
     *
     * @param forceUpdate True if update regardless if playback info is changed.
     */
    private void updateDisplay(boolean forceUpdate) {

        mPlayPauseButton.update(mCurrentIsPlaying);

        // Create bitmap to draw in.
        mBitmap = Bitmap.createBitmap(WIDTH, HEIGHT, BITMAP_CONFIG);

        // Set the density to default to avoid scaling.
        mBitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        Canvas canvas = new Canvas(mBitmap);
        Paint paint = new Paint();

        // Add album art.
        if (mCurrentAlbumArt == null) {
            mCurrentAlbumArt = BitmapFactory.decodeResource(mContext.getResources(),
                    R.drawable.fallback_cover, mBitmapOptions);
        }
        Rect source = new Rect(0, 0, mCurrentAlbumArt.getWidth(), mCurrentAlbumArt.getHeight());
        Rect dest = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        canvas.drawBitmap(mCurrentAlbumArt, source, dest, paint);

        // Add background for top buttons
//        Bitmap volumeBackground = BitmapFactory.decodeResource(mContext.getResources(),
//                R.drawable.player_text_top_bg, mBitmapOptions);
//        canvas.drawBitmap(volumeBackground, 0, 0, paint);

        // Add background for text.
        Bitmap textBackground = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.player_text_bottom_bg, mBitmapOptions);
        canvas.drawBitmap(textBackground, 0, HEIGHT - textBackground.getHeight(), paint);

        TextPaint textPaint = new TextPaint(paint);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Add artist
        if (mCurrentArtist != null) {
            TextPaint artistPaint = new TextPaint(textPaint);
            artistPaint.setTextSize(mContext.getResources().getDimensionPixelSize(
                    R.dimen.smart_watch_text_size_normal));
            artistPaint.setColor(mContext.getResources().getColor(R.color.smart_watch_text_color_white));
            int textX = WIDTH / 2;
            ExtensionUtils.drawText(canvas, mCurrentArtist, textX, ARTIST_Y_POS, artistPaint, ARTIST_WIDTH);
        }

        // Add title
        if (mCurrentTitle != null) {
            TextPaint titlePaint = new TextPaint(textPaint);
            titlePaint.setTextSize(mContext.getResources().getDimensionPixelSize(
                    R.dimen.smart_watch_text_size_small));
            titlePaint.setColor(mContext.getResources().getColor(R.color.smart_watch_text_color_white));
            int textX = WIDTH / 2;
            ExtensionUtils.drawText(canvas, mCurrentTitle, textX, TITLE_Y_POS, titlePaint, TITLE_WIDTH);
        }

        // Add buttons
        showBitmapAndButtons();
    }

    /**
     * Create all buttons.
     */
    private void createButtons() {
        mButtons.clear();

//        // Volume down
//        Bitmap volumeDownBitmap = BitmapFactory.decodeResource(mContext.getResources(),
//                R.drawable.music_volme_minus_icn, mBitmapOptions);
//        Bitmap volumeDownPressedBitmap = BitmapFactory.decodeResource(mContext.getResources(),
//                R.drawable.music_volme_minus_pressed_icn, mBitmapOptions);
//        ControlButton volumeDownButton = new ControlButton(0, 0, volumeDownBitmap,
//                volumeDownPressedBitmap) {
//            @Override
//            public void onClick() {
//                //TODO
//            }
//        };
//        mButtons.add(volumeDownButton);
//
//        // Volume up
//        Bitmap volumeUpBitmap = BitmapFactory.decodeResource(mContext.getResources(),
//                R.drawable.music_volme_plus_icn, mBitmapOptions);
//        Bitmap volumeUpPressedBitmap = BitmapFactory.decodeResource(mContext.getResources(),
//                R.drawable.music_volme_plus_pressed_icn, mBitmapOptions);
//        ControlButton volumeUpButton = new ControlButton(WIDTH - volumeUpBitmap.getWidth(), 0,
//                volumeUpBitmap, volumeUpPressedBitmap) {
//            @Override
//            public void onClick() {
//            	//TODO
//            }
//        };
//        mButtons.add(volumeUpButton);

        // Previous
        Bitmap previousBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.music_previous_icn, mBitmapOptions);
        Bitmap previousPressedBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.music_previous_pressed_icn, mBitmapOptions);
        ControlButton previousButton = new ControlButton(0, HEIGHT - previousBitmap.getHeight(),
                previousBitmap, previousPressedBitmap) {
            @Override
            public void onClick() {
            	mediaButton(PlaybackService.ACTION_REWIND_SONG);
            }
        };
        mButtons.add(previousButton);

        // Next
        Bitmap nextBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.music_next_icn, mBitmapOptions);
        Bitmap nextPressedBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.music_next_pressed_icn, mBitmapOptions);
        ControlButton nextButton = new ControlButton(WIDTH - nextBitmap.getWidth(), HEIGHT
                - nextBitmap.getHeight(), nextBitmap, nextPressedBitmap) {
            @Override
            public void onClick() {
            	mediaButton(PlaybackService.ACTION_NEXT_SONG_AUTOPLAY);
            }
        };
        mButtons.add(nextButton);

        // Play pause
        Bitmap playBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.music_play_icn, mBitmapOptions);
        Bitmap playPressedBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.music_play_pressed_icn, mBitmapOptions);
        Bitmap pauseBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.music_pause_icn, mBitmapOptions);
        Bitmap pausePressedBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.music_pause_pressed_icn, mBitmapOptions);
        mPlayPauseButton = new PlayPauseButton(PLAY_PAUSE_X, PLAY_PAUSE_Y,
                playBitmap, playPressedBitmap, pauseBitmap, pausePressedBitmap) {
            @Override
            public void onClick() {
            	mediaButton(PlaybackService.ACTION_TOGGLE_PLAYBACK);
            }
        };
        mButtons.add(mPlayPauseButton);
    }
    
    private void mediaButton(String act) {
		if (act != null) {
			Intent intent = new Intent(mContext, PlaybackService.class);
			intent.setAction(act);
			mContext.startService(intent);
		}
    }

}
