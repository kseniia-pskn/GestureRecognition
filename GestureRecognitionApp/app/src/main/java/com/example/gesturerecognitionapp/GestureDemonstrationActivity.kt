package com.kseniia.gestureapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.kseniia.gestureapp.R

class GestureDemonstrationActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var selectedGesture: String
    private var replayCount = 0
    private val maxReplayCount = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gesture_demonstration)

        selectedGesture = intent.getStringExtra("SELECTED_GESTURE") ?: "Unknown Gesture"

        findViewById<TextView>(R.id.tv_selected_gesture).text = "Gesture: $selectedGesture"

        videoView = findViewById(R.id.video_expert_gesture)
        val videoPath = "android.resource://" + packageName + "/raw/" + getGestureVideoFileName(selectedGesture)
        videoView.setVideoURI(Uri.parse(videoPath))
        videoView.start()

        findViewById<Button>(R.id.btn_replay).setOnClickListener {
            if (replayCount < maxReplayCount) {
                videoView.start()
                replayCount++
            }
        }

        findViewById<Button>(R.id.btn_practice).setOnClickListener {
            val intent = Intent(this, GesturePracticeActivity::class.java)
            intent.putExtra("SELECTED_GESTURE", selectedGesture)
            startActivity(intent)
        }
    }

    private fun getGestureVideoFileName(gesture: String): String {
        return when (gesture) {
            "Turn On Light" -> "h_lighton"
            "Turn Off Light" -> "h_lightoff"
            "Turn On Fan" -> "h_fanon"
            "Turn Off Fan" -> "h_fanoff"
            "Increase Fan Speed" -> "h_increasefanspeed"
            "Decrease Fan Speed" -> "h_decreasefanspeed"
            "Set Thermostat Temperature" -> "h_setthermo"
            "Digit 0" -> "h_0"
            "Digit 1" -> "h_1"
            "Digit 2" -> "h_2"
            "Digit 3" -> "h_3"
            "Digit 4" -> "h_4"
            "Digit 5" -> "h_5"
            "Digit 6" -> "h_6"
            "Digit 7" -> "h_7"
            "Digit 8" -> "h_8"
            "Digit 9" -> "h_9"
            else -> "default"
        }
    }
}
