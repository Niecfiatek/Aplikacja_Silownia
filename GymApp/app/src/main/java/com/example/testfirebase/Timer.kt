package com.example.testfirebase

import CustomCountdownTimer
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.DecimalFormat
import kotlin.math.roundToInt

class Timer : AppCompatActivity() {
    private lateinit var timeTxt: TextView

    private lateinit var circularProgressBar: ProgressBar

    private var countdownTime = 0
    private var clockTime = (countdownTime * 1000).toLong()

    private var progressTime = (clockTime / 1000).toFloat()

    private lateinit var customCountdownTimer : CustomCountdownTimer

    private lateinit var editTextSeconds: EditText

    private lateinit var back: Button

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

        editTextSeconds = findViewById(R.id.editTextSeconds)

        back = findViewById(R.id.backBt)


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


        circularProgressBar.max = progressTime.toInt()

        circularProgressBar.progress = progressTime.toInt()

        customCountdownTimer.startTimer()

        val startBtn = findViewById<Button>(R.id.startBtn)

        val pauseBtn = findViewById<Button>(R.id.pauseBtn)

        val resetBtn = findViewById<Button>(R.id.resetBtn)


        startBtn.setOnClickListener {

            val inputSeconds = editTextSeconds.text.toString().toIntOrNull()

            if (inputSeconds != null && inputSeconds > 0) {

                countdownTime = inputSeconds

                clockTime = (countdownTime * 1000).toLong()

                progressTime = (clockTime / 1000).toFloat()

                customCountdownTimer.destroyTimer()

                customCountdownTimer = CustomCountdownTimer((countdownTime * 1000).toLong(), 1000)

                customCountdownTimer.onTick = { millisUntilFinished ->

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

                circularProgressBar.max = progressTime.toInt()

                circularProgressBar.progress = progressTime.toInt()

                customCountdownTimer.startTimer()

            }

        }


        var isPaused = false

        pauseBtn.setOnClickListener {

            if (!isPaused)

            {

                customCountdownTimer.pauseTimer()

                pauseBtn.text = "Resume"

                isPaused = true

            }

            else

            {

                customCountdownTimer.resumeTimer()

                pauseBtn.text = "Pause"

                isPaused = false

            }

        }

        resetBtn.setOnClickListener {

            circularProgressBar.progress = progressTime.toInt()

            customCountdownTimer.restartTimer()

        }

        back.setOnClickListener{
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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

    private var x1 = 0f

    private var y1 = 0f

    private var x2 = 0f

    private var y2 = 0f


    override fun onTouchEvent(touchEvent: MotionEvent): Boolean {

        when (touchEvent.action) {

            MotionEvent.ACTION_DOWN -> {

                x1 = touchEvent.x

                y1 = touchEvent.y

            }

            MotionEvent.ACTION_UP -> {

                x2 = touchEvent.x

                y2 = touchEvent.y

                if (x1 < x2) {

                    val i = Intent(this@Timer, AddWorkoutPlan::class.java)

                    startActivity(i)

                } else if (x1 > x2) {

                    val i = Intent(this@Timer, Calendar::class.java)

                    startActivity(i)

                }

            }

        }

        return super.onTouchEvent(touchEvent)

    }


}
