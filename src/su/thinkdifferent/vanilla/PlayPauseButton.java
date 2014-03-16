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

import ch.blinkenlights.android.vanilla.PlaybackService;
import android.graphics.Bitmap;

/**
 * The play pause button handles media player play pause.
 */
public class PlayPauseButton extends ControlButton {

    private final Bitmap mPlayBitmap;

    private final Bitmap mPlayPressedBitmap;

    private final Bitmap mPauseBitmap;

    private final Bitmap mPausePressedBitmap;

    private boolean mIsPlaying;

    /**
     * Create play pause button.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     * @param mediaPlayerAdapter The media player adapter.
     * @param playBitmap The play bitmap.
     * @param playPressedBitmap The play pressed bitmap.
     * @param pauseBitmap The pause bitmap.
     * @param pausePressedBitmap The pause pressed bitmap
     */
    public PlayPauseButton(final int x, final int y, 
            final Bitmap playBitmap, final Bitmap playPressedBitmap, final Bitmap pauseBitmap,
            final Bitmap pausePressedBitmap) {
        // Set the play bitmap default just to set anything.
        super(x, y, playBitmap, playPressedBitmap);
        mPlayBitmap = playBitmap;
        mPlayPressedBitmap = playPressedBitmap;
        mPauseBitmap = pauseBitmap;
        mPausePressedBitmap = pausePressedBitmap;

        // Update to show the correct image.
        //TODO
    }

    /**
     * Update the button to show the correct icon.
     *
     * @param isPlaying Is the media player currently playing.
     */
    public void update(final boolean isPlaying) {
        mIsPlaying = isPlaying;
        if (mIsPlaying) {
            mBitmap = mPauseBitmap;
            mPressedBitmap = mPausePressedBitmap;
        } else {
            mBitmap = mPlayBitmap;
            mPressedBitmap = mPlayPressedBitmap;
        }
    }
}
