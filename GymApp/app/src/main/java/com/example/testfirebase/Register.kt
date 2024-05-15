package com.example.testfirebase

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class Register : AppCompatActivity() {

    lateinit var editTextEmail:TextInputEditText
    lateinit var editTextPassword:TextInputEditText
    lateinit var editTextConfirmPassword:TextInputEditText
    lateinit var buttonReg:Button
    lateinit var auth:FirebaseAuth
    lateinit var progressBar:ProgressBar
    lateinit var textView:TextView


    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()
        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        editTextConfirmPassword = findViewById(R.id.repeatpassword)
        buttonReg = findViewById(R.id.btn_register)
        progressBar = findViewById(R.id.progressBar)
        textView = findViewById(R.id.loginNow)


        textView.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
        }




        buttonReg.setOnClickListener{

                progressBar.visibility = View.VISIBLE
                var email:String
                var password:String
                var confirmpassword:String
                email = editTextEmail.getText().toString()
                password = editTextPassword.getText().toString()
                confirmpassword = editTextConfirmPassword.getText().toString()

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if(TextUtils.isEmpty(password)){
                    Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener() { task ->
                        //progressBar.visibility = View.GONE
                        if (task.user != null && confirmpassword==password) {
                            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                            startActivity(intent)
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(baseContext, "Account created", Toast.LENGTH_SHORT).show()
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(baseContext, "Different password.", Toast.LENGTH_SHORT).show()
                        }
                    }

        }
    }
}