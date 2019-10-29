package com.kienht.imagedrawing

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.kienht.imagedrawing.drawing.OnCreateBitmapCallback

/**
 * @author kienht
 * @company OICSoft
 * @since 25/10/2019
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawImageView = findViewById<ImageDrawingView>(R.id.image_drawing)
        drawImageView.loadImage(R.drawable.iphonex)

        drawImageView.onCreateBitmapCallback = object : OnCreateBitmapCallback {
            override fun onBitmapCreated(bitmap: Bitmap?) {
            }

            override fun onBitmapCreationError() {
            }
        }

//        drawImageView.getBitmap()
    }
}