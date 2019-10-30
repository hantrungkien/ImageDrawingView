package com.kienht.imagedrawing

import android.content.Context
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.kienht.imagedrawing.drawing.OnCreateBitmapCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * @author kienht
 * @company OICSoft
 * @since 25/10/2019
 */
class MainActivity : AppCompatActivity() {

    lateinit var drawImageView: ImageDrawingView
    lateinit var imageTest: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawImageView = findViewById<ImageDrawingView>(R.id.image_drawing)
        drawImageView.loadImage(R.drawable.iphonex)

        imageTest = findViewById<ImageView>(R.id.image_test)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit_photo, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_done -> {
                getBitmap()
            }
        }
        return true
    }

    private fun getBitmap() {
        lifecycleScope.launch {
            drawImageView.showLoading()
            val path = withContext(Dispatchers.IO) {
                val bitmap = drawImageView.createBitmap()
                getScreenshotPath()
                    .takeUnless { it.isEmpty() }
                    ?.let { bitmap.saveToFile(it) }
                    ?.absolutePath
            }
            drawImageView.hideLoading()
            Glide.with(imageTest).clear(imageTest)
            Glide.with(imageTest)
                .load(path)
                .dontAnimate()
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageTest)
        }
    }
}

fun Context.getScreenshotPath(): String {
    val folderPath = "${cacheDir}/screenshot"
    val folder = File(folderPath)
    if (!folder.exists()) {
        folder.mkdirs()
    }
    return "${folderPath}/${System.currentTimeMillis()}.png"
}

fun Bitmap.saveToFile(path: String): File? {
    return try {
        val file = File(path)
        file.createNewFile()
        FileOutputStream(file).use { out -> this.compress(Bitmap.CompressFormat.PNG, 100, out) }
        file
    } catch (e: Exception) {
        null
    }
}