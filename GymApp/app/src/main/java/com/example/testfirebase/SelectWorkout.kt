package com.example.testfirebase

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import android.content.DialogInterface
import android.content.Intent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.QuerySnapshot
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.widget.LinearLayout
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FieldValue
import androidx.core.content.ContextCompat

object MyVariables {
    var workoutId: String? = null
}

class SelectWorkout : AppCompatActivity() {

    private val db = Firebase.firestore
    private val workoutCollection = db.collection("WorkoutPlans")
    private lateinit var header: TextView
    private lateinit var back: Button
    private lateinit var workoutsLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_select_workout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        header = findViewById(R.id.header)
        workoutsLayout = findViewById(R.id.workoutsLayout)

        // Pobierz wszystkie plany treningowe z bazy danych
        val workoutsNamesTask: Task<QuerySnapshot> = workoutCollection.get()

        workoutsNamesTask.addOnSuccessListener { querySnapshot ->
            val workoutInfos = mutableListOf<Pair<String, String>>() // Lista par (nazwa planu, id planu)
            for (document in querySnapshot.documents) {
                val workoutName = document.getString("Name of Workout Plan")
                val workoutId = document.id
                if (workoutName != null && workoutId != null) {
                    workoutInfos.add(workoutName to workoutId)
                }
            }

            addWorkoutButtons(workoutInfos)
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error retrieving workout plans: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addWorkoutButtons(workoutInfos: List<Pair<String, String>>) {
        // Ustaw stałe wartości dla szerokości i wysokości przycisków
        val buttonWidthPx = 350.dpToPx()
        val buttonHeightPx = 50.dpToPx()

        val marginBottomPx = 16.dpToPx() // Margines między przyciskami

        val buttonLayoutParams = LinearLayout.LayoutParams(
            buttonWidthPx,
            buttonHeightPx
        ).apply {
            bottomMargin = marginBottomPx
        }

        workoutInfos.forEach { (workoutName, workoutId) ->
            val button = Button(this)
            button.text = workoutName
            button.layoutParams = buttonLayoutParams
            button.setBackgroundResource(R.drawable.button_regular)
            button.setTextColor(ContextCompat.getColor(this, android.R.color.white))

            button.setOnClickListener {
                MyVariables.workoutId = workoutId
                val intent = Intent(this, ActiveWorkout::class.java)
                startActivity(intent)
            }

            workoutsLayout.addView(button)
        }

        back = findViewById(R.id.back)

        back.setOnClickListener{
            val intent = Intent(applicationContext, MenuWorkoutPlan::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }
    }

    private fun Int.dpToPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }
}
