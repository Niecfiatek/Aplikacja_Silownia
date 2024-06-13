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
            val newExercises = mutableListOf<Pair<String, String>>()

            // Pobierz ćwiczenia z każdego spinnera i dodaj do listy
            for (i in 0 until layoutForSpinners.childCount step 2) {
                val spinner = layoutForSpinners.getChildAt(i) as? Spinner
                val editText = layoutForSpinners.getChildAt(i + 1) as? EditText
                val selectedExercise = spinner?.selectedItem?.toString()
                val exerciseValue = editText?.text?.toString()
                if (selectedExercise != null && exerciseValue != null) {
                    newExercises.add(Pair(selectedExercise, exerciseValue))
                }
            }

            // Zaktualizuj nazwę planu treningowego w bazie danych
            val newWorkoutName = editText.text.toString()
            updateWorkoutNameAndPlanInDatabase(workoutName, newWorkoutName, newExercises)

            // Odśwież zawartość spinnera
            refreshWorkoutSpinner()
        }



        workoutSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            var previousViews = mutableListOf<View>()

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Usuń poprzednie widoki
                previousViews.forEach { view ->
                    layoutForSpinners.removeView(view)
                }
                previousViews.clear()

                val selectedItem = adapter.getItem(position)
                selectedItem?.let { workoutName ->
                    textInputLayout.visibility = View.VISIBLE
                    editText.setText(workoutName)

                    // Pobierz pełne dane na temat wybranego planu treningowego z bazy danych Firebase
                    val query = workoutCollection.whereEqualTo("Name of Workout Plan", workoutName)
                    query.get().addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val document = documents.documents[0]
                            val exercisesMap = document.get("Exercises") as? Map<String, String>
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
                            exercisesMap?.forEach { (exerciseName, exerciseValue) ->
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

                                val editText = EditText(this@EditWorkoutPlan)
                                editText.setText(exerciseValue)

                                // Dodanie Spinnera i EditText do layoutu
                                layoutForSpinners.addView(spinner)
                                layoutForSpinners.addView(editText)

                                // Dodaj spinner do listy poprzednich widoków
                                previousViews.add(spinner)
                                previousViews.add(editText)
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

    private fun refreshWorkoutSpinner() {
        val workoutsNamesTask: Task<QuerySnapshot> = workoutCollection.get()

        workoutsNamesTask.addOnSuccessListener { querySnapshot ->
            val workoutNames = mutableListOf<String>()
            for (document in querySnapshot.documents) {
                val workoutName = document.getString("Name of Workout Plan")
                workoutName?.let {
                    workoutNames.add(it)
                }
            }
            adapter.clear()
            adapter.addAll(workoutNames)
            adapter.notifyDataSetChanged()
        }
    }
    private fun scrollToBottom(scrollView: ScrollView) {
        scrollView.post {
            scrollView.fullScroll(View.FOCUS_DOWN)
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
        // Zapisanie indeksu wybranego elementu spinnera do savedInstanceState
        outState.putInt("selectedWorkoutIndex", workoutSpinner.selectedItemPosition)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        selectedWorkoutIndex = savedInstanceState.getInt("selectedWorkoutIndex", 0)
        workoutSpinner.setSelection(selectedWorkoutIndex)
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
        }

        val editText = EditText(this)

        layoutForSpinners.addView(spinner)
        layoutForSpinners.addView(editText)

        scrollToBottom(findViewById(R.id.scrollView))
    }


    private fun removeSelectedSpinner() {
        val childCount = layoutForSpinners.childCount
        if (childCount > 1) {
            layoutForSpinners.removeViewAt(childCount - 1)
            layoutForSpinners.removeViewAt(childCount - 2)
        }

        scrollToBottom(findViewById(R.id.scrollView))
    }


    private fun updateWorkoutNameAndPlanInDatabase(
        oldWorkoutName: String,
        newWorkoutName: String,
        exercises: List<Pair<String, String>>
    ) {
        val query = workoutCollection.whereEqualTo("Name of Workout Plan", oldWorkoutName)
        query.get().addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                val document = documents.documents[0]
                workoutCollection.document(document.id).delete()
                    .addOnSuccessListener {
                        // Utwórz nowy dokument z nową nazwą i ćwiczeniami
                        val newWorkoutPlan = hashMapOf(
                            "Name of Workout Plan" to newWorkoutName,
                            "Exercises" to exercises.toMap()
                        )
                        workoutCollection.add(newWorkoutPlan)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Workout plan updated successfully", Toast.LENGTH_SHORT).show()
                                refreshWorkoutSpinner()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Error adding new workout plan: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Error deleting old workout plan: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Dokument nie istnieje, więc nie ma potrzeby go usuwać
                // Utwórz nowy dokument z nową nazwą i ćwiczeniami
                val newWorkoutPlan = hashMapOf(
                    "Name of Workout Plan" to newWorkoutName,
                    "Exercises" to exercises.toMap()
                )
                workoutCollection.add(newWorkoutPlan)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Workout plan created successfully", Toast.LENGTH_SHORT).show()
                        refreshWorkoutSpinner()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Error adding new workout plan: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error fetching old workout plan: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun deleteWorkoutPlanFromDatabase(workoutName: String) {
        val query = workoutCollection.whereEqualTo("Name of Workout Plan", workoutName)
        query.get().addOnSuccessListener { documents ->
            for (document in documents) {
                workoutCollection.document(document.id).delete()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Błąd podczas usuwania planu treningowego: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
