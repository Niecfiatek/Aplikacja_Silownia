package com.example.testfirebase

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.QuerySnapshot
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.widget.LinearLayout
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FieldValue

class EditWorkoutPlan : AppCompatActivity() {

    private var selectedWorkoutIndex: Int = 0

    private val db = Firebase.firestore
    private val workoutCollection = db.collection("WorkoutPlans")
    private val exerciseCollection = db.collection("Exercise")
    private lateinit var listofExercise: MutableList<String>
    private lateinit var workoutSpinner: Spinner
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var buttonBack: Button
    private lateinit var buttonSave: Button
    private lateinit var buttonDelete: Button
    private lateinit var textInputLayout: TextInputLayout
    private lateinit var editText: EditText
    private lateinit var layoutForSpinners: LinearLayout
    private lateinit var addExerciseButton: Button
    private lateinit var removeSpinnerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_workout_plan)
        layoutForSpinners = findViewById(R.id.layout_for_spinners)

        listofExercise = mutableListOf()
        workoutSpinner = findViewById(R.id.workoutSpinner)
        buttonBack = findViewById(R.id.buttonBack)
        buttonSave = findViewById(R.id.buttonSave)
        buttonDelete = findViewById(R.id.buttonDelete)
        removeSpinnerButton = findViewById(R.id.removeSpinnerButton)
        textInputLayout = findViewById(R.id.textInputLayout)
        editText = findViewById(R.id.partInput)
        addExerciseButton = findViewById(R.id.addExerciseButton)

        val workoutsNamesTask: Task<QuerySnapshot> = workoutCollection.get()

        workoutsNamesTask.addOnSuccessListener { querySnapshot ->
            val workoutNames = mutableListOf<String>()
            for (document in querySnapshot.documents) {
                val workoutName = document.getString("Name of Workout Plan")
                workoutName?.let {
                    workoutNames.add(it)
                }
            }
            adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, workoutNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            workoutSpinner.adapter = adapter

            // Ustawienie wybranego elementu w spinnerze
            if (selectedWorkoutIndex != -1 && selectedWorkoutIndex < workoutNames.size) {
                workoutSpinner.setSelection(selectedWorkoutIndex)
            }
        }

        buttonBack.setOnClickListener{
            val intent = Intent(applicationContext, MenuWorkoutPlan::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }


        buttonSave.setOnClickListener {
            val workoutName = workoutSpinner.selectedItem.toString()
            val newExercises = mutableListOf<String>()

            // Pobierz ćwiczenia z każdego spinnera i dodaj do listy
            for (i in 0 until layoutForSpinners.childCount) {
                val spinner = layoutForSpinners.getChildAt(i) as? Spinner
                val selectedExercise = spinner?.selectedItem?.toString()
                selectedExercise?.let {
                    newExercises.add(it)
                }
            }

            // Zaktualizuj nazwę planu treningowego w bazie danych
            val newWorkoutName = editText.text.toString()
            updateWorkoutNameAndPlanInDatabase(workoutName, newWorkoutName, newExercises)

            recreate()
        }

        workoutSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            var previousSpinners = mutableListOf<Spinner>()

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Usuń poprzednie spinnery
                previousSpinners.forEach { spinner ->
                    layoutForSpinners.removeView(spinner)
                }
                previousSpinners.clear()

                val selectedItem = adapter.getItem(position)
                selectedItem?.let { workoutName ->
                    textInputLayout.visibility = View.VISIBLE
                    editText.setText(workoutName)

                    // Pobierz pełne dane na temat wybranego planu treningowego z bazy danych Firebase
                    val query = workoutCollection.whereEqualTo("Name of Workout Plan", workoutName)
                    query.get().addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val document = documents.documents[0]
                            val exercisesList = mutableListOf<String>()
                            for ((key, value) in document.data.orEmpty()) {
                                if (key.startsWith("Exercise")) {
                                    val exercise = value as? String
                                    exercise?.let {
                                        exercisesList.add(it)
                                    }
                                }
                            }

                            // Inicjalizacja Spinnera dla każdego ćwiczenia
                            exercisesList.forEach { exerciseName ->
                                val spinner = Spinner(this@EditWorkoutPlan)
                                val exerciseNamesTask: Task<QuerySnapshot> = exerciseCollection.get()

                                exerciseNamesTask.addOnSuccessListener { querySnapshot ->
                                    val exerciseNames = mutableListOf<String>()
                                    for (document in querySnapshot.documents) {
                                        val exercise = document.getString("Name of Exercise")
                                        exercise?.let {
                                            exerciseNames.add(it)
                                        }
                                    }
                                    val exerciseAdapter = ArrayAdapter(this@EditWorkoutPlan, android.R.layout.simple_spinner_item, exerciseNames)
                                    exerciseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    spinner.adapter = exerciseAdapter

                                    // Ustawienie domyślnego ćwiczenia z danego planu treningowego
                                    val defaultPosition = exerciseNames.indexOf(exerciseName)
                                    spinner.setSelection(defaultPosition)
                                }

                                // Dodanie Spinnera do layoutu
                                layoutForSpinners.addView(spinner)

                                // Dodaj spinner do listy poprzednich spinnerów
                                previousSpinners.add(spinner)
                            }
                        } else {
                            Toast.makeText(this@EditWorkoutPlan, "Brak danych dla wybranego planu treningowego", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { exception ->
                        Toast.makeText(this@EditWorkoutPlan, "Błąd podczas pobierania danych: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                textInputLayout.visibility = View.GONE
            }
        }

        addExerciseButton.setOnClickListener {
            addNewExerciseSpinner()
        }

        removeSpinnerButton.setOnClickListener {
            removeSelectedSpinner()
        }

        buttonDelete.setOnClickListener {
            val workoutName = workoutSpinner.selectedItem.toString()

            // Wywołaj okno dialogowe potwierdzające usunięcie planu treningowego
            showDeleteConfirmationDialog(workoutName)
        }

        if (savedInstanceState != null) {
            selectedWorkoutIndex = savedInstanceState.getInt("selectedWorkoutIndex", 0)
        }
    }

    private fun showDeleteConfirmationDialog(workoutName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Deleting Workout...")
        builder.setMessage("Do you really want to delete workout '$workoutName'?")

        builder.setPositiveButton("Yes") { dialog, which ->
            deleteWorkoutPlanFromDatabase(workoutName)

            selectedWorkoutIndex = 0
            recreate()
        }

        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }

        builder.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Zapisanie indeksu wybranego elementu w spinnerze
        outState.putInt("selectedWorkoutIndex", workoutSpinner.selectedItemPosition)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // Przywrócenie zapamiętanego indeksu wybranego elementu w spinnerze
        selectedWorkoutIndex = savedInstanceState.getInt("selectedWorkoutIndex", 0)
    }

    private fun removeSelectedSpinner() {
        val lastSpinnerIndex = layoutForSpinners.childCount - 1
        if (lastSpinnerIndex >= 0) {
            layoutForSpinners.removeViewAt(lastSpinnerIndex)
        } else {
            Toast.makeText(this, "No spinner to remove", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addNewExerciseSpinner() {
        val spinner = Spinner(this)
        val exerciseNamesTask: Task<QuerySnapshot> = exerciseCollection.get()

        exerciseNamesTask.addOnSuccessListener { querySnapshot ->
            val exerciseNames = mutableListOf<String>()
            for (document in querySnapshot.documents) {
                val exercise = document.getString("Name of Exercise")
                exercise?.let {
                    exerciseNames.add(it)
                }
            }
            val exerciseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, exerciseNames)
            exerciseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = exerciseAdapter

            // Ustawienie parametrów layoutu dla spinnera
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            // Dodanie spinnera na końcu layoutu
            layoutForSpinners.addView(spinner, layoutParams)
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error retrieving exercise data: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateWorkoutNameAndPlanInDatabase(workoutName: String, newWorkoutName: String, newExercises: List<String>) {
        val query = workoutCollection.whereEqualTo("Name of Workout Plan", workoutName)
        query.get().addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                val document = documents.documents[0]
                val workoutId = document.id

                val updatedData = mutableMapOf<String, Any>()

                // Zaktualizuj nazwę planu treningowego
                updatedData["Name of Workout Plan"] = newWorkoutName

                // Usuń wszystkie obecne ćwiczenia
                for ((key, _) in document.data.orEmpty()) {
                    if (key.startsWith("Exercise")) {
                        updatedData[key] = FieldValue.delete()
                    }
                }

                // Dodaj nowe ćwiczenia
                newExercises.forEachIndexed { index, exercise ->
                    updatedData["Exercise ${index + 1}"] = exercise
                }

                // Zaktualizuj dokument w bazie danych
                workoutCollection.document(workoutId)
                    .update(updatedData)
                    .addOnSuccessListener {
                        Toast.makeText(this@EditWorkoutPlan, "Workout plan updated successfully", Toast.LENGTH_SHORT).show()
                        workoutSpinner.setSelection(0)
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this@EditWorkoutPlan, "Error updating workout plan: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this@EditWorkoutPlan, "Workout plan not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this@EditWorkoutPlan, "Error retrieving workout plan data: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteWorkoutPlanFromDatabase(workoutName: String) {
        val query = workoutCollection.whereEqualTo("Name of Workout Plan", workoutName)
        query.get().addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                val document = documents.documents[0]
                val workoutId = document.id

                // Usuń dokument z bazy danych
                workoutCollection.document(workoutId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this@EditWorkoutPlan, "Workout plan deleted successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this@EditWorkoutPlan, "Error deleting workout plan: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this@EditWorkoutPlan, "Workout plan not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this@EditWorkoutPlan, "Error retrieving workout plan data: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }
}