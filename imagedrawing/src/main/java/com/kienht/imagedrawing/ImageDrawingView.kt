package com.kienht.imagedrawing

import android.content.Context
import android.graphics.*

import android.graphics.Color.*
import android.graphics.drawable.Drawable
import android.text.Layout
import android.util.AttributeSet
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.color.ColorPalette
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.kienht.imagedrawing.drawing.FreeDrawView
import com.kienht.imagedrawing.sticker.*

/**
 * @author kienht
 * @company OICSoft
 * @since 25/10/2019
 */
class ImageDrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr),
    SeekBar.OnSeekBarChangeListener,
    OnTouchDrawViewListener {

    companion object {
        val TAG = ImageDrawingView::class.java.simpleName

        private const val THICKNESS_STEP = 2
        private const val THICKNESS_MAX = 80
        private const val THICKNESS_MIN = 15

        private const val ALPHA_STEP = 1
        private const val ALPHA_MAX = 255
        private const val ALPHA_MIN = 0
    }

    private val stickerView: StickerView
    private val imageView: ImageView
    private val freeDrawView: FreeDrawView
    private val alphaSeekBar: SeekBar
    private val thicknessSeekBar: SeekBar
    private val inputFake: EditText
    private val loadingLayout: FrameLayout

    private val glideCustomerTarget: CustomTarget<Bitmap>

    init {
        inflate(context, R.layout.draw_image_view, this)
        stickerView = findViewById(R.id.drawing_sticker_view)
        imageView = findViewById(R.id.drawing_image_view)
        freeDrawView = findViewById(R.id.free_draw_view)
        loadingLayout = findViewById(R.id.layout_loading)
        inputFake = findViewById(R.id.drawing_input_fake)

        inputFake.isCursorVisible = false
        inputFake.doAfterTextChanged {
            stickerView.text = it?.toString()
        }

        val colors =
            intArrayOf(BLACK, WHITE, RED, GREEN, BLUE, CYAN, YELLOW, MAGENTA, *ColorPalette.Primary)

        freeDrawView.setOnTouchDrawViewListener(this)

        val undoButton = findViewById<ImageButton>(R.id.drawing_button_undo)
        undoButton.setOnClickListener { freeDrawView.undoLast() }
        val redoButton = findViewById<ImageButton>(R.id.drawing_button_redo)
        redoButton.setOnClickListener { freeDrawView.redoLast() }
        val clearAllButton = findViewById<ImageButton>(R.id.drawing_button_clear_all)
        clearAllButton.setOnClickListener {
            freeDrawView.clearDrawAndHistory()
            stickerView.removeAllStickers()
        }
        val colorButton = findViewById<ImageButton>(R.id.drawing_button_color)
        colorButton.setOnClickListener {
            MaterialDialog(context, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                if (context is AppCompatActivity) {
                    lifecycleOwner(context)
                }
                cornerRadius(16f)
                positiveButton(text = "Select")
                colorChooser(
                    colors = colors,
                    initialSelection = freeDrawView.paintColor
                ) { _, color ->
                    freeDrawView.paintColor = color
                }
            }
        }
        val textButton = findViewById<ImageButton>(R.id.drawing_button_text)
        textButton.setOnClickListener {
            val textSticker = TextSticker(context)
                .apply {
                    text = " "
                    setTextColor(freeDrawView.paintColor)
                    setTextAlign(Layout.Alignment.ALIGN_CENTER)
                    this.setPadding(20f, 10f, 20f, 10f)
                    resizeText()
                }

            stickerView.addSticker(textSticker)
        }

        stickerView.onStickerOperationListener = object : OnStickerOperationListener() {
            override fun onStickerTouchOutside() {
                freeDrawView.isLock = false
                inputFake.clearFocus()
                hideKeyboard()
                changeText("")
            }

            override fun onStickerAdded(sticker: Sticker) {
                freeDrawView.isLock = true
                inputFake.clearFocus()
                hideKeyboard()
                changeText("")
            }

            override fun onStickerDeleted(sticker: Sticker) {
                freeDrawView.isLock = false
                inputFake.clearFocus()
                hideKeyboard()
                changeText("")
            }

            override fun onStickerClicked(sticker: Sticker) {
                freeDrawView.isLock = true
                inputFake.clearFocus()
                hideKeyboard()
                changeText("")
            }

            override fun onStickerDoubleTapped(sticker: Sticker) {
                inputFake.requestFocus()
                inputFake.requestFocusFromTouch()
                showKeyboard()
                val currentText = stickerView.text ?: ""
                changeText(currentText)
            }
        }

        alphaSeekBar = findViewById(R.id.drawing_slider_alpha)
        alphaSeekBar.max = (ALPHA_MAX - ALPHA_MIN) / ALPHA_STEP
        val alphaProgress = (freeDrawView.paintAlpha - ALPHA_MIN) / ALPHA_STEP
        alphaSeekBar.progress = alphaProgress
        alphaSeekBar.setOnSeekBarChangeListener(this)

        thicknessSeekBar = findViewById(R.id.drawing_slider_thickness)
        thicknessSeekBar.max = (THICKNESS_MAX - THICKNESS_MIN) / THICKNESS_STEP
        val thicknessProgress = ((freeDrawView.paintWidth - THICKNESS_MIN) / THICKNESS_STEP).toInt()
        thicknessSeekBar.progress = thicknessProgress
        thicknessSeekBar.setOnSeekBarChangeListener(this)

        glideCustomerTarget = object : CustomTarget<Bitmap>() {
            override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                imageView.setImageBitmap(bitmap)
                post {
                    val imageBound = getImageBounds(imageView)
                    val freeDrawViewParams = freeDrawView.layoutParams
                    freeDrawViewParams.width = imageBound.width().toInt()
                    freeDrawViewParams.height = imageBound.height().toInt()
                    freeDrawView.layoutParams = freeDrawViewParams

                    val stickerParams = stickerView.layoutParams
                    stickerParams.width = imageBound.width().toInt()
                    stickerParams.height = imageBound.height().toInt()
                    freeDrawView.layoutParams = stickerParams
                    loadingLayout.isVisible = false
                }
            }

            override fun onLoadCleared(placeholder: Drawable?) {

            }
        }
    }

    override fun onDetachedFromWindow() {
        hideKeyboard()
        super.onDetachedFromWindow()
    }


    override fun onDrawViewTouched() {
        stickerView.setTouchOutside()
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
        if (seekBar.id == thicknessSeekBar.id) {
            freeDrawView.setPaintWidthPx((THICKNESS_MIN + progress * THICKNESS_STEP).toFloat())
        } else {
            freeDrawView.paintAlpha = ALPHA_MIN + progress * ALPHA_STEP
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar) = Unit

    @WorkerThread
    fun createBitmap(): Bitmap = stickerView.createBitmap()

    @MainThread
    fun loadImage(path: String) {
        loadingLayout.isVisible = true
        Glide.with(imageView)
            .asBitmap()
            .load(path)
            .dontAnimate()
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(glideCustomerTarget)
    }

    @MainThread
    fun loadImage(@DrawableRes drawable: Int) {
        loadingLayout.isVisible = true
        Glide.with(imageView)
            .asBitmap()
            .load(drawable)
            .dontAnimate()
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(glideCustomerTarget)
    }

    @MainThread
    fun showLoading() {
        loadingLayout.isVisible = true
    }

    @MainThread
    fun hideLoading() {
        loadingLayout.isVisible = false
    }

    private fun overlay(bmp1: Bitmap, bmp2: Bitmap): Bitmap {
        val bmOverlay = Bitmap.createBitmap(bmp1.width, bmp1.height, bmp1.config)
        val canvas = Canvas(bmOverlay)
        canvas.drawBitmap(bmp1, Matrix(), null)
        canvas.drawBitmap(bmp2, 0f, 0f, null)
        return bmOverlay
    }

    private fun getImageBounds(imageView: ImageView): RectF {
        val bounds = RectF()
        val drawable = imageView.drawable
        if (drawable != null) {
            imageView.imageMatrix.mapRect(bounds, RectF(drawable.bounds))
        }
        return bounds
    }

    private fun changeText(value: String) {
        inputFake.setText(value)
        inputFake.setSelection(value.length)
    }

    private fun showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(inputFake.windowToken, 0)
    }
}