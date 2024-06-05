package com.example.testfirebase

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class ActiveWorkout : AppCompatActivity() {
    private lateinit var back: Button
    private lateinit var finish: Button
    private val REQUEST_CODE = 1
    private val exerciseCheckedState = HashMap<String, Boolean>()

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
        finish = findViewById(R.id.finishBtn)

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

        finish.setOnClickListener {
            if (areAllExercisesChecked()) {
                showCongratulations()
            } else {
                showErrorDialog()
            }
        }
    }

    private fun areAllExercisesChecked(): Boolean {
        return exerciseCheckedState.values.all { it }
    }

    private fun showCongratulations() {
        val successConstraintLayout = findViewById<ConstraintLayout>(R.id.successConstraintLayout)
        val view = LayoutInflater.from(this).inflate(R.layout.success_dialog, successConstraintLayout)
        val successDone = view.findViewById<Button>(R.id.successDone)

        val builder = AlertDialog.Builder(this)
        builder.setView(view)
        val alertDialog = builder.create()

        successDone.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            alertDialog.dismiss()
        }
        alertDialog.setOnShowListener {
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(0))
        }
        alertDialog.show()
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


    private fun showErrorDialog() {
        val errorConstraintLayout = findViewById<ConstraintLayout>(R.id.errorConstraintLayout)
        val view = LayoutInflater.from(this).inflate(R.layout.error_dialog, errorConstraintLayout)
        val errorDone = view.findViewById<Button>(R.id.errorDone)

        val builder = AlertDialog.Builder(this)
        builder.setView(view)
        val alertDialog = builder.create()

        errorDone.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.setOnShowListener {
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(0))
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
                        val exerciseList = mutableListOf<Triple<String, String, String?>>()

                        for ((fieldName, fieldValue) in documentData) {
                            if (fieldName.startsWith("Exercise")) {
                                // Extract the exercise name and reps from the field value
                                val exerciseData = fieldValue.toString().split("=")
                                val exerciseName = exerciseData[0].trim().removePrefix("{")
                                val exerciseReps = if (exerciseData.size > 1) exerciseData[1].trim().removeSuffix("}") else ""
                                exerciseList.add(Triple(fieldName, exerciseName, exerciseReps))
                                exerciseCheckedState[exerciseName] = false // Initialize checked state
                            }
                        }

                        exerciseList.sortBy { it.first.substringAfter("Exercise ").toInt() }

                        for ((fieldName, exerciseName, exerciseReps) in exerciseList) {
                            val backgroundRes = if (exerciseReps != null) R.drawable.style_unchecked else R.drawable.style_checked
                            val exerciseLayout = exerciseReps?.let {
                                createExerciseLayout(exerciseName,
                                    it, backgroundRes)
                            }
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

    private fun createExerciseLayout(exerciseName: String, exerciseReps: String, backgroundRes: Int): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
            }
            background = resources.getDrawable(backgroundRes, null)
            setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())

            val exerciseTextView = TextView(this@ActiveWorkout).apply {
                text = "$exerciseName"
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                setOnClickListener {
                    if (!exerciseCheckedState[exerciseName]!!) {
                        val intent = Intent(this@ActiveWorkout, ActiveExercise::class.java)
                        intent.putExtra("EXERCISE_NAME", exerciseName)
                        intent.putExtra("EXERCISE_REPS", exerciseReps)
                        startActivityForResult(intent, REQUEST_CODE)
                    }
                }
            }

            addView(exerciseTextView)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            val exerciseName = data?.getStringExtra("EXERCISE_NAME")
            if (exerciseName != null) {
                updateExerciseBackground(exerciseName)
            }
        }
    }

    private fun updateExerciseBackground(exerciseName: String) {
        exerciseCheckedState[exerciseName] = true
        val exercisesLayout = findViewById<LinearLayout>(R.id.exercisesLayout)
        for (i in 0 until exercisesLayout.childCount) {
            val exerciseLayout = exercisesLayout.getChildAt(i) as LinearLayout
            val exerciseTextView = exerciseLayout.getChildAt(0) as TextView
            if (exerciseTextView.text.contains(exerciseName)) {
                exerciseLayout.background = resources.getDrawable(R.drawable.style_checked, null)
                exerciseLayout.setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
                exerciseTextView.setOnClickListener(null) // Disable the click listener
                break
            }
        }
    }

    fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }
}
