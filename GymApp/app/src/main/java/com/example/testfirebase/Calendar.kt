package com.example.testfirebase

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.MotionEvent
import android.widget.Button
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import com.example.testfirebase.databinding.ActivityCalendarBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Calendar : AppCompatActivity() {
    private lateinit var binding: ActivityCalendarBinding
    private lateinit var back: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.calendar.setOnClickListener {
            setDate()
        }

        back = findViewById(R.id.backBtn)

        back.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }

        setToolbar()
    }

    private fun setToolbar(){
        supportActionBar?.title = "Cos"
        supportActionBar?.subtitle = "COS COS COS"
    }

    private  fun setDate() {
        val datePicker = Calendar.getInstance()
        val date = DatePickerDialog.OnDateSetListener { view: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
            datePicker[Calendar.YEAR] = year
            datePicker[Calendar.MONTH] = month
            datePicker[Calendar.DAY_OF_MONTH] = dayOfMonth
            val dateFormat = "dd-MMMM-yyyy"
            val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
            val formattedDate = simpleDateFormat.format(datePicker.time)
            binding.data.text = SpannableStringBuilder.valueOf(formattedDate)
        }
        DatePickerDialog(
            this@Calendar, date,
            datePicker[Calendar.YEAR],
            datePicker[Calendar.MONTH],
            datePicker[Calendar.DAY_OF_MONTH]
        ).show()
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
                    val i = Intent(this@Calendar, Timer::class.java)
                    startActivity(i)
                } else if (x1 > x2) {
                    val i = Intent(this@Calendar, MainActivity::class.java)
                    startActivity(i)
                }
            }
        }
        return super.onTouchEvent(touchEvent)
    }
}