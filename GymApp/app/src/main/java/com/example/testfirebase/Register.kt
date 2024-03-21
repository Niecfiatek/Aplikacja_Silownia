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
import android.content.Intent

class Register : AppCompatActivity() {

    lateinit var editTextMail:TextInputEditText
    lateinit var editTextPassword:TextInputEditText
    lateinit var buttonReg:Button
    lateinit var auth:FirebaseAuth
    lateinit var progressBar:ProgressBar
    lateinit var textView:TextView

<<<<<<< HEAD
=======
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
>>>>>>> d73aa399aa750731bfae8d90723d09ab517504a5
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
        editTextMail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        buttonReg = findViewById(R.id.btn_register)
        progressBar = findViewById(R.id.progressBar)
        textView = findViewById(R.id.loginNow)
<<<<<<< HEAD

        textView.setOnClickListener {
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()
        }

=======
        textView.setOnClickListener({
        fun onClick(view:View){
                val intent = Intent(applicationContext, Login::class.java.apply {
                    var flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                })
                startActivity(intent)
                finish()
            }
        })
>>>>>>> d73aa399aa750731bfae8d90723d09ab517504a5
        buttonReg.setOnClickListener{
            fun onClick(view:View){
                progressBar.visibility = View.VISIBLE
                var email:String
                var password:String
                email = editTextMail.getText().toString()
                password = editTextMail.getText().toString()

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show()
                }

                if(TextUtils.isEmpty(password)){
                    Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show()
                }

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener() { task ->
                        progressBar.visibility = View.GONE
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(baseContext, "Account created", Toast.LENGTH_SHORT).show()
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}