package com.example.testfirebase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class ActiveWorkout : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_active_workout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val trainingName = intent.getStringExtra("trainingName")
        findDocument(trainingName.toString()) {documentId ->
            if(documentId != null)
            {
                printExercisesFromDocument(documentId)
            }
            else
            {
                Toast.makeText(this, "$trainingName", Toast.LENGTH_SHORT).show()
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun findDocument(trainingName: String, callback: (String?) -> Unit) {
        val collectionReference = FirebaseFirestore.getInstance().collection("WorkoutPlans")
        collectionReference.whereEqualTo("Name of Workout Plan", trainingName)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val documentId = document.id
                    callback(documentId)
                    return@addOnSuccessListener  // Dodaj ten return, aby przerwać iterację po znalezieniu pasującego dokumentu
                }
                callback(null) // Wywołaj funkcję zwrotną z wartością null, jeśli nie znaleziono pasującego dokumentu
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Błąd podczas pobierania dokumentów: $exception")
                callback(null)
            }
    }

    private fun printExercisesFromDocument(documentId: String) {
        val collectionReference = FirebaseFirestore.getInstance().collection("WorkoutPlans")
        val documentReference = collectionReference.document(documentId)

        documentReference.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val documentData = documentSnapshot.data
                    if (documentData != null) {
                        val exercisesLayout = findViewById<LinearLayout>(R.id.exercisesLayout)
                        val exerciseList = mutableListOf<Pair<String, String>>()

                        for ((fieldName, fieldValue) in documentData) {
                            // Jeśli pole zawiera ćwiczenie, dodajemy je do listy
                            if (fieldName.startsWith("Exercise")) {
                                exerciseList.add(fieldName to fieldValue.toString())
                            }
                        }

                        exerciseList.sortBy { it.first.substringAfter("Exercise ").toInt() }

                        for ((fieldName, fieldValue) in exerciseList) {
                            val exerciseTextView = TextView(this)
                            exerciseTextView.text = "$fieldValue"
                            exercisesLayout.addView(exerciseTextView)
                        }
                    } else {
                        Log.d("TAG", "Brak danych w dokumencie.")
                    }
                } else {
                    Log.d("TAG", "Dokument nie istnieje.")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Błąd podczas pobierania dokumentu: $exception")
            }
    }

}