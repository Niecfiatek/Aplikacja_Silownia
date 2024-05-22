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

class ActiveExercise : AppCompatActivity() {

    private lateinit var exerciseTable: TableLayout
    private var setCount = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_exercise)

        exerciseTable = findViewById(R.id.exerciseTable)
        val addRowButton: Button = findViewById(R.id.addRowButton)
        val exerciseTitle: TextView = findViewById(R.id.exerciseTitle)

        val exerciseName = intent.getStringExtra("EXERCISE_NAME")
        exerciseTitle.text = exerciseName

        addRowButton.setOnClickListener {
            addNewRow()
        }
    }

    private fun addNewRow() {
        val newRow = TableRow(this)
        newRow.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )

        val setTextView = TextView(this)
        setTextView.text = setCount.toString()
        setTextView.gravity = Gravity.CENTER
        setTextView.setPadding(8, 8, 8, 8)
        setTextView.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        setTextView.setBackgroundResource(R.drawable.cell_border)
        setTextView.height = dpToPx(40)  // Stała wysokość w dp
        newRow.addView(setTextView)

        val repsEditText = EditText(this)
        repsEditText.hint = "Reps"
        repsEditText.gravity = Gravity.CENTER
        repsEditText.inputType = InputType.TYPE_CLASS_NUMBER
        repsEditText.setPadding(8, 8, 8, 8)
        repsEditText.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        repsEditText.setBackgroundResource(R.drawable.cell_border)
        repsEditText.height = dpToPx(40)  // Stała wysokość w dp
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

        val timeEditText = EditText(this)
        timeEditText.hint = "Time"
        timeEditText.gravity = Gravity.CENTER
        timeEditText.inputType = InputType.TYPE_CLASS_NUMBER
        timeEditText.setPadding(8, 8, 8, 8)
        timeEditText.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        timeEditText.setBackgroundResource(R.drawable.cell_border)
        timeEditText.height = dpToPx(40)  // Stała wysokość w dp
        newRow.addView(timeEditText)

        exerciseTable.addView(newRow)
        setCount++
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}