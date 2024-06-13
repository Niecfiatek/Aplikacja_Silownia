package com.example.testfirebase

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.content.Intent
import android.view.View
import android.widget.AdapterView
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import org.w3c.dom.Text
import java.util.Calendar

class Stats : AppCompatActivity() {

    private lateinit var back: Button
    private lateinit var showButton: Button
    private lateinit var yearSpinner: Spinner
    private lateinit var monthSpinner: Spinner
    private lateinit var exerciseTable: TableLayout
    private lateinit var choosenExerciseTable: TableLayout
    private lateinit var exerciseTitleTable: TextView
    private lateinit var exerciseSpinner: Spinner
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var exerciseNames: MutableList<String>
    private val dbExercise = FirebaseFirestore.getInstance()
    private val exerciseCollection = dbExercise.collection("Exercise")

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stats)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        back = findViewById(R.id.backBt)
        showButton = findViewById(R.id.button)
        yearSpinner = findViewById(R.id.yearSpinner)
        monthSpinner = findViewById(R.id.monthSpinner)
        exerciseTable = findViewById(R.id.exerciseTable)
        choosenExerciseTable = findViewById(R.id.newExerciseTable)
        exerciseTitleTable = findViewById(R.id.tittleOfExercise)
        exerciseSpinner = findViewById(R.id.exerciseSpinner)
        exerciseNames = mutableListOf()


        back = findViewById(R.id.backBt)
        back.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        val years = (2024..2124).toList()
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpinner.adapter = yearAdapter

        val months = listOf(
            "01", "02", "03", "04", "05", "06",
            "07", "08", "09", "10", "11", "12"
        )
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthSpinner.adapter = monthAdapter

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        yearSpinner.setSelection(yearAdapter.getPosition(currentYear))
        monthSpinner.setSelection(monthAdapter.getPosition(currentMonth))

        showButton.setOnClickListener {
            val selectedYear = yearSpinner.selectedItem.toString()
            val selectedMonth = monthSpinner.selectedItem.toString()
            val previousMonth = getPreviousMonth(selectedMonth)

            fetchData(selectedYear, selectedMonth, previousMonth)
        }

        loadExerciseNames()
    }

    private fun loadExerciseNames() {
        val exerciseNamesTask: Task<QuerySnapshot> = exerciseCollection.get()

        exerciseNamesTask.addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                val exerciseName = document.getString("Name of Exercise")
                exerciseName?.let {
                    exerciseNames.add(it)
                }
            }
            setupSpinner()
        }
    }

    private fun setupSpinner() {
        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, exerciseNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        exerciseSpinner.adapter = adapter
        exerciseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedExercise = exerciseNames[position]
                exerciseTitleTable.text = selectedExercise
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Nie używane w tym przypadku
            }
        }


    }

    private fun getPreviousMonth(currentMonth: String): String {
        val month = currentMonth.toInt()
        val previousMonth = if (month == 1) 12 else month - 1
        return previousMonth.toString().padStart(2, '0')
    }

    private fun fetchData(year: String, currentMonth: String, previousMonth: String) {
        val currentDatePattern = ".*\\.$currentMonth\\.$year"
        val previousDatePattern = if (currentMonth == "01") {
            ".*\\.12\\.${year.toInt() - 1}"
        } else {
            ".*\\.$previousMonth\\.$year"
        }

        db.collection("CalendarCollection")
            .get()
            .addOnSuccessListener { snapshot ->
                val currentMonthSnapshot = snapshot.documents.filter { it.getString("Date")?.matches(Regex(currentDatePattern)) == true }
                val previousMonthSnapshot = snapshot.documents.filter { it.getString("Date")?.matches(Regex(previousDatePattern)) == true }
                updateTable(currentMonthSnapshot, previousMonthSnapshot)
            }
    }

    private fun getInfoExercise(nameOfExercise: String, year: String, currentMonth: String, previousMonth: String){
        choosenExerciseTable.removeAllViews()
        val currentDatePattern = ".*\\.$currentMonth\\.$year"
        val previousDatePattern = if (currentMonth == "01") {
            ".*\\.12\\.${year.toInt() - 1}"
        } else {
            ".*\\.$previousMonth\\.$year"
        }
        db.collection("CalendarCollection")
            .whereEqualTo("Workout Plan Name", nameOfExercise)
            .get()
            .addOnSuccessListener { snapshot ->
                val currentMonthSnapshot = snapshot.documents.filter { it.getString("Date")?.matches(Regex(currentDatePattern)) == true }
                val previousMonthSnapshot = snapshot.documents.filter { it.getString("Date")?.matches(Regex(previousDatePattern)) == true }
            }
            .addOnFailureListener { exception ->
                // Handle failure
                exception.printStackTrace()
            }
    }

    private fun updateTable(currentMonthSnapshot: List<DocumentSnapshot>, previousMonthSnapshot: List<DocumentSnapshot>) {
        exerciseTable.removeViews(1, exerciseTable.childCount - 1)

        val currentMonthCounts = getCountsByWorkoutName(currentMonthSnapshot)
        val previousMonthCounts = getCountsByWorkoutName(previousMonthSnapshot)

        val allWorkoutNames = currentMonthCounts.keys + previousMonthCounts.keys

        allWorkoutNames.forEach { workoutName ->
            val row = TableRow(this)

            val workoutNameView = TextView(this).apply {
                text = workoutName
                setPadding(8, 8, 8, 8)
                setBackgroundResource(R.drawable.cell_border)
                gravity = android.view.Gravity.CENTER
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f)
            }
            val previousMonthView = TextView(this).apply {
                text = previousMonthCounts[workoutName]?.toString() ?: "0"
                setPadding(8, 8, 8, 8)
                setBackgroundResource(R.drawable.cell_border)
                gravity = android.view.Gravity.CENTER
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            }
            val currentMonthView = TextView(this).apply {
                text = currentMonthCounts[workoutName]?.toString() ?: "0"
                setPadding(8, 8, 8, 8)
                setBackgroundResource(R.drawable.cell_border)
                gravity = android.view.Gravity.CENTER
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            }

            row.addView(workoutNameView)
            row.addView(previousMonthView)
            row.addView(currentMonthView)

            exerciseTable.addView(row)
        }
    }

    private fun getCountsByWorkoutName(documents: List<DocumentSnapshot>): Map<String, Int> {
        val counts = mutableMapOf<String, Int>()

        documents.forEach { document ->
            val workoutName = document.getString("Workout Plan Name") ?: return@forEach
            counts[workoutName] = counts.getOrDefault(workoutName, 0) + 1
        }

        return counts
    }
}
