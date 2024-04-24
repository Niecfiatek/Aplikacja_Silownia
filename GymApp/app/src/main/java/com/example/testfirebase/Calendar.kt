package com.example.testfirebase

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.MotionEvent
import android.widget.Button
import android.widget.CalendarView
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import com.example.testfirebase.databinding.ActivityCalendarBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Calendar : AppCompatActivity() {
    private lateinit var binding: ActivityCalendarBinding
    private lateinit var back: Button
    private lateinit var add: Button
    private lateinit var calendar: CalendarView
    private val markedDates = mutableListOf<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        calendar = findViewById(R.id.calendarView)

        fetchCalendarDatesFromFirebase()


        /*binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val dateFormat = "dd.MM.yyyy"
            val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
            val formattedDate = simpleDateFormat.format(calendar.time)
            binding.data.text = SpannableStringBuilder.valueOf(formattedDate)
        }*/

        back = findViewById(R.id.backBtn)
        add = findViewById(R.id.addBtn)

        back.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        add.setOnClickListener{
            val selectedDate = binding.data.text.toString()
            showWorkoutPlansDialog(selectedDate)
        }

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
                    dateString?.let {
                        try {
                            val date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(it)
                            date?.let {
                                val calendar = Calendar.getInstance()
                                calendar.time = it
                                markedDates.add(calendar.timeInMillis)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                markDatesOnCalendar()
            }
            .addOnFailureListener { exception ->
                println("Błąd podczas pobierania dat z Firebase: $exception")
            }
    }

    private fun markDatesOnCalendar() {
        calendar.setOnDateChangeListener(null)

        for (dateInMillis in markedDates) {
            //calendar.setDate(dateInMillis, true, true)
        }

        /*calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth.${month + 1}.$year"
        }*/
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