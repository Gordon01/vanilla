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

 * Neither the name of the Sony Ericsson Mobile Communications AB / Sony Mobile
 Communications AB nor the names of its contributors may be used to endorse or promote
 products derived from this software without specific prior written permission.

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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import ch.blinkenlights.android.vanilla.PlaybackService;
import ch.blinkenlights.android.vanilla.Song;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.ExtensionUtils;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlObjectClickEvent;
import com.sonyericsson.extras.liveware.extension.util.control.ControlView;
import com.sonyericsson.extras.liveware.extension.util.control.ControlView.OnClickListener;
import com.sonyericsson.extras.liveware.extension.util.control.ControlViewGroup;

/**
 * The sample control for SmartWatch handles the control on the accessory. This
 * class exists in one instance for every supported host application that we
 * have registered to
 */
public final class ControlSmartWatch2 extends ControlExtension {

    private static final int SELECT_TOGGLER_MS = 200;
    private static final int MENU_ITEM_0 = 0;
    private static final int MENU_ITEM_1 = 1;
    private static final int MENU_ITEM_2 = 2;
    private static final int MENU_ITEM_3 = 3;
    private static final int MENU_ITEM_4 = 4;
    private static final int MENU_ITEM_5 = 5;
    public static final int[] SHUFFLE_ICONS =
		{ R.drawable.sw_rnd_off, R.drawable.sw_rnd_active, R.drawable.sw_rnd_album_active };
	public static final int[] FINISH_ICONS =
		{ R.drawable.sw_rpt_off, R.drawable.sw_rpt_active, R.drawable.sw_rpt_current_active, R.drawable.sw_rpt_stop_current_active, R.drawable.sw_rpt_random_active };

    private Handler mHandler;

    private ControlViewGroup mLayout = null;
    private Context mContext; 

    private boolean mTextMenu = false;
    private boolean mCurrentIsPlaying;
    
    Bundle[] mMenuItemsText = new Bundle[3];
    Bundle[] mMenuItemsIcons = new Bundle[3];
    boolean isRegistered = false;

    /**
     * Create sample control.
     *
     * @param hostAppPackageName Package name of host application.
     * @param context The context.
     * @param handler The handler to use
     */
    ControlSmartWatch2(final String hostAppPackageName, final Context context,
            Handler handler) {
        super(context, hostAppPackageName);
        mContext = context;
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        mHandler = handler;
        setupClickables(context);
        initializeMenus();
    }

