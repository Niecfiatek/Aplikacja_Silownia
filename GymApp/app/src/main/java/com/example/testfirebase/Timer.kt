package com.example.testfirebase

import CustomCountdownTimer
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.DecimalFormat
import kotlin.math.roundToInt

class Timer : AppCompatActivity() {
    private lateinit var timeTxt: TextView
    private lateinit var circularProgressBar: ProgressBar
    private val countdownTime = 60
    private val clockTime = (countdownTime * 1000).toLong()
    private val progressTime = clockTime.toInt()
    private lateinit var customCountdownTimer : CustomCountdownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressedMethod()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        timeTxt = findViewById(R.id.timeText)
        circularProgressBar = findViewById(R.id.circularProgresssBar)

        var secondsLeft = 0
        customCountdownTimer = object : CustomCountdownTimer(clockTime, 1000) {}
        customCountdownTimer.onTick = {millisUntilFinished ->
                val second = (millisUntilFinished / 1000.0f).roundToInt()
                if (second != secondsLeft) {
                    secondsLeft = second
                    timerFormat(
                        secondsLeft,
                        timeTxt
                    )
                }
            }

        customCountdownTimer.onFinish = {
            timerFormat(
                0,
                timeTxt
            )
        }

        circularProgressBar.max = progressTime
        circularProgressBar.progress = progressTime
        customCountdownTimer.startTimer()

        val pauseBtn = findViewById<Button>(R.id.pauseBtn)
        val resumeBtn = findViewById<Button>(R.id.resumeBtn)
        val resetBtn = findViewById<Button>(R.id.resetBtn)

        pauseBtn.setOnClickListener {
            customCountdownTimer.pauseTimer()
        }

        resumeBtn.setOnClickListener {
            customCountdownTimer.resumeTimer()
        }

        resetBtn.setOnClickListener {
            circularProgressBar.progress = progressTime
            customCountdownTimer.restartTimer()
        }
    }

    private fun timerFormat(secondsLeft: Int, timeText: TextView) {
        circularProgressBar.progress = secondsLeft
        val decimalFormat = DecimalFormat("00")
        val min = (secondsLeft % 3600) / 60
        val seconds = secondsLeft % 60

        val timeFormat = decimalFormat.format(min) + ":" + decimalFormat.format(seconds)
        timeTxt.text = timeFormat
    }

    private fun onBackPressedMethod() {
        customCountdownTimer.destroyTimer()
        finish()
    }

    override fun onPause() {
        customCountdownTimer.pauseTimer()
        super.onPause()
    }

    override fun onResume() {
        customCountdownTimer.resumeTimer()
        super.onResume()
    }

    override fun onDestroy() {
        customCountdownTimer.destroyTimer()
        super.onDestroy()
    }
}