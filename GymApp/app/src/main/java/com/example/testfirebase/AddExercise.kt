package com.example.testfirebase

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase



class AddExercise : AppCompatActivity() {
    private lateinit var add: Button
    private lateinit var back: Button
    private lateinit var nameInput: EditText
    private lateinit var bodyPartInput: AutoCompleteTextView
    private lateinit var bodySubPartInput: AutoCompleteTextView
    private lateinit var type: AutoCompleteTextView
    private lateinit var mesureInput: AutoCompleteTextView
    private lateinit var description: EditText
    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_exercise)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addEx)) { v, insets ->
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

        add = findViewById(R.id.add)
        back = findViewById(R.id.backBtn)

        add.setOnClickListener {

            val n = nameInput.text.toString().trim()
            val p = bodyPartInput.text.toString().trim()
            val sp = bodySubPartInput.text.toString().trim()
            val t = type.text.toString().trim()
            val m = mesureInput.toString().trim()
            val d = description.text.toString().trim()
            val exer = hashMapOf(
                "Name of Exercise" to n,
                "Body part" to p,
                "Body sub-part" to sp,
                "Type of training" to t,
                "Mesure type" to m,
                "Description" to d
            )

            db.collection("Exercise").document().set(exer)
                .addOnSuccessListener {
                    Toast.makeText(this, "Successfully Added!", Toast.LENGTH_SHORT).show()
                    nameInput.text.clear()
                    bodyPartInput.text.clear()
                    bodySubPartInput.text.clear()
                    type.text.clear()
                    mesureInput.text.clear()
                    description.text.clear()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
                }
        }

        back.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
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
                    val i = Intent(this@AddExercise, MainActivity::class.java)
                    startActivity(i)
                } else if (x1 > x2) {
                    val i = Intent(this@AddExercise, AddWorkoutPlan::class.java)
                    startActivity(i)
                }
            }
        }
        return super.onTouchEvent(touchEvent)
    }
}