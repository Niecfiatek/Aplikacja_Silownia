package com.example.testfirebase

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.text.InputType
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddWorkoutPlan : AppCompatActivity() {
    private val db = Firebase.firestore
    private val exerciseCollection = db.collection("Exercise")
    private lateinit var workoutPlanInputContainer: LinearLayout
    private lateinit var addEx: Button
    private lateinit var addWorkPlan: Button
    private lateinit var removeEx: Button
    private lateinit var firstSpinner: Spinner
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var listofExercise: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_workout_plan)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        workoutPlanInputContainer = findViewById(R.id.workoutPlanInputContainer)
        addEx = findViewById(R.id.addExercise)
        addWorkPlan = findViewById(R.id.addPlan)
        removeEx = findViewById(R.id.removeExercise)
        firstSpinner = findViewById(R.id.exerciseSpinner)

        val exerciseNamesTask: Task<QuerySnapshot> = exerciseCollection.get()
        exerciseNamesTask.addOnSuccessListener { querySnapshot ->
            val exerciseNames = mutableListOf<String>()
            for (document in querySnapshot.documents) {
                val exerciseName = document.getString("Name of Exercise")
                exerciseName?.let {
                    exerciseNames.add(it)
                }
            }
            adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, exerciseNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            firstSpinner.adapter = adapter
        }

        addEx.setOnClickListener {
            addSpinner()
        }
        removeEx.setOnClickListener{
            removeSpinner()
        }
        addWorkPlan.setOnClickListener{
            addWorkPlan()
        }

    }

    private fun addSpinner() {
        val newSpinner = Spinner(this)
        newSpinner.adapter = adapter
        // Kopiowanie ustawień pierwszego Spinnera
        newSpinner.background = firstSpinner.background
        newSpinner.prompt = firstSpinner.prompt
        newSpinner.setSelection(firstSpinner.selectedItemPosition)

        // Pobranie marginesów z pierwszego Spinnera
        val params = firstSpinner.layoutParams as LinearLayout.LayoutParams

        // Ustawienie marginesów dla nowego Spinnera
        val newParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        newParams.marginStart = params.marginStart
        newParams.marginEnd = params.marginEnd
        newSpinner.layoutParams = newParams
        workoutPlanInputContainer.addView(newSpinner)
    }

    private fun removeSpinner() {
        if (workoutPlanInputContainer.childCount > 1) {
            val lastIndex = workoutPlanInputContainer.childCount - 1
            workoutPlanInputContainer.removeViewAt(lastIndex)
        }
    }
    private fun addWorkPlan() {
        //listofExercise.clear() // Wyczyść listę przed dodaniem nowych ćwiczeń
        for (i in 0 until workoutPlanInputContainer.childCount - 1) {
            val view = workoutPlanInputContainer.getChildAt(i)
            if (view is Spinner) {
                val selectedExercise = view.selectedItem as String
                listofExercise.add(selectedExercise)
            }
        }

        // Przypisanie danych do bazy danych dla każdego ćwiczenia osobno
        listofExercise.forEach { exercise ->
            val workoutPlan = hashMapOf(
                "Exercise" to exercise
            )
            // Dodanie danych do bazy danych
            db.collection("WorkoutPlans").add(workoutPlan)
                .addOnSuccessListener {
                    Toast.makeText(this@AddWorkoutPlan, "Successfully Added!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this@AddWorkoutPlan, "Failed!", Toast.LENGTH_SHORT).show()
                }
        }
    }
}