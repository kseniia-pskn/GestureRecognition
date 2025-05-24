package com.kseniia.gestureapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics

class MainActivity : AppCompatActivity() {

    private lateinit var spinnerGesture: Spinner
    private lateinit var selectedGesture: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        spinnerGesture = findViewById(R.id.spinner_gesture)

        val gestures = listOf(
            "Turn On Light", "Turn Off Light", "Turn On Fan", "Turn Off Fan",
            "Increase Fan Speed", "Decrease Fan Speed", "Set Thermostat Temperature",
            "Digit 0", "Digit 1", "Digit 2", "Digit 3", "Digit 4", "Digit 5", "Digit 6",
            "Digit 7", "Digit 8", "Digit 9"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, gestures)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGesture.adapter = adapter

        spinnerGesture.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedGesture = gestures[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //if needed
            }
        }

        findViewById<View>(R.id.btn_next).setOnClickListener {
            val intent = Intent(this, GestureDemonstrationActivity::class.java)
            intent.putExtra("SELECTED_GESTURE", selectedGesture)
            startActivity(intent)
        }
    }
}
