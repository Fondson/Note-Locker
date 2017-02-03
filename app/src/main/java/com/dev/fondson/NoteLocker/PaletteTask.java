package com.dev.fondson.NoteLocker;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Target;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by Fondson on 2017-02-01.
 */

public class PaletteTask extends AsyncTask<Bitmap, Integer, Integer>{
    public static int mutedColour = 0;
    public static int vibrantColour = 0;

    // Do the long-running work in here
    protected Integer doInBackground(Bitmap... bitmaps) {
        if (bitmaps.length > 1) return 0;
        Palette p = Palette.from(bitmaps[0])
                .addTarget(Target.DARK_MUTED)
                .addTarget(Target.DARK_VIBRANT)
                .generate();
        mutedColour = p.getDarkMutedColor(0);
        vibrantColour = p.getDarkVibrantColor(0);
        return 0;
    }

    // This is called each time you call publishProgress()
    protected void onProgressUpdate(Integer... progress) {
    }

    // This is called when doInBackground() is finished
    protected void onPostExecute(Integer result) {}
}
