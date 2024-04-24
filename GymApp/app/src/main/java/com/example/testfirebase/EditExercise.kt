package com.example.testfirebase

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.QuerySnapshot
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.view.View
import android.widget.AdapterView
import com.google.firebase.firestore.DocumentSnapshot

class EditExercise : AppCompatActivity() {
    private val db = Firebase.firestore
    private val exerciseCollection = db.collection("Exercise")
    private lateinit var workoutPlanInputContainer: LinearLayout
    private lateinit var backBt: Button
    private lateinit var firstSpinner: Spinner
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var exerciseNameEditText: EditText
    private lateinit var exerciseDescriptionEditText: EditText
    private lateinit var exerciseIntensityEditText: EditText
    private lateinit var exerciseDurationEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_exercise)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        workoutPlanInputContainer = findViewById(R.id.workoutPlanInputContainer)
        firstSpinner = findViewById(R.id.exerciseSpinner)
        backBt = findViewById(R.id.backButton)
        exerciseNameEditText = findViewById(R.id.exerciseNameEditText)
        exerciseDescriptionEditText = findViewById(R.id.exerciseDescriptionEditText)
        exerciseIntensityEditText = findViewById(R.id.exerciseIntensityEditText)
        exerciseDurationEditText = findViewById(R.id.exerciseDurationEditText)

        // Ustawienie słuchacza zdarzeń na spinnerze
        firstSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Pobierz nazwę wybranego ćwiczenia
                val selectedExerciseName = parent?.getItemAtPosition(position).toString()

                // Wykonaj zapytanie do bazy danych Firestore, aby pobrać dane o wybranym ćwiczeniu
                val query = exerciseCollection.whereEqualTo("Name of Exercise", selectedExerciseName)

                query.get().addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        // Pobieranie wszystkich dostępnych pól dokumentu
                        val exerciseData = document.data
                        // Wyświetlanie informacji o ćwiczeniu w odpowiednich polach do edycji
                        exerciseNameEditText.setText(exerciseData["Name of Exercise"].toString())
                        exerciseDescriptionEditText.setText(exerciseData["Description"].toString())
                        exerciseIntensityEditText.setText(exerciseData["Type of training"].toString())
                        // Uwaga: "Body part" i "Body sub-part" nie są używane w kodzie
                        // Ustawienie pola "exerciseDurationEditText" na puste, ponieważ nie ma odpowiadającego pola w bazie danych
                        exerciseDurationEditText.setText("")
                    }
                }.addOnFailureListener { exception ->
                    // Obsługa błędu
                    Toast.makeText(this@EditExercise, "Wystąpił błąd podczas pobierania danych: $exception", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Nic nie rób w przypadku, gdy nie wybrano żadnego elementu
            }
        }

        val exerciseNamesTask: Task<QuerySnapshot> = exerciseCollection.get()
        exerciseNamesTask.addOnSuccessListener { querySnapshot ->
            val exerciseNames = mutableListOf<String>()
            for (document in querySnapshot.documents) {
                val exerciseName = document.getString("Name of Exercise")
                exerciseName?.let {
                    exerciseNames.add(it)
                }
            }
            adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, exerciseNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            firstSpinner.adapter = adapter
        }

        backBt.setOnClickListener{
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }
    }
}
