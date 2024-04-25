package com.example.testfirebase

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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
    private lateinit var workoutPlanInputContainer: LinearLayout
    private lateinit var save: Button
    private lateinit var backBt: Button
    private lateinit var firstSpinner: Spinner
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var bodyPartInput: AutoCompleteTextView
    private lateinit var bodySubPartInput: AutoCompleteTextView
    private lateinit var type: AutoCompleteTextView
    private lateinit var mesureInput: AutoCompleteTextView
    private lateinit var nameInput: EditText
    private lateinit var description: EditText
    private val db = Firebase.firestore
    private val exerciseCollection = db.collection("Exercise")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_exercise)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        nameInput = findViewById(R.id.nameInput)
        description = findViewById(R.id.descriptionInput)

        val bodyPartArray = resources.getStringArray(R.array.bodypart)
        val bodySubPartArray = resources.getStringArray(R.array.bodysubpart)
        val typeArray = resources.getStringArray(R.array.typ)
        val mesureArray = resources.getStringArray(R.array.mesure)

        val arrayAdapterBodyPart = ArrayAdapter(this, R.layout.dropdown_item, bodyPartArray)
        val arrayAdapterBodySubPart = ArrayAdapter(this, R.layout.dropdown_item, bodySubPartArray)
        val arrayAdapterType = ArrayAdapter(this, R.layout.dropdown_item, typeArray)
        val arrayAdapterMesure = ArrayAdapter(this, R.layout.dropdown_item, mesureArray)

        bodyPartInput = findViewById(R.id.partInput)
        bodySubPartInput = findViewById(R.id.subpartInput)
        type = findViewById(R.id.typeInput)
        mesureInput = findViewById(R.id.mesureInput)

        bodyPartInput.setAdapter(arrayAdapterBodyPart)
        bodySubPartInput.setAdapter(arrayAdapterBodySubPart)
        type.setAdapter(arrayAdapterType)
        mesureInput.setAdapter(arrayAdapterMesure)

        workoutPlanInputContainer = findViewById(R.id.workoutPlanInputContainer)
        firstSpinner = findViewById(R.id.exerciseSpinner)

        backBt = findViewById(R.id.backButton)
        save = findViewById(R.id.save)

        save.setOnClickListener {

            val n = nameInput.text.toString().trim()
            val p = bodyPartInput.text.toString().trim()
            val sp = bodySubPartInput.text.toString().trim()
            val t = type.text.toString().trim()
            val m = mesureInput.text.toString().trim()
            val d = description.text.toString().trim()

            // Pobierz nazwę wybranego ćwiczenia z pierwszej rozwijanej listy
            val selectedExerciseName = firstSpinner.selectedItem.toString()

            val exer = hashMapOf(
                "Name of Exercise" to n,
                "Body part" to p,
                "Body sub-part" to sp,
                "Type" to t,
                "Mesure" to m,
                "Description" to d
            )

            // Pobierz referencję do dokumentu ćwiczenia, które chcesz zaktualizować
            val exerciseDocRef = exerciseCollection.whereEqualTo("Name of Exercise", selectedExerciseName).limit(1)

            exerciseDocRef.get().addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    // Aktualizuj istniejący dokument z nowymi danymi
                    document.reference.set(exer)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Successfully Edited!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to Edit!", Toast.LENGTH_SHORT).show()
                        }
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to retrieve document: $exception", Toast.LENGTH_SHORT).show()
            }
        }

        firstSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedExerciseName = parent?.getItemAtPosition(position).toString()
                val query = exerciseCollection.whereEqualTo("Name of Exercise", selectedExerciseName)

                query.get().addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        val exerciseData = document.data
                        nameInput.setText(exerciseData["Name of Exercise"]?.toString() ?: "...")
                        description.setText(exerciseData["Description"]?.toString() ?: "...")
                        bodyPartInput.setText(exerciseData["Body part"]?.toString() ?: "...")
                        bodySubPartInput.setText(exerciseData["Body sub-part"]?.toString() ?: "...")
                        type.setText(exerciseData["Type"]?.toString() ?: "...")
                        mesureInput.setText(exerciseData["Mesure"]?.toString() ?: "...")
                    }

                    // Aktualizacja adapterów dla pozostałych rozwijanych list
                    val updatedBodyPartArray = resources.getStringArray(R.array.bodypart)
                    val updatedBodySubPartArray = resources.getStringArray(R.array.bodysubpart)
                    val updatedTypeArray = resources.getStringArray(R.array.typ)
                    val updatedMesureArray = resources.getStringArray(R.array.mesure)

                    val updatedArrayAdapterBodyPart = ArrayAdapter(this@EditExercise, R.layout.dropdown_item, updatedBodyPartArray)
                    val updatedArrayAdapterBodySubPart = ArrayAdapter(this@EditExercise, R.layout.dropdown_item, updatedBodySubPartArray)
                    val updatedArrayAdapterType = ArrayAdapter(this@EditExercise, R.layout.dropdown_item, updatedTypeArray)
                    val updatedArrayAdapterMesure = ArrayAdapter(this@EditExercise, R.layout.dropdown_item, updatedMesureArray)

                    bodyPartInput.setAdapter(updatedArrayAdapterBodyPart)
                    bodySubPartInput.setAdapter(updatedArrayAdapterBodySubPart)
                    type.setAdapter(updatedArrayAdapterType)
                    mesureInput.setAdapter(updatedArrayAdapterMesure)
                }.addOnFailureListener { exception ->
                    Toast.makeText(this@EditExercise, "Failed to retrieve document: $exception", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
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
