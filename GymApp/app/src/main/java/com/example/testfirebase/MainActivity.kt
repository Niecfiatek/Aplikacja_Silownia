package com.example.testfirebase

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var b: Button
    private lateinit var addEx: Button
    private lateinit var editEx: Button
    private lateinit var calendar: Button
    private lateinit var timer: Button
    private lateinit var addPlan: Button
    private lateinit var textView:TextView
    private val user = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        b = findViewById(R.id.logout)
        //editEx = findViewById(R.id.editExercise)
        textView = findViewById(R.id.user_details)
        if(user == null){
            val intent = Intent(applicationContext, Login::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }
        else{
            textView.text = user.email
        }
        b.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(applicationContext, Login::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }

        /// AddExercise Button
        addEx = findViewById(R.id.addExercise)
        textView = findViewById(R.id.user_details)

        addEx.setOnClickListener {
            val intent = Intent(applicationContext, AddExercise::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }

        /// Calendar Button
        calendar = findViewById(R.id.calendar)
        textView = findViewById(R.id.user_details)

        calendar.setOnClickListener {
            val intent = Intent(applicationContext, Calendar::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }

        /// Timer Button
        timer = findViewById(R.id.timer)
        textView = findViewById(R.id.user_details)

        timer.setOnClickListener {
            val intent = Intent(applicationContext, Timer::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }

        /// Workout Plan Button
        addPlan = findViewById(R.id.addWorkout)
        textView = findViewById(R.id.user_details)

        addPlan.setOnClickListener {
            val intent = Intent(applicationContext, AddWorkoutPlan::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }

        /// Workout Plan Button
        addPlan = findViewById(R.id.addWorkout)
        textView = findViewById(R.id.user_details)

        addPlan.setOnClickListener {
            val intent = Intent(applicationContext, AddWorkoutPlan::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }

        /// Edit Exercise
        editEx = findViewById(R.id.editExercise)
        textView = findViewById(R.id.user_details)

        editEx.setOnClickListener {
            val intent = Intent(applicationContext, EditExercise::class.java).apply {
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
                    val i = Intent(this@MainActivity, Calendar::class.java)
                    startActivity(i)
                } else if (x1 > x2) {
                    val i = Intent(this@MainActivity, AddExercise::class.java)
                    startActivity(i)
                }
            }
        }
        return super.onTouchEvent(touchEvent)
    }


}