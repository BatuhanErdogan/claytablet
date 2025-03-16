package com.mehrphilalethes.claytablet

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ToggleButton
import androidx.activity.ComponentActivity
// import androidx.activity.viewModels
import com.mehrphilalethes.claytablet.R.id
import com.mehrphilalethes.claytablet.view.SettingsActivity
import com.mehrphilalethes.claytablet.view.TabletCanvasView
// import com.mehrphilalethes.claytablet.viewmodel.TabletViewModel

class MainActivity : ComponentActivity() {

    // private val tabletViewModel: TabletViewModel by viewModels()
    private lateinit var tabletCanvasView: TabletCanvasView
    private var currentDeviceRotation = 180f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabletCanvasView = findViewById(id.tabletCanvasView)
        val undoButton = findViewById<Button>(id.undoButton)
        val redoButton = findViewById<Button>(id.redoButton)
        val eraserButton = findViewById<ToggleButton>(id.eraserButton)
        val clearButton = findViewById<Button>(id.clearButton)
        val snapButton = findViewById<ToggleButton>(id.snapButton)
        val scrollButton = findViewById<ToggleButton>(id.scrollButton)
        val settingsButton = findViewById<Button>(id.settingsButton)
        val rotateButton = findViewById<Button>(id.rotateButton)

        // TODO: Add ViewModel integration later when transliteration logic is ready

        undoButton.setOnClickListener { tabletCanvasView.undo() }
        redoButton.setOnClickListener { tabletCanvasView.redo() }

        eraserButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                eraserButton.setBackgroundResource(R.drawable.erase_on)
                tabletCanvasView.eraserMode = true
            } else {
                eraserButton.setBackgroundResource(R.drawable.erase_off)
                tabletCanvasView.eraserMode = false
            }
        }

        snapButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                snapButton.setBackgroundResource(R.drawable.snap_on)
                tabletCanvasView.snapMode = true
            } else {
                snapButton.setBackgroundResource(R.drawable.snap_off)
                tabletCanvasView.snapMode = false
            }
        }

        scrollButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                scrollButton.setBackgroundResource(R.drawable.scroll_on)
                tabletCanvasView.scrollMode = true
            } else {
                scrollButton.setBackgroundResource(R.drawable.scroll_off)
                tabletCanvasView.scrollMode = false
            }
        }

        clearButton.setOnClickListener { tabletCanvasView.clearTablet() }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        rotateButton.setOnClickListener {
            var newDeviceRotation = currentDeviceRotation + 90f
                currentDeviceRotation = newDeviceRotation
                tabletCanvasView.setDeviceRotation(newDeviceRotation - 180)
                rotateUIButtons(180 - newDeviceRotation)
        }
    }

    override fun onResume() {
        super.onResume()
        tabletCanvasView = findViewById(id.tabletCanvasView)
        tabletCanvasView.loadPreferences(this)
    }

    private fun rotateUIButtons(angle: Float) {
        val undoButton = findViewById<Button>(id.undoButton)
        val redoButton = findViewById<Button>(id.redoButton)
        val eraserButton = findViewById<ToggleButton>(id.eraserButton)
        val clearButton = findViewById<Button>(id.clearButton)
        val snapButton = findViewById<ToggleButton>(id.snapButton)
        val settingsButton = findViewById<Button>(id.settingsButton)
        val rotateButton = findViewById<Button>(id.rotateButton)

        val buttons = listOf(undoButton, redoButton, eraserButton, clearButton, snapButton, settingsButton, rotateButton)
        buttons.forEach { button ->
            button.animate()
                .rotation(angle)
                .setDuration(300)
                .start()
        }
    }
}