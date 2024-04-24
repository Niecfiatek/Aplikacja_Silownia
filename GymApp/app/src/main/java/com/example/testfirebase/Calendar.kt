package com.example.testfirebase

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.testfirebase.databinding.ActivityCalendarBinding
import com.google.firebase.firestore.FirebaseFirestore

class Calendar : AppCompatActivity() {
    private lateinit var binding: ActivityCalendarBinding
    private lateinit var back: Button
    private lateinit var add: Button
    private lateinit var calendar: CalendarView
    private lateinit var plansTextView: TextView
    private val plansList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calendar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        plansTextView = findViewById(R.id.plansTextView)
        calendar = findViewById(R.id.calendarView)

        //fetchCalendarDatesFromFirebase()

        back = findViewById(R.id.backBtn)
        add = findViewById(R.id.addBtn)

        back.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        calendar.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val selectedDate = String.format("%02d.%02d.%d", dayOfMonth, month + 1, year)
            add.setOnClickListener{
                showWorkoutPlansDialog(selectedDate)
            }
        }

        fetchCalendarDatesFromFirebase()

        setToolbar()
    }

    private fun setToolbar() {
        supportActionBar?.title = "Cos"
        supportActionBar?.subtitle = "COS COS COS"
    }

    private fun fetchCalendarDatesFromFirebase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("CalendarCollection")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val dateString = document.getString("Date")
                    val planName = document.getString("Workout Plan Name")
                    dateString?.let { date ->
                        planName?.let { plan ->
                            plansList.add("Name: $plan ----> Date: $date")
                        }
                    }
                }
                // Po pobraniu danych z Firebase wyświetlamy je pod kalendarzem
                displayPlans()
            }
            .addOnFailureListener { exception ->
                Log.e("CalendarActivity", "Error fetching data: $exception")
            }
    }
    private fun displayPlans() {
        val plansText = plansList.joinToString("\n")
        plansTextView.text = plansText
    }

    private fun showWorkoutPlansDialog(selectedDate: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("WorkoutPlans")
            .get()
            .addOnSuccessListener { result ->
                val workoutPlans = mutableListOf<String>()
                for (document in result) {
                    val planName = document.getString("Name of Workout Plan")
                    planName?.let {
                        workoutPlans.add(it)
                    }
                }
                showWorkoutPlansAlertDialog(workoutPlans.toTypedArray(), selectedDate)
            }
            .addOnFailureListener { exception ->
                println("Error: $exception")
            }
    }

    private fun showWorkoutPlansAlertDialog(workoutPlans: Array<String>, selectedDate: String) {
        val builder = AlertDialog.Builder(this@Calendar)
        builder.setTitle("Choose your workout plan")

        builder.setItems(workoutPlans) { dialog, which ->
            addWorkoutPlanToCalendar(workoutPlans[which], selectedDate)
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        val dialog = builder.create()
        dialog.show()
    }

    private fun addWorkoutPlanToCalendar(workoutPlanName: String, selectedDate: String) {
        val db = FirebaseFirestore.getInstance()
        val calendarData = hashMapOf(
            "Workout Plan Name" to workoutPlanName,
            "Date" to selectedDate
        )

        db.collection("CalendarCollection")
            .add(calendarData)
            .addOnSuccessListener { documentReference ->
                println("Successfully added workout plan with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                println("Error: $e")
            }
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
                    val i = Intent(this@Calendar, Timer::class.java)
                    startActivity(i)
                } else if (x1 > x2) {
                    val i = Intent(this@Calendar, MainActivity::class.java)
                    startActivity(i)
                }
            }
        }
        return super.onTouchEvent(touchEvent)
    }
}