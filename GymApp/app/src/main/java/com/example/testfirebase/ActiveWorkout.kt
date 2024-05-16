package com.example.testfirebase

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore


class ActiveWorkout : AppCompatActivity() {
        private lateinit var back: Button
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_active_workout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
            back=findViewById(R.id.backBtn)
            if(MyVariables.workoutId != null)
            {
                printExercisesFromDocument(MyVariables.workoutId.toString())
            }
            else
            {
                Toast.makeText(this, "${MyVariables.workoutId}", Toast.LENGTH_SHORT).show()
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            back.setOnClickListener {
                val intent = Intent(applicationContext, MainActivity::class.java).apply {
                    flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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
                        val checkboxLayout = findViewById<LinearLayout>(R.id.checkboxLayout)
                        val exerciseList = mutableListOf<Pair<String, String>>()

                        for ((fieldName, fieldValue) in documentData) {
                            // Jeśli pole zawiera ćwiczenie, dodajemy je do listy
                            if (fieldName.startsWith("Exercise")) {
                                exerciseList.add(fieldName to fieldValue.toString())
                            }
                        }

                        exerciseList.sortBy { it.first.substringAfter("Exercise ").toInt() }

                        for ((fieldName, fieldValue) in exerciseList) {
                            val exerciseLayout = LinearLayout(this)
                            exerciseLayout.orientation = LinearLayout.HORIZONTAL
                            exerciseLayout.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )

                            val exerciseTextView = TextView(this)
                            exerciseTextView.text = fieldValue
                            exerciseTextView.textSize = 21.5f // Ustawienie większej czcionki

                            val checkBox = CheckBox(this)
                            checkBox.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            checkBox.isClickable = false
                            //checkBox.isFocusable = false
                            // Dodaj właściwości do CheckBox, jeśli jest to wymagane

                            exerciseLayout.addView(exerciseTextView)
                            checkboxLayout.addView(checkBox)
                            exercisesLayout.addView(exerciseLayout)
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

    fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }
    }

