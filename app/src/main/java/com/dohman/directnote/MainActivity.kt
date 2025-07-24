package com.dohman.directnote

import android.os.Bundle
import android.view.ScaleGestureDetector
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dohman.directnote.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {
    private lateinit var binding: ActivityMainBinding
    private var hasEditTextBeenInit = false
    private var seekbarProgress = Constants.DEFAULT_FONT_SIZE

    private lateinit var scaleGestureDetector: ScaleGestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applicationContext.let {
            if (!Prefs.isDarkModeChosen(it)) {
                setViewsAtLightMode() // Layout default is Dark Mode colored
            }
            seekbarProgress = Prefs.getSeekbarProgress(it)
        }

        binding.edtMain.post { setupEditText() }
        binding.seekbar.post { setupSlider() }

        setupOnClickListeners()
        setupOnTouchListeners()
    }

    override fun onResume() {
        super.onResume()
        if (hasEditTextBeenInit) {
            binding.edtMain.requestFocus()
        }
    }

    override fun onPause() {
        super.onPause()
        binding.edtMain.clearFocus()
    }

    private fun setupEditText() {
        binding.edtMain.textSize = Prefs.getSeekbarProgress(ctx = applicationContext) + Constants.DEFAULT_FONT_SIZE
        binding.edtMain.requestFocus()
        hasEditTextBeenInit = true
    }

    private fun setupSlider() {
        binding.seekbar.setProgress(Prefs.getSeekbarProgress(ctx = applicationContext).toInt(), false)
    }

    private fun setupOnClickListeners() {
        binding.seekbar.setOnSeekBarChangeListener(this)
        binding.btnClear.setOnClickListener { binding.edtMain.text?.clear() }
        binding.btnDarkMode.setOnClickListener { btnDarkModeAction() }
    }

    private fun setupOnTouchListeners() {
        scaleGestureDetector = ScaleGestureDetector(this, simpleOnScaleGestureListener)
        binding.edtMain.setOnTouchListener { v, event ->
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
        binding.background.setBackgroundColor(getColor(backgroundColor))

        binding.btnClear.setBackgroundResource(btnClearDrawable)
        binding.btnDarkMode.setBackgroundResource(btnDarkModeDrawable)

        accentColor.let {
            binding.edtMain.setTextColor(getColor(it))
            binding.seekbar.progressDrawable.setApiColorFilter(ContextCompat.getColor(applicationContext, it), Mode.MULTIPLY)
            binding.seekbar.thumb.setApiColorFilter(ContextCompat.getColor(applicationContext, it), Mode.SRC_ATOP)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(p0: SeekBar?) {}
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        val textSizeWithOffset = progress.toFloat() + Constants.DEFAULT_FONT_SIZE
        binding.edtMain.textSize = textSizeWithOffset
        Prefs.saveSeekbarProgress(ctx = applicationContext, factor = progress.toFloat())
    }

    private val simpleOnScaleGestureListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            seekbarProgress *= detector.scaleFactor
            seekbarProgress = 1.0f.coerceAtLeast(seekbarProgress.coerceAtMost(100.0f))
            binding.edtMain.textSize = seekbarProgress + Constants.DEFAULT_FONT_SIZE
            binding.seekbar.progress = seekbarProgress.toInt()
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            super.onScaleEnd(detector)
            Prefs.saveSeekbarProgress(ctx = applicationContext, factor = binding.seekbar.progress.toFloat())
        }
    }
}