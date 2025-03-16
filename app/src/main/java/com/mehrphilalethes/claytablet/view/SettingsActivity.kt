package com.mehrphilalethes.claytablet.view

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import androidx.activity.ComponentActivity
import com.mehrphilalethes.claytablet.R

class SettingsActivity : ComponentActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences("tablet_prefs", MODE_PRIVATE)

        val headSizeSeekBar = findViewById<SeekBar>(R.id.headSizeSeekBar)
        val winkelSizeSeekBar = findViewById<SeekBar>(R.id.winkelSizeSeekBar)
        val bodyThicknessSeekBar = findViewById<SeekBar>(R.id.bodyThicknessSeekBar)
        val thresholdSeekBar = findViewById<SeekBar>(R.id.thresholdSeekBar)
        val saveButton = findViewById<Button>(R.id.doneButton)
        val resetButton = findViewById<Button>(R.id.resetButton)
        val previewView = findViewById<WedgePreviewView>(R.id.wedgePreviewView)

        // Load existing preferences explicitly
        headSizeSeekBar.progress = prefs.getInt("headSize", 13)
        winkelSizeSeekBar.progress = prefs.getInt("winkelSize", 12)
        bodyThicknessSeekBar.progress = prefs.getInt("bodyThickness", 8)
        thresholdSeekBar.progress = prefs.getInt("threshold", 148)

        fun updateView() {
            previewView.updateParameters(
                headSizeSeekBar.progress / 100f,
                winkelSizeSeekBar.progress / 100f,
                bodyThicknessSeekBar.progress.toFloat(),
                thresholdSeekBar.progress.toFloat()
            )
        }
        updateView()

        // Update preview explicitly when sliders change
        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) { updateView() }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        }

        headSizeSeekBar.setOnSeekBarChangeListener(listener)
        winkelSizeSeekBar.setOnSeekBarChangeListener(listener)
        bodyThicknessSeekBar.setOnSeekBarChangeListener(listener)
        thresholdSeekBar.setOnSeekBarChangeListener(listener)

        resetButton.setOnClickListener {
            headSizeSeekBar.progress = 13
            winkelSizeSeekBar.progress = 12
            bodyThicknessSeekBar.progress = 8
            thresholdSeekBar.progress = 148
        }

        saveButton.setOnClickListener {
            prefs.edit().apply {
                putInt("headSize", headSizeSeekBar.progress)
                putInt("winkelSize", winkelSizeSeekBar.progress)
                putInt("bodyThickness", bodyThicknessSeekBar.progress)
                putInt("threshold", thresholdSeekBar.progress)
                apply()
            }
            Log.d("Setting: ", "Head: ${headSizeSeekBar.progress}\n" +
                    "Winkel: ${winkelSizeSeekBar.progress}]n" +
                    "Threshold: ${thresholdSeekBar.progress}")
            finish()
        }
    }
}
