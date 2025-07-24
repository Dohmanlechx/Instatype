package com.dohman.directnote

import android.os.Bundle
import android.view.ScaleGestureDetector
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {
    private var hasEditTextBeenInit = false
    private var seekbarProgress = Constants.DEFAULT_FONT_SIZE

    private lateinit var scaleGestureDetector: ScaleGestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        applicationContext.let {
            if (!Prefs.isDarkModeChosen(it)) {
                setViewsAtLightMode() // Layout default is Dark Mode colored
            }
            seekbarProgress = Prefs.getSeekbarProgress(it)
        }

        edt_main.post { setupEditText() }
        seekbar.post { setupSlider() }

        setupOnClickListeners()
        setupOnTouchListeners()
    }

    override fun onResume() {
        super.onResume()
        if (hasEditTextBeenInit) {
            edt_main.requestFocus()
        }
    }

    override fun onPause() {
        super.onPause()
        edt_main.clearFocus()
    }

    private fun setupEditText() {
        edt_main.textSize = Prefs.getSeekbarProgress(ctx = applicationContext) + Constants.DEFAULT_FONT_SIZE
        edt_main.requestFocus()
        hasEditTextBeenInit = true
    }

    private fun setupSlider() {
        seekbar.setProgress(Prefs.getSeekbarProgress(ctx = applicationContext).toInt(), false)
    }

    private fun setupOnClickListeners() {
        seekbar.setOnSeekBarChangeListener(this)
        btn_clear.setOnClickListener { edt_main.text?.clear() }
        btn_dark_mode.setOnClickListener { btnDarkModeAction() }
    }

    private fun setupOnTouchListeners() {
        scaleGestureDetector = ScaleGestureDetector(this, simpleOnScaleGestureListener)
        edt_main.setOnTouchListener { v, event ->
            v.performClick()

            if (event.pointerCount <= 1) {
                return@setOnTouchListener false
            } else {
                scaleGestureDetector.onTouchEvent(event)
            }
        }
    }

    private fun btnDarkModeAction() {
        val isGoingIntoDarkMode = !Prefs.isDarkModeChosen(applicationContext)
        if (isGoingIntoDarkMode) {
            setViewsAtDarkMode()
        } else {
            setViewsAtLightMode()
        }
        saveDarkModeValue(isDarkMode = isGoingIntoDarkMode)
    }

    private fun saveDarkModeValue(isDarkMode: Boolean) =
        Prefs.saveDarkModeValue(applicationContext, isDarkMode)

    private fun setViewsAtDarkMode() = dryTheWholeLayout(
        backgroundColor = R.color.colorPrimaryDark,
        accentColor = R.color.colorAccent,
        btnClearDrawable = R.drawable.ic_clear_darkmode,
        btnDarkModeDrawable = R.drawable.ic_brightness_darkmode
    )

    private fun setViewsAtLightMode() = dryTheWholeLayout(
        backgroundColor = R.color.colorAccent,
        accentColor = R.color.colorPrimaryDark,
        btnClearDrawable = R.drawable.ic_clear_lightmode,
        btnDarkModeDrawable = R.drawable.ic_brightness_lightmode
    )

    private fun dryTheWholeLayout(
        backgroundColor: Int,
        accentColor: Int,
        btnClearDrawable: Int,
        btnDarkModeDrawable: Int
    ) {
        background.setBackgroundColor(getColor(backgroundColor))

        btn_clear.setBackgroundResource(btnClearDrawable)
        btn_dark_mode.setBackgroundResource(btnDarkModeDrawable)

        accentColor.let {
            edt_main.setTextColor(getColor(it))
            seekbar.progressDrawable.setApiColorFilter(ContextCompat.getColor(applicationContext, it), Mode.MULTIPLY)
            seekbar.thumb.setApiColorFilter(ContextCompat.getColor(applicationContext, it), Mode.SRC_ATOP)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(p0: SeekBar?) {}
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        val textSizeWithOffset = progress.toFloat() + Constants.DEFAULT_FONT_SIZE
        edt_main.textSize = textSizeWithOffset
        Prefs.saveSeekbarProgress(ctx = applicationContext, factor = progress.toFloat())
    }

    private val simpleOnScaleGestureListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            seekbarProgress *= detector.scaleFactor
            seekbarProgress = 1.0f.coerceAtLeast(seekbarProgress.coerceAtMost(100.0f))
            edt_main.textSize = seekbarProgress + Constants.DEFAULT_FONT_SIZE
            seekbar.progress = seekbarProgress.toInt()
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            super.onScaleEnd(detector)
            Prefs.saveSeekbarProgress(ctx = applicationContext, factor = seekbar.progress.toFloat())
        }
    }
}