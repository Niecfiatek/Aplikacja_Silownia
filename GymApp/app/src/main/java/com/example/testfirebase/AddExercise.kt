package com.example.testfirebase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

//import com.nk.myapplication.databinding.ActivityFirestoreBinding

class AddExercise : AppCompatActivity() {

    private lateinit var add: Button
    private lateinit var textView: TextView
    private lateinit var nameInput: EditText
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
        add = findViewById(R.id.add)
        textView = findViewById(R.id.name)
        nameInput = findViewById(R.id.nameInput)



        add.setOnClickListener {

            val n = nameInput.text.toString().trim()
            val exer = hashMapOf("Name of Exercise" to n)

            db.collection("Exercise").document().set(exer)
                .addOnSuccessListener {
                    Toast.makeText(this, "Successfully Added!", Toast.LENGTH_SHORT).show()
                    nameInput.text.clear()
                    //addExerciseToFirestore(name)
                    //val intent = Intent(applicationContext, AddExercise::class.java).apply {
                    //    flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                //  }
                //startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
                    nameInput.text.clear()
                }
        }
    }

    //val intent = Intent(applicationContext, AnotherActivity::class.java).apply {
    //    flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    //}

}