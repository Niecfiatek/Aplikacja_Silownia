package com.example.testfirebase

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
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

        back = findViewById(R.id.backBtn)

        if (MyVariables.workoutId != null) {
            printExercisesFromDocument(MyVariables.workoutId.toString())
        } else {
            Toast.makeText(this, "${MyVariables.workoutId}", Toast.LENGTH_SHORT).show()
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        back.setOnClickListener {
            showCustomDialog()
        }
    }

    private fun showCustomDialog() {
        val dialogView = layoutInflater.inflate(R.layout.pop_out_custom_dialog, null)
        val dialogBuilder = AlertDialog.Builder(this).setView(dialogView)
        val alertDialog = dialogBuilder.create()

        dialogView.findViewById<Button>(R.id.btnYes).setOnClickListener {
            val intent = Intent(applicationContext, SelectWorkout::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            alertDialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnNo).setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
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
                        val exercisesMap = documentData["Exercises"] as Map<String, String>?

                        if (exercisesMap != null) {
                            for ((exerciseName, exerciseReps) in exercisesMap) {
                                val exerciseLayout = LinearLayout(this).apply {
                                    orientation = LinearLayout.HORIZONTAL
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    ).apply {
                                        setMargins(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
                                    }
                                    background = resources.getDrawable(R.drawable.style_unchecked, null)
                                    setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
                                }

                                val exerciseTextView = TextView(this).apply {
                                    text = "$exerciseName"
                                    textSize = 16f
                                    layoutParams = LinearLayout.LayoutParams(
                                        0,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        1f
                                    )
                                }

                                exerciseTextView.setOnClickListener {
                                    val intent = Intent(this@ActiveWorkout, ActiveExercise::class.java)
                                    intent.putExtra("EXERCISE_NAME", exerciseName)
                                    startActivity(intent)
                                }

                                exerciseLayout.addView(exerciseTextView)
                                exercisesLayout.addView(exerciseLayout)
                            }
                        } else {
                            Log.d("TAG", "Brak ćwiczeń w dokumencie.")
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
