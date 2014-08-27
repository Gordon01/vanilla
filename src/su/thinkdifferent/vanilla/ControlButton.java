/*
Copyright (C) 2014  Alexander Sergeev <etc9053@gmail.com>
Copyright (c) 2011, Sony Ericsson Mobile Communications AB
   
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

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.Dbg;
import com.sonyericsson.extras.liveware.extension.util.control.ControlTouchEvent;

import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * The control button class is used to create a button on a control extension.
 */
public class ControlButton {
    private final Rect mRect;

    protected Bitmap mBitmap;

    protected Bitmap mPressedBitmap;

    private boolean mIsPressed = false;

    /**
     * Create control button.
     *
     * @param x The x position of the button.
     * @param y The y position of the button.
     * @param bitmap The bitmap to show.
     * @param pressedBitmap The bitmap to show when the button is pressed.
     */
    public ControlButton(final int x, final int y, final Bitmap bitmap, final Bitmap pressedBitmap) {
        mBitmap = bitmap;
        mPressedBitmap = pressedBitmap;
        mRect = new Rect(x, y, x + mBitmap.getWidth(), y + mBitmap.getHeight());
    }

    /**
     * Get the x position of the button.
     *
     * @return The x position.
     */
    public int getX() {
        return mRect.left;
    }

    /**
     * Get the y position of the button.
     *
     * @return The y position.
     */
    public int getY() {
        return mRect.top;
    }

    /**
     * Get the button width.
     *
     * @return The button width.
     */
    public int getWidth() {
        return mRect.width();
    }

    /**
     * Get the button height.
     *
     * @return The button height.
     */
    public int getHeight() {
        return mRect.height();
    }

    /**
     * Get the bitmap to show. Which bitmap to show depends on the button state.
     *
     * @return The bitmap to show.
     */
    public Bitmap getBitmap() {
        if (mIsPressed) {
            return mPressedBitmap;
        } else {
            return mBitmap;
        }
    }

    /**
     * Is the button currently pressed.
     *
     * @return True if the button is currently pressed.
     */
    public boolean isPressed() {
        return mIsPressed;
    }

    /**
     * Handle touch event. Does nothing if the touch event is not for this
     * button.
     *
     * @param event The touch event.
     */
    public void checkTouchEvent(final ControlTouchEvent event) {
        if (mRect.contains(event.getX(), event.getY())) {
            onTouch(event);
        } else if (mIsPressed) {
            Dbg.v("Button no longer in focus.");
            mIsPressed = false;
        }
    }

    /**
     * Touch event for this button.
     *
     * @param event The touch event.
     */
    public void onTouch(final ControlTouchEvent event) {
        if (event.getAction() == Control.Intents.TOUCH_ACTION_RELEASE) {
            if (mIsPressed) {
                onClick();
            } else {
                Dbg.v("Release button not pressed. Ignoring.");
            }
            mIsPressed = false;
        }

        if (event.getAction() == Control.Intents.TOUCH_ACTION_PRESS) {
            mIsPressed = true;
        }

        // Nothing to do on move.
    }

    /**
     * Button has been clicked. (Touch release event.)
     */
    public void onClick() {
        // Do the action.
    }
}