    private void initializeMenus() {
        mMenuItemsText[0] = new Bundle();
        mMenuItemsText[0].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, MENU_ITEM_0);
        mMenuItemsText[0].putString(Control.Intents.EXTRA_MENU_ITEM_TEXT, "Item 1");
        mMenuItemsText[1] = new Bundle();
        mMenuItemsText[1].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, MENU_ITEM_1);
        mMenuItemsText[1].putString(Control.Intents.EXTRA_MENU_ITEM_TEXT, "Item 2");
        mMenuItemsText[2] = new Bundle();
        mMenuItemsText[2].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, MENU_ITEM_2);
        mMenuItemsText[2].putString(Control.Intents.EXTRA_MENU_ITEM_TEXT, "Item 3");

        mMenuItemsIcons[0] = new Bundle();
        mMenuItemsIcons[0].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, MENU_ITEM_3);
        mMenuItemsIcons[0].putString(Control.Intents.EXTRA_MENU_ITEM_ICON,
                ExtensionUtils.getUriString(mContext, R.drawable.actions_call));
        mMenuItemsIcons[1] = new Bundle();
        mMenuItemsIcons[1].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, MENU_ITEM_4);
        mMenuItemsIcons[1].putString(Control.Intents.EXTRA_MENU_ITEM_ICON,
                ExtensionUtils.getUriString(mContext, R.drawable.actions_reply));
        mMenuItemsIcons[2] = new Bundle();
        mMenuItemsIcons[2].putInt(Control.Intents.EXTRA_MENU_ITEM_ID, MENU_ITEM_5);
        mMenuItemsIcons[2].putString(Control.Intents.EXTRA_MENU_ITEM_ICON,
                ExtensionUtils.getUriString(mContext, R.drawable.actions_view_in_phone));
    }

    /**
     * Get supported control width.
     *
     * @param context The context.
     * @return the width.
     */
    public static int getSupportedControlWidth(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.smart_watch_2_control_width);
    }

    /**
     * Get supported control height.
     *
     * @param context The context.
     * @return the height.
     */
    public static int getSupportedControlHeight(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.smart_watch_2_control_height);
    }

    @Override
    public void onDestroy() {
        Log.d(SWExtensionService.LOG_TAG, "SampleControlSmartWatch onDestroy");
        mHandler = null;
    };

    @Override
    public void onStart() {
        // Nothing to do. Animation is handled in onResume.
    }

    @Override
    public void onStop() {
        // Nothing to do. Animation is handled in onPause.
    }
    
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
    	  @Override
    	  public void onReceive(Context context, Intent intent) {
    	    // Extract data included in the Intent
    	    String message = intent.getStringExtra("message");
    	    Log.d("receiver", "Got message: " + message);
    	    updateSongInfo();
    	  }
    	};
    	   
    @Override
    public void onResume() {
    	Log.d(SWExtensionService.LOG_TAG, "Resuming SW2 activity");
    	
    	IntentFilter mFilter = new IntentFilter("SMARTWATCH_REFRESH");
    	mContext.registerReceiver(mMessageReceiver, mFilter);
    	isRegistered = true;
    	
		Song song = null;
		Bitmap cover = null;
        Bundle[] data = new Bundle[5];

		if (PlaybackService.hasInstance()) {
			PlaybackService service = PlaybackService.get(mContext);
			song = service.getSong(0);
			cover = song.getCover(mContext);
			int state = service.getState();
			mCurrentIsPlaying = (state & PlaybackService.FLAG_PLAYING) != 0;
		
	        Bundle b1 = new Bundle();
	        b1.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.artist);
	        b1.putString(Control.Intents.EXTRA_TEXT, song.artist);
	
	        Bundle b2 = new Bundle();
	        b2.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.track);
	        b2.putString(Control.Intents.EXTRA_TEXT, song.title);
	        
	        //This trick is inspired by the way how ControlExtension.sendImage() works
	        Bundle b3 = new Bundle();
	        b3.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.play);
			if (mCurrentIsPlaying) {
				b3.putString(Control.Intents.EXTRA_DATA_URI, 
		        		ExtensionUtils.getUriString(mContext, R.drawable.sw_pause));
			} else {
				b3.putString(Control.Intents.EXTRA_DATA_URI, 
		        		ExtensionUtils.getUriString(mContext, R.drawable.sw_play));
			}
			
			int shuffle = PlaybackService.shuffleMode(state);
			int finish = PlaybackService.finishAction(state);
			
	        Bundle b4 = new Bundle();
	        b4.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.sw_shuffle);
	        b4.putString(Control.Intents.EXTRA_DATA_URI, 
	        		ExtensionUtils.getUriString(mContext, SHUFFLE_ICONS[shuffle]));
	        
	        Bundle b5 = new Bundle();
	        b5.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.sw_repeat);
	        b5.putString(Control.Intents.EXTRA_DATA_URI, 
	        		ExtensionUtils.getUriString(mContext, FINISH_ICONS[finish]));
	
	        data[0] = b1;
	        data[1] = b2;
	        data[2] = b3;
	        data[3] = b4;
	        data[4] = b5;
		}

		showLayout(R.layout.sw_control_2, data);
		
		//It's better to send image after showing layout because it speed up loading (but very slightly).
		//But sending cover with Bundle is also possible. 
		if (cover != null) {
			sendImage(R.id.album_cover, cover);
		} else {
			sendImage(R.id.album_cover, R.drawable.fallback_cover);
		}		
    }
    
    public void updateSongInfo() {
    	Log.d(SWExtensionService.LOG_TAG, "Updating song info in SW2 activity");
		Song song = null;
		Bitmap cover = null;
		int shuffle = 0;
		int finish = 0;
		
		if (PlaybackService.hasInstance()) {
			PlaybackService service = PlaybackService.get(mContext);
			song = service.getSong(0);
			cover = song.getCover(mContext);
			int state = service.getState();
			shuffle = PlaybackService.shuffleMode(state);
			finish = PlaybackService.finishAction(state);
			Log.d(SWExtensionService.LOG_TAG, "State = " + Integer.toString(state));
			Log.d(SWExtensionService.LOG_TAG, "Shuffle = " + Integer.toString(shuffle));
			Log.d(SWExtensionService.LOG_TAG, "Finish = " + Integer.toString(finish));
			mCurrentIsPlaying = (state & PlaybackService.FLAG_PLAYING) != 0;
		}
		
		//TODO: Need to check is cover changed because resending it is not a good idea because of speed.
		if (cover != null) {
			sendImage(R.id.album_cover, cover);
		} else {
			sendImage(R.id.album_cover, R.drawable.fallback_cover);
		}
		
		//TODO: And better check exactly what is changed to update exactly what is changed
		if (mCurrentIsPlaying) {
			sendImage(R.id.play, R.drawable.sw_pause);
		} else {
			sendImage(R.id.play, R.drawable.sw_play);
		}
				
		sendImage(R.id.sw_shuffle, SHUFFLE_ICONS[shuffle]);
		sendImage(R.id.sw_repeat, FINISH_ICONS[finish]);
		
    	sendText(R.id.artist, song.artist);
		sendText(R.id.track, song.title);
    }

    @Override
    public void onPause() {
        Log.d(SWExtensionService.LOG_TAG, "Stopping SW2 activity");
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

    @Override
    public void onObjectClick(final ControlObjectClickEvent event) {
        Log.d(SWExtensionService.LOG_TAG, "onObjectClick() " + event.getClickType());
        if (event.getLayoutReference() != -1) {
            mLayout.onClick(event.getLayoutReference());
        }
    }

    @Override
    public void onKey(final int action, final int keyCode, final long timeStamp) {
        Log.d(SWExtensionService.LOG_TAG, "onKey()");
        if (action == Control.Intents.KEY_ACTION_RELEASE
                && keyCode == Control.KeyCodes.KEYCODE_OPTIONS) {
//            toggleMenu();
        }
        else if (action == Control.Intents.KEY_ACTION_RELEASE
                && keyCode == Control.KeyCodes.KEYCODE_BACK) {
            Log.d(SWExtensionService.LOG_TAG, "onKey() - back button intercepted.");
        }
    }

    @Override
    public void onMenuItemSelected(final int menuItem) {
        Log.d(SWExtensionService.LOG_TAG, "onMenuItemSelected() - menu item " + menuItem);
        if (menuItem == MENU_ITEM_0) {
            clearDisplay();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onResume();
                }
            }, 1000);
        }
    }

    private void toggleMenu() {
        if (mTextMenu) {
            showMenu(mMenuItemsIcons);
        }
        else
        {
            showMenu(mMenuItemsText);
        }
        mTextMenu = !mTextMenu;
    }

    private void setupClickables(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.sw_control_2
                , null);
        mLayout = (ControlViewGroup) parseLayout(layout);
        if (mLayout != null) {
            ControlView btn_back = mLayout.findViewById(R.id.back);
            btn_back.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick() {
                    sendImage(R.id.back, R.drawable.sw_back_pressed);
                    sendActionToPS(PlaybackService.ACTION_REWIND_SONG);
                    mHandler.postDelayed(new SelectToggler(R.id.back,
                            R.drawable.sw_back), SELECT_TOGGLER_MS);
                }
            });
            
            ControlView btn_next = mLayout.findViewById(R.id.next);
            btn_next.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick() {
                    sendImage(R.id.next, R.drawable.sw_next_pressed);
                    sendActionToPS(PlaybackService.ACTION_NEXT_SONG_AUTOPLAY);
                    mHandler.postDelayed(new SelectToggler(R.id.next,
                            R.drawable.sw_next), SELECT_TOGGLER_MS);
                }
            });
            
            ControlView btn_rand = mLayout.findViewById(R.id.sw_shuffle);
            btn_rand.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick() {
                	//TODO: Add pressed animation
                    sendActionToPS(PlaybackService.ACTION_CYCLE_SHUFFLE);
                }
            });
            
            ControlView btn_repeat = mLayout.findViewById(R.id.sw_repeat);
            btn_repeat.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick() {
                	//TODO: Add pressed animation
                    sendActionToPS(PlaybackService.ACTION_CYCLE_REPEAT);
                }
            });
            
            //Play button requires trick to change state
            //TODO: This is slow. Maybe change visibility?
            ControlView btn_play = mLayout.findViewById(R.id.play);
            btn_play.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick() {
                	if (mCurrentIsPlaying) {
	                    sendImage(R.id.play, R.drawable.sw_play_pressed);
	                    sendActionToPS(PlaybackService.ACTION_TOGGLE_PLAYBACK);
	                    mHandler.postDelayed(new SelectToggler(R.id.play,
	                            R.drawable.sw_play), SELECT_TOGGLER_MS);
                	} else {
	                    sendImage(R.id.play, R.drawable.sw_pause_pressed);
	                    sendActionToPS(PlaybackService.ACTION_TOGGLE_PLAYBACK);
	                    mHandler.postDelayed(new SelectToggler(R.id.play,
	                            R.drawable.sw_pause), SELECT_TOGGLER_MS);
                	}
                }
            });
        }
    }
    
    
    private void sendActionToPS(String act) {
		if (act != null) {
			Intent intent = new Intent(mContext, PlaybackService.class);
			intent.setAction(act);
			mContext.startService(intent);
		}
    }

    private class SelectToggler implements Runnable {

        private int mLayoutReference;
        private int mResourceId;

        SelectToggler(int layoutReference, int resourceId) {
            mLayoutReference = layoutReference;
            mResourceId = resourceId;
        }

        @Override
        public void run() {
            sendImage(mLayoutReference, mResourceId);
        }

    }

}
