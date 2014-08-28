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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import ch.blinkenlights.android.vanilla.FullPlaybackActivity;
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
    private static final int[] SHUFFLE_ICONS =
		{ R.drawable.sw_rnd_off, R.drawable.sw_rnd_active, R.drawable.sw_rnd_album_active };
    private static final int[] FINISH_ICONS =
		{ R.drawable.sw_rpt_off, R.drawable.sw_rpt_active, R.drawable.sw_rpt_current_active, R.drawable.sw_rpt_stop_current_active, R.drawable.sw_rpt_random_active };
    private static final int[] BW_SHUFFLE_ICONS =
		{ R.drawable.bw_rnd_off, R.drawable.bw_rnd_active, R.drawable.bw_rnd_album_active };
    private static final int[] BW_FINISH_ICONS =
		{ R.drawable.bw_rpt_off, R.drawable.bw_rpt_active, R.drawable.bw_rpt_current_active, R.drawable.bw_rpt_stop_current_active, R.drawable.bw_rpt_random_active };
    private static final String[] SHUFFLE_ICONS_STRING =
		{ "rnd_off", "rnd_active", "rnd_album_active" };
    private static final String[] FINISH_ICONS_STRING =
		{ "rpt_off", "rpt_active", "rpt_current_active", "rpt_stop_current_active", "rpt_random_active" };
   
    private Handler mHandler;

    private ControlViewGroup mLayout = null;
    private Context mContext; 
    private SharedPreferences mPrefs = null;

    private boolean mTextMenu = false;
    private boolean mCurrentIsPlaying;
    
    Bundle[] mMenuItemsText = new Bundle[3];
    Bundle[] mMenuItemsIcons = new Bundle[3];
    boolean isRegistered = false;
    boolean mPowersave = false;
    
    //Previous song, mediaplayer state and cover. Used to detect what is changed on update
    Song pSong = null;
	Bitmap pCover = null;
	int pState = 0;
    

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
    	
    	//Setting broadcat listener
    	IntentFilter mFilter = new IntentFilter("SMARTWATCH_REFRESH");
    	mContext.registerReceiver(mMessageReceiver, mFilter);
    	isRegistered = true;
    	    	
    	//Setting preference var. We need to read settings in many places
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    	
		Song song = null;
		Bitmap cover = null;
        Bundle[] data = new Bundle[5];

		if (PlaybackService.hasInstance()) {     
			Log.d(SWExtensionService.LOG_TAG, "PlaybackService exists");
			PlaybackService service = PlaybackService.get(mContext);
			//TODO:It is possible, that PlaybackService simply don't have song.
			//So it's need to be handled properly, so no NullPointException.
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
	        
	        //Updating pre-variables
	        pSong = song;
	        pState = state;
	        pCover = cover;
		} else {
			Log.d(SWExtensionService.LOG_TAG, "PlaybackService does not exist. Starting it.");
			Intent intent = new Intent(mContext, FullPlaybackActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
			//Sometimes it is VERY slow so I decided to start activity
//        	mContext.startService(new Intent(mContext, PlaybackService.class));
		}
		
		//Decide what layout to show
	    Boolean altLayout = mPrefs.getBoolean(
	    mContext.getString(R.string.preference_key_border), false);
	    if (altLayout)
	    	showLayout(R.layout.sw_control_2, data);
	    else
	    	showLayout(R.layout.sw_control_2_alt, data);
	    
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
    	
		//This should never happen bacause PlaybackService sends broadcast intent, 
    	//which calls this function, but just to be safe :)
		if (!PlaybackService.hasInstance()) 
			return;
			
		Song song = null;
		Bitmap cover = null;
		int shuffle = 0;
		int finish = 0;
		int state = 0;
		
		//Firstly, we getting current state from PlaybackService
		PlaybackService service = PlaybackService.get(mContext);
		song = service.getSong(0);
		cover = song.getCover(mContext);
		state = service.getState();
		shuffle = PlaybackService.shuffleMode(state);
		finish = PlaybackService.finishAction(state);
		Log.d(SWExtensionService.LOG_TAG, "State = " + Integer.toString(state));
		Log.d(SWExtensionService.LOG_TAG, "Shuffle = " + Integer.toString(shuffle));
		Log.d(SWExtensionService.LOG_TAG, "Finish = " + Integer.toString(finish));
		mCurrentIsPlaying = (state & PlaybackService.FLAG_PLAYING) != 0;
	
		//We just started PlaybackService at onResume. 
		//TODO: doubling the code. needs optimization.
		if (pSong == null) {
			pSong = song;
			sendText(R.id.artist, song.artist);
			sendText(R.id.track, song.title);
			//Dirty hack to force update play/shuffle/final buttons
			pState = 999;
		}
		
		//Song is a big object so we cheching artist/title individually
		//Check if artist changed
		if (pSong.artist != song.artist) {
	    	sendText(R.id.artist, song.artist);
		}
		
		//Check if title changed
		if (pSong.title != song.title) {
			sendText(R.id.track, song.title);
		}
		
		//Globally check is whole state changed
		if (pState != state) {
			//Check if playing status changed
			boolean pPlaying = (pState & PlaybackService.FLAG_PLAYING) != 0;
			if (pPlaying != mCurrentIsPlaying) {
				if (mCurrentIsPlaying) {
					updateActionButton(R.id.play, "pause");
				} else {
					updateActionButton(R.id.play, "play");
				}
			}
			
			//Check if shuffle state changed
			int pShuffle = PlaybackService.shuffleMode(pState);
			if (pShuffle != shuffle) {
				updateActionButton(R.id.sw_shuffle, SHUFFLE_ICONS_STRING[shuffle]);
			}
			
			//Check if finish action changed
			int pFinish = PlaybackService.finishAction(pState);
			if (pFinish != finish) {
				updateActionButton(R.id.sw_repeat, FINISH_ICONS_STRING[finish]);
			}
		}
		
		//Cover is updated last bacause it result plugin to look more responsive.
		//Check if cover is updated
		if ((pCover != cover) && (mPowersave == false)) {
			if (cover != null) {
				sendImage(R.id.album_cover, cover);
			} else {
				sendImage(R.id.album_cover, R.drawable.fallback_cover);
			}
		}
		
        //Updating pre-variables
        pSong = song;
        pState = state;
        pCover = cover;
		
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
    	String action = "";
        switch (direction) {
            case Control.Intents.SWIPE_DIRECTION_UP:
                action = mPrefs.getString(
                        mContext.getString(R.string.preference_key_swipe_up), "");
                handleSwipe(action);
                break;
            case Control.Intents.SWIPE_DIRECTION_DOWN:
                action = mPrefs.getString(
                        mContext.getString(R.string.preference_key_swipe_down), "");
                handleSwipe(action);
                break;
            case Control.Intents.SWIPE_DIRECTION_LEFT:
                action = mPrefs.getString(
                        mContext.getString(R.string.preference_key_swipe_left), "");
                handleSwipe(action);
                break;
            case Control.Intents.SWIPE_DIRECTION_RIGHT:
                action = mPrefs.getString(
                        mContext.getString(R.string.preference_key_swipe_right), "");
                handleSwipe(action);
                break;
            default:
                break;
        }
    }
    
    public void updateActionButton(final int layoutReference, String button) {
    	int resourceId;
    	if (mPowersave) {
    		//B/W Image:
    		resourceId = mContext.getResources().getIdentifier("bw_"+button, "drawable", mContext.getPackageName());
    		sendImage(layoutReference, resourceId);
    	}
    	else {
    		//Colour Image:
    		resourceId = mContext.getResources().getIdentifier("sw_"+button, "drawable", mContext.getPackageName());
    		sendImage(layoutReference, resourceId);
    	}
    }
    
    public void handleSwipe(String action) {
    	Log.d(SWExtensionService.LOG_TAG, "Handling swipe: " + action);
    	AudioManager mAudioManager = (AudioManager)mContext.getSystemService(PlaybackService.AUDIO_SERVICE);
    	if 		  (action.equals("next_song")) {
    		sendActionToPS(PlaybackService.ACTION_NEXT_SONG);
    	} else if (action.equals("prev_song")) {
    		sendActionToPS(PlaybackService.ACTION_PREVIOUS_SONG);
    	} else if (action.equals("next_auto")) {
    		sendActionToPS(PlaybackService.ACTION_NEXT_SONG_AUTOPLAY);
    	} else if (action.equals("prev_auto")) {
    		sendActionToPS(PlaybackService.ACTION_REWIND_SONG);
    	} else if (action.equals("vol_up")) {
        	mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    	} else if (action.equals("vol_down")) {
    		mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    	}
    }
    
    @Override
    public void onActiveLowPowerModeChange(boolean lowPowerModeOn) {
    	Log.d(SWExtensionService.LOG_TAG, "Low power mode changed " + lowPowerModeOn);
    	
        Boolean action = mPrefs.getBoolean(
                mContext.getString(R.string.preference_key_powersave), false);
        if (!action) {
        	Intent intent = new Intent(Control.Intents.CONTROL_STOP_REQUEST_INTENT);
            sendToHostApp(intent);
        }
        
        Bundle[] data = new Bundle[5];
       
        if (lowPowerModeOn) {
        	//Extension goes to powersave mode - prepare and show B/W layout
        	mPowersave = true;
        	
        	Bundle b1 = new Bundle();
	        b1.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.artist);
	        b1.putString(Control.Intents.EXTRA_TEXT, pSong.artist);
	
	        Bundle b2 = new Bundle();
	        b2.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.track);
	        b2.putString(Control.Intents.EXTRA_TEXT, pSong.title);
	        
	        Bundle b3 = new Bundle();
	        b3.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.play);
			if (mCurrentIsPlaying) {
				b3.putString(Control.Intents.EXTRA_DATA_URI, 
		        		ExtensionUtils.getUriString(mContext, R.drawable.bw_pause));
			} else {
				b3.putString(Control.Intents.EXTRA_DATA_URI, 
		        		ExtensionUtils.getUriString(mContext, R.drawable.bw_play));
			}
			
			int shuffle = PlaybackService.shuffleMode(pState);
			int finish = PlaybackService.finishAction(pState);
			
	        Bundle b4 = new Bundle();
	        b4.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.sw_shuffle);
	        b4.putString(Control.Intents.EXTRA_DATA_URI, 
	        		ExtensionUtils.getUriString(mContext, BW_SHUFFLE_ICONS[shuffle]));
	        
	        Bundle b5 = new Bundle();
	        b5.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.sw_repeat);
	        b5.putString(Control.Intents.EXTRA_DATA_URI, 
	        		ExtensionUtils.getUriString(mContext, BW_FINISH_ICONS[finish]));
	
	        data[0] = b1;
	        data[1] = b2;
	        data[2] = b3;
	        data[3] = b4;
	        data[4] = b5;
        	
        	showLayout(R.layout.sw_control_2_powersave, data);
        }
        else {
        	//Extension exits powersave mode - prepare and show normal layout
        	mPowersave = false;
        	
        	Bundle b1 = new Bundle();
	        b1.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.artist);
	        b1.putString(Control.Intents.EXTRA_TEXT, pSong.artist);
	
	        Bundle b2 = new Bundle();
	        b2.putInt(Control.Intents.EXTRA_LAYOUT_REFERENCE, R.id.track);
	        b2.putString(Control.Intents.EXTRA_TEXT, pSong.title);
	        
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
			
			int shuffle = PlaybackService.shuffleMode(pState);
			int finish = PlaybackService.finishAction(pState);
			
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
        	
			//Decide what layout to show
		    Boolean altLayout = mPrefs.getBoolean(
		    mContext.getString(R.string.preference_key_border), false);
		    if (altLayout)
		    	showLayout(R.layout.sw_control_2, data);
		    else
		    	showLayout(R.layout.sw_control_2_alt, data);
        	
    		if (pCover != null) {
    			sendImage(R.id.album_cover, pCover);
    		} else {
    			sendImage(R.id.album_cover, R.drawable.fallback_cover);
    		}	
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
                	//We need to get status every click, because it can be changed anytime.
                	//Another way - globalvar and onClick listener on pref item.
                    boolean isAutoplay = mPrefs.getBoolean(
                            mContext.getString(R.string.preference_key_autoplay), false);
                    if (isAutoplay) {
                    	sendActionToPS(PlaybackService.ACTION_REWIND_SONG);
                    } else {
                        sendActionToPS(PlaybackService.ACTION_PREVIOUS_SONG);
                    }
                    sendImage(R.id.back, R.drawable.sw_back_pressed);
                    mHandler.postDelayed(new SelectToggler(R.id.back,
                            R.drawable.sw_back), SELECT_TOGGLER_MS);
                }
            });
            
            ControlView btn_next = mLayout.findViewById(R.id.next);
            btn_next.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick() {
                	//We need to get status every click, because it can be changed anytime.
                	//Another way - globalvar and onClick listener on pref item.
                    boolean isAutoplay = mPrefs.getBoolean(
                            mContext.getString(R.string.preference_key_autoplay), false);
                    if (isAutoplay) {
                    	sendActionToPS(PlaybackService.ACTION_NEXT_SONG_AUTOPLAY);
                    } else {
                        sendActionToPS(PlaybackService.ACTION_NEXT_SONG);
                    }
                    sendImage(R.id.next, R.drawable.sw_next_pressed);
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
            //TODO: This is slow and bydlo. Maybe change visibility?
            ControlView btn_play = mLayout.findViewById(R.id.play);
            btn_play.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick() {
                	if (mCurrentIsPlaying) {
	                    sendActionToPS(PlaybackService.ACTION_TOGGLE_PLAYBACK);
	                    sendImage(R.id.play, R.drawable.sw_play_pressed);
	                    mHandler.postDelayed(new SelectToggler(R.id.play,
	                            R.drawable.sw_play), SELECT_TOGGLER_MS);
                	} else {
	                    sendActionToPS(PlaybackService.ACTION_TOGGLE_PLAYBACK);
	                    sendImage(R.id.play, R.drawable.sw_pause_pressed);
	                    mHandler.postDelayed(new SelectToggler(R.id.play,
	                            R.drawable.sw_pause), SELECT_TOGGLER_MS);
                	}
                }
            });
        }
    }
    
    
    private void sendActionToPS(String act) {
    	Log.d(SWExtensionService.LOG_TAG, "Sending action to PS: " + act);
		if (act != null) {
			ComponentName service = new ComponentName(mContext, PlaybackService.class);
			Intent intent = new Intent(mContext, PlaybackService.class);
			intent.setAction(act);
			intent.setComponent(service);
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
