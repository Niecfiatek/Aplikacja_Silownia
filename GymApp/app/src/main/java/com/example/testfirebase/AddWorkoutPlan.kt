package com.example.testfirebase

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.text.InputType
import android.util.TypedValue
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.android.gms.tasks.Task

class AddWorkoutPlan : AppCompatActivity() {
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val exerciseCollection = firestore.collection("Exercise")
    private lateinit var firstExercise: AutoCompleteTextView
    private lateinit var workoutPlanInputContainer: LinearLayout
    private lateinit var addEx: Button
    private lateinit var addWorkPlan: Button
    private lateinit var removeEx: Button
    val exerciseNames = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_workout_plan)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        workoutPlanInputContainer = findViewById(R.id.workoutPlanInputContainer)
        addEx = findViewById(R.id.addExercise)
        addWorkPlan = findViewById(R.id.addPlan)
        removeEx = findViewById(R.id.removeExercise)
        firstExercise = findViewById(R.id.NameOfExerciseInput)
        val exerciseNamesTask: Task<QuerySnapshot> = exerciseCollection.get()
        exerciseNamesTask.addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                val exerciseName = document.getString("Name of Exercise")
                exerciseName?.let {
                    exerciseNames.add(it)
                }
            }
            val adapter = ArrayAdapter(this, R.layout.dropdown_item, exerciseNames)
            firstExercise.setAdapter(adapter)
        }

        addEx.setOnClickListener {
            addAutoCompleteTextView(exerciseNames)
        }
        removeEx.setOnClickListener{
            removeAutoCompleteTextView()
        }

    }

    private fun addAutoCompleteTextView(array: MutableList<String>) {
        val textInputLayout = TextInputLayout(this)
        val autoCompleteTextView = AutoCompleteTextView(this)
        autoCompleteTextView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        autoCompleteTextView.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        autoCompleteTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        autoCompleteTextView.setHint("Exercise Name")
        textInputLayout.addView(autoCompleteTextView)

        // Ustawienie stylu dla TextInputLayout, aby odpowiadał pierwszemu TextInputLayout
        textInputLayout.setHintTextAppearance(com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox_ExposedDropdownMenu)

        workoutPlanInputContainer.addView(textInputLayout)
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, array)
        autoCompleteTextView.setAdapter(adapter)
    }
    private fun removeAutoCompleteTextView() {
        if (workoutPlanInputContainer.childCount > 1) {
            val lastIndex = workoutPlanInputContainer.childCount - 1
            workoutPlanInputContainer.removeViewAt(lastIndex)
        }
    }
}