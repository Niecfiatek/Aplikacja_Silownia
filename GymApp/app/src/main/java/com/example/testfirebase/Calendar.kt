package com.example.testfirebase

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.testfirebase.databinding.ActivityCalendarBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class Calendar : AppCompatActivity() {
    private lateinit var binding: ActivityCalendarBinding
    private lateinit var back: Button
    private lateinit var add: Button
    private lateinit var calendar: CalendarView
    private lateinit var plansTextView: TextView
    private lateinit var data: TextView
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

        data = findViewById(R.id.dataview)
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
            val selectedDateText = String.format("%02d.%02d.%d", dayOfMonth, month + 1, year)
            data.text = selectedDateText

            fetchCalendarDatesFromFirebase(year, month, dayOfMonth)
            add.setOnClickListener{
                showWorkoutPlansDialog(selectedDateText)
            }

            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            val today = Calendar.getInstance()




            plansTextView.setOnClickListener {
                if (today.get(Calendar.DAY_OF_MONTH) == dayOfMonth &&
                    today.get(Calendar.MONTH) + 1 == month + 1 &&
                    today.get(Calendar.YEAR) == year &&
                    plansTextView.text != "Brak wyników" &&
                    plansTextView.text != "") {

                    val trainingName = plansTextView.text.toString().substringAfter(": ").trim()

                    findDocument(trainingName) { documentId ->
                        if (documentId != null) {
                            MyVariables.workoutId = documentId
                            //Toast.makeText(this, MyVariables.workoutId, Toast.LENGTH_SHORT).show()
                            val intent = Intent(applicationContext, ActiveWorkout::class.java).apply {
                                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Error: Document not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    showErrorDialog()
                }
            }



        }

        setToolbar()
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
            Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
        }
        alertDialog.setOnShowListener {
            //alertDialog.window?.setBackgroundDrawable(ColorDrawable(0))
        }
        alertDialog.show()
    }

    private fun setToolbar() {
        supportActionBar?.title = "Cos"
        supportActionBar?.subtitle = "COS COS COS"
    }

    private fun fetchCalendarDatesFromFirebase(selectedYear: Int, selectedMonth: Int, selectedDay: Int) {
        val db = FirebaseFirestore.getInstance()
        db.collection("CalendarCollection")
            .get()
            .addOnSuccessListener { result ->
                plansList.clear() // Wyczyść listę przed dodaniem nowych danych
                for (document in result) {
                    val dateString = document.getString("Date")
                    dateString?.let { date ->
                        val parts = date.split(".")
                        if (parts.size == 3) {
                            val day = parts[0].toIntOrNull()
                            val month = parts[1].toIntOrNull()
                            val year = parts[2].toIntOrNull()
                            if (day != null && month != null && year != null) {
                                if (year == selectedYear && month == selectedMonth + 1 && selectedDay == day) {
                                    val planName = document.getString("Workout Plan Name")
                                    planName?.let { plan ->
                                        plansList.add("Name of training: $plan")
                                    }
                                }
                            }
                        }
                    }
                }
                if (plansList.isEmpty()) {
                    plansTextView.text = "Brak planów"
                } else {
                    displayPlans()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MyCalendarActivity", "Error fetching data: $exception")
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
                callback(null)
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