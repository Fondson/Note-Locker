package com.dev.fondson.NoteLocker;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.os.AsyncTask;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Target;
import android.util.Log;
import android.util.MutableInt;

import java.util.List;

/**
 * Created by Fondson on 2017-02-01.
 */

public class PaletteTask extends AsyncTask<Bitmap, Integer, Integer>{
    public static MutableInt dominantColour = new MutableInt(0);
    public static MutableInt dominantTextColour = new MutableInt(0);
    public static MutableInt secondDominantColour = new MutableInt(0);
    public static MutableInt secondDominantTextColour = new MutableInt(0);
    private List<Palette.Swatch> swatchList = null;
    private int maxPop;

    // Do the long-running work in here
    protected Integer doInBackground(Bitmap... bitmaps) {
        if (bitmaps.length > 1) return 0;
        Palette p = Palette.from(bitmaps[0]).generate();

        swatchList = p.getSwatches();
        // setting dominantColour
        Palette.Swatch dc = findDominantSwatch(swatchList);
        dominantColour.value = dc.getRgb();
        dominantTextColour.value = dc.getBodyTextColor();

        // setting secondDominantColour
        Palette.Swatch sdc = findSecondDominantSwatch(swatchList);
        if (sdc != null){
            secondDominantColour.value = sdc.getRgb();
            secondDominantTextColour.value = sdc.getBodyTextColor();
        }else{
            secondDominantColour.value = Color.BLACK;
            secondDominantTextColour.value = Color.WHITE;
        }

        return 0;
    }

    private Palette.Swatch findDominantSwatch(List<Palette.Swatch> swatchList) {
        maxPop = Integer.MIN_VALUE;
        Palette.Swatch maxSwatch = null;
        for (int i = 0, count = swatchList.size(); i < count; i++) {
            Palette.Swatch swatch = swatchList.get(i);
            if (swatch.getPopulation() > maxPop) {
                maxSwatch = swatch;
                maxPop = swatch.getPopulation();
            }
        }
        return maxSwatch;
    }

    private Palette.Swatch findSecondDominantSwatch(List<Palette.Swatch> swatchList) {
        int secondMaxPop = Integer.MIN_VALUE;
        Palette.Swatch secondMaxSwatch = null;
        for (int i = 0, count = swatchList.size(); i < count; i++) {
            Palette.Swatch swatch = swatchList.get(i);
            if (swatch.getPopulation() < maxPop && swatch.getPopulation() > secondMaxPop) {
                secondMaxSwatch = swatch;
                maxPop = swatch.getPopulation();
            }
        }
        return secondMaxSwatch;
    }

    // This is called each time you call publishProgress()
    protected void onProgressUpdate(Integer... progress) {
    }

    // This is called when doInBackground() is finished
    protected void onPostExecute(Integer result) {}
}
