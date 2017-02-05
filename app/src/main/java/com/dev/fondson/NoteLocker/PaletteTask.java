package com.dev.fondson.NoteLocker;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Target;
import android.util.MutableInt;

/**
 * Created by Fondson on 2017-02-01.
 */

public class PaletteTask extends AsyncTask<Bitmap, Integer, Integer>{
    public static MutableInt mutedColour = new MutableInt(0);
    public static MutableInt vibrantColour = new MutableInt(0);
    final static int alphaValue = 191;

    // Do the long-running work in here
    protected Integer doInBackground(Bitmap... bitmaps) {
        if (bitmaps.length > 1) return 0;
        Palette p = Palette.from(bitmaps[0])
                .addTarget(Target.DARK_MUTED)
                .addTarget(Target.DARK_VIBRANT)
                .generate();
        mutedColour.value = p.getDarkMutedColor(Color.BLACK);
        vibrantColour.value = p.getDarkVibrantColor(Color.BLACK);
        return 0;
    }

    // This is called each time you call publishProgress()
    protected void onProgressUpdate(Integer... progress) {
    }

    // This is called when doInBackground() is finished
    protected void onPostExecute(Integer result) {}
}
