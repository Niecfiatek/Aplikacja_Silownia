package com.example.testfirebase

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class AddWorkoutPlan : AppCompatActivity() {
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val exerciseCollection = firestore.collection("Exercise")
    private lateinit var workoutPlanInput : AutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_workout_plan)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }
        fetchExerciseNames()
    }
    private fun fetchExerciseNames() {
        workoutPlanInput=findViewById(R.id.NameOfExerciseInput)
        val exerciseNames = mutableListOf<String>()
        exerciseCollection.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val exerciseName = document.getString("Name of Exercise")
                    exerciseName?.let {
                        exerciseNames.add(it)
                    }
                }
                val arrayAdapterWorkoutPlan = ArrayAdapter(this, R.layout.dropdown_item, exerciseNames)
                workoutPlanInput.setAdapter(arrayAdapterWorkoutPlan)
            }
            .addOnFailureListener { exception ->
                // Obsługa błędu pobierania danych
                // W przypadku niepowodzenia, tutaj możesz obsłużyć błąd, np. wyświetlając komunikat użytkownikowi
            }
    }

}