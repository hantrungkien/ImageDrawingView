package com.kienht.imagedrawing.drawing;

import android.graphics.Bitmap;

/**
 * @author kienht
 * @company OICSoft
 * @since 25/10/2019
 */
public interface OnCreateBitmapCallback {

    void onBitmapCreated(Bitmap bitmap);

    void onBitmapCreationError();
}
