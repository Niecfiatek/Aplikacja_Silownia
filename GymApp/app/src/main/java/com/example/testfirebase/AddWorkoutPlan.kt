package com.example.testfirebase

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.text.InputType
import android.view.MotionEvent
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.QuerySnapshot
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.appcompat.app.AlertDialog

class AddWorkoutPlan : AppCompatActivity() {
    private val db = Firebase.firestore
    private val exerciseCollection = db.collection("Exercise")
    private lateinit var workoutPlanInputContainer: LinearLayout
    private lateinit var exerciseListContainer: LinearLayout
    private lateinit var addEx: Button
    private lateinit var addWorkPlan: Button
    private lateinit var removeEx: Button
    private lateinit var backBt: Button
    private lateinit var firstSpinner: Spinner
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var listofExercise: MutableList<String>
    private lateinit var exerciseListTextView: TextView
    private lateinit var scrollView: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_workout_plan)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        listofExercise = mutableListOf()
        workoutPlanInputContainer = findViewById(R.id.workoutPlanInputContainer)
        addEx = findViewById(R.id.addExercise)
        addWorkPlan = findViewById(R.id.addPlan)
        removeEx = findViewById(R.id.removeExercise)
        firstSpinner = findViewById(R.id.exerciseSpinner)
        backBt = findViewById(R.id.backButton)
        scrollView = findViewById(R.id.exerciseScrollView)
        exerciseListContainer = findViewById(R.id.exerciseListContainer)
        exerciseListTextView = TextView(this).apply {
            text = "Exercise list"
            textSize = 18f
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            visibility = TextView.GONE
            setTextColor(resources.getColor(android.R.color.black, null))
        }
        workoutPlanInputContainer.addView(exerciseListTextView, 1) // Add it after the spinner

        val exerciseNamesTask: Task<QuerySnapshot> = exerciseCollection.get()

        exerciseNamesTask.addOnSuccessListener { querySnapshot ->
            val exerciseNames = mutableListOf<String>()
            for (document in querySnapshot.documents) {
                val exerciseName = document.getString("Name of Exercise")
                exerciseName?.let {
                    exerciseNames.add(it)
                }
            }
            adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, exerciseNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            firstSpinner.adapter = adapter
        }

        addEx.setOnClickListener {
            val selectedExercise = firstSpinner.selectedItem as String
            if (selectedExercise.isNotEmpty()) {
                addExerciseToList(selectedExercise)
            }
        }

        removeEx.setOnClickListener {
            removeLastExercise()
        }

        addWorkPlan.setOnClickListener {
            showInputDialog()
        }

        backBt.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun addExerciseToList(exerciseName: String) {
        val exerciseButton = Button(this).apply {
            text = exerciseName
            isClickable = false
            isEnabled = false
            setBackgroundResource(R.drawable.button_regular)
            setTextColor(resources.getColor(R.color.white, null))
            backgroundTintList = null
        }
        val measureInput = EditText(this).apply {
            hint = "Enter value for $exerciseName"
            setHintTextColor(resources.getColor(android.R.color.black, null))
        }
        listofExercise.add(exerciseName)
        exerciseListContainer.addView(exerciseButton)
        exerciseListContainer.addView(measureInput)
        exerciseListTextView.visibility = TextView.VISIBLE

        // Scroll to the bottom of the ScrollView
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun removeLastExercise() {
        if (listofExercise.isNotEmpty()) {
            listofExercise.removeAt(listofExercise.size - 1)
            exerciseListContainer.removeViewAt(exerciseListContainer.childCount - 1) // Remove EditText
            exerciseListContainer.removeViewAt(exerciseListContainer.childCount - 1) // Remove Button
            if (listofExercise.isEmpty()) {
                exerciseListTextView.visibility = TextView.GONE
            }
        }
    }

    private fun addWorkPlan(workoutPlanName: String) {
        val workoutPlan = hashMapOf<String, Any>()
        workoutPlan["Name of Workout Plan"] = workoutPlanName
        val exerciseMap = hashMapOf<String, Any>()

        var i = 0
        while (i < exerciseListContainer.childCount) {
            val view = exerciseListContainer.getChildAt(i)
            if (view is Button) {
                val exerciseName = view.text.toString()
                val valueView = exerciseListContainer.getChildAt(i + 1)
                if (valueView is EditText) {
                    val valueEntered = valueView.text.toString()
                    exerciseMap[exerciseName] = valueEntered
                }
                i += 2 // Move to the next pair of Button and EditText
            } else {
                i++
            }
        }

        workoutPlan["Exercises"] = exerciseMap

        db.collection("WorkoutPlans").document().set(workoutPlan)
            .addOnSuccessListener {
                Toast.makeText(this@AddWorkoutPlan, "Successfully Added!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this@AddWorkoutPlan, "Failed!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showInputDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Workout Plan Name")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            val workoutPlanName = input.text.toString()
            if (workoutPlanName.isNotEmpty()) {
                addWorkPlan(workoutPlanName)
            } else {
                Toast.makeText(this, "Please enter a name for the workout plan", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
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
                    val i = Intent(this@AddWorkoutPlan, AddExercise::class.java)
                    startActivity(i)
                } else if (x1 > x2) {
                    val i = Intent(this@AddWorkoutPlan, Timer::class.java)
                    startActivity(i)
                }
            }
        }
        return super.onTouchEvent(touchEvent)
    }
}