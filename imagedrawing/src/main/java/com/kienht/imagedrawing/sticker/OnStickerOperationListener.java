package com.kienht.imagedrawing.sticker;

import androidx.annotation.NonNull;

/**
 * @author kienht
 * @company OICSoft
 * @since 29/10/2019
 */
public abstract class OnStickerOperationListener {

    public void onStickerAdded(@NonNull Sticker sticker) {
    }

    public void onStickerClicked(@NonNull Sticker sticker) {
    }

    public void onStickerDeleted(@NonNull Sticker sticker) {
    }

    public void onStickerDragFinished(@NonNull Sticker sticker) {
    }

    public void onStickerTouchedDown(@NonNull Sticker sticker) {
    }

    public void onStickerZoomFinished(@NonNull Sticker sticker) {
    }

    public void onStickerFlipped(@NonNull Sticker sticker) {
    }

    public void onStickerDoubleTapped(@NonNull Sticker sticker) {
    }

    public void onStickerTouchOutside() {
    }
}
