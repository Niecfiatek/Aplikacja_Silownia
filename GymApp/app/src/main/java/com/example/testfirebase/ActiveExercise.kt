package com.example.testfirebase

import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import CustomCountdownTimer
import android.content.Intent
import android.view.MotionEvent
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.DecimalFormat
import kotlin.math.roundToInt

class ActiveExercise : AppCompatActivity() {

    private lateinit var timeTxt: TextView
    private lateinit var circularProgressBar: ProgressBar
    private var countdownTime = 0
    private var clockTime = (countdownTime * 1000).toLong()
    private var progressTime = (clockTime / 1000).toFloat()
    private lateinit var customCountdownTimer : CustomCountdownTimer
    private lateinit var editTextSeconds: EditText
    private lateinit var back: Button
    private lateinit var finish: Button
    private lateinit var exerciseTable: TableLayout
    private var setCount = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_exercise)

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressedMethod()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        exerciseTable = findViewById(R.id.exerciseTable)
        val addRowButton: Button = findViewById(R.id.addRowButton)
        val deleteRowButton: Button = findViewById(R.id.deleteRowButton)
        val exerciseTitle: TextView = findViewById(R.id.exerciseTitle)

        val exerciseName = intent.getStringExtra("EXERCISE_NAME")
        val exerciseReps = intent.getStringExtra("EXERCISE_REPS")
        val desiredRepsTextView = findViewById<TextView>(R.id.desiredReps)
        val desiredRepsText = desiredRepsTextView.text.toString()
        val updatedDesiredRepsText = "$desiredRepsText $exerciseReps"
        desiredRepsTextView.text = updatedDesiredRepsText
        exerciseTitle.text = exerciseName

        addRowButton.setOnClickListener {
            addNewRow()
        }

        deleteRowButton.setOnClickListener {
            removeLastRow()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        timeTxt = findViewById(R.id.timeText)
        circularProgressBar = findViewById(R.id.circularProgresssBar)
        editTextSeconds = findViewById(R.id.editTextSeconds)
        back = findViewById(R.id.backBt)
        finish = findViewById(R.id.finishBt)

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
            val intent = Intent(applicationContext, ActiveWorkout::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        finish.setOnClickListener {
            val exerciseName = intent.getStringExtra("EXERCISE_NAME")
            val exerciseDataString = readAllTable()

            val intent = Intent()
            intent.putExtra("EXERCISE_NAME", exerciseName)
            intent.putExtra("EXERCISE_DATA_STRING", exerciseDataString)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private val exerciseData = mutableListOf<Map<String, Any>>()

    private fun addNewRow() {
        val newRow = TableRow(this)
        newRow.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )

        val setEditView = EditText(this)
        setEditView.isFocusable = false
        setEditView.isFocusableInTouchMode = false
        setEditView.isClickable = false
        setEditView.isLongClickable = false
        setEditView.setText(setCount.toString())
        setEditView.gravity = Gravity.CENTER
        setEditView.setPadding(8, 8, 8, 8)
        setEditView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        setEditView.setBackgroundResource(R.drawable.cell_border)
        setEditView.height = dpToPx(40)
        newRow.addView(setEditView)

        val repsEditText = EditText(this)
        repsEditText.hint = "Reps"
        repsEditText.gravity = Gravity.CENTER
        repsEditText.inputType = InputType.TYPE_CLASS_NUMBER
        repsEditText.setPadding(8, 8, 8, 8)
        repsEditText.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        repsEditText.setBackgroundResource(R.drawable.cell_border)
        repsEditText.height = dpToPx(40)
        newRow.addView(repsEditText)

        val weightEditText = EditText(this)
        weightEditText.hint = "Weight"
        weightEditText.gravity = Gravity.CENTER
        weightEditText.inputType = InputType.TYPE_CLASS_NUMBER
        weightEditText.setPadding(8, 8, 8, 8)
        weightEditText.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        weightEditText.setBackgroundResource(R.drawable.cell_border)
        weightEditText.height = dpToPx(40)  // Stała wysokość w dp
        newRow.addView(weightEditText)

        exerciseTable.addView(newRow)
        setCount++

    }

    private fun removeLastRow() {
        val rowCount = exerciseTable.childCount
        if (rowCount > 1) {
            exerciseTable.removeViewAt(rowCount - 1)
            setCount--
        }
    }

    private fun readAllTable(): String {
        var totalSets = 0
        var totalReps = 0
        var totalWeight = 0.0

        for (i in 1 until exerciseTable.childCount) {  // Start from 1 to skip the header row
            val row = exerciseTable.getChildAt(i) as TableRow
            val repsEditText = row.getChildAt(1) as EditText
            val weightEditText = row.getChildAt(2) as EditText

            val reps = repsEditText.text.toString().toIntOrNull() ?: 0
            val weight = weightEditText.text.toString().toDoubleOrNull() ?: 0.0

            totalSets++
            totalReps += reps
            totalWeight += weight
        }

        val averageReps = if (totalSets > 0) totalReps.toDouble() / totalSets else 0.0
        val averageWeight = if (totalSets > 0) totalWeight / totalSets else 0.0

        val decimalFormat = DecimalFormat("#.##")
        val formattedReps = decimalFormat.format(averageReps)
        val formattedWeight = decimalFormat.format(averageWeight)

        val resultString = "$totalSets,$formattedReps,$formattedWeight"
        return resultString
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
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