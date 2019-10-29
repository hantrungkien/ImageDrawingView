package com.kienht.imagedrawing.drawing;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * @author kienht
 * @company OICSoft
 * @since 25/10/2019
 */
public interface OnDrawBitmapAsyncCallback {
    void onDrawBitmapCreated(Canvas canvas, Bitmap bitmap);

    void onDrawBitmapError();
}
