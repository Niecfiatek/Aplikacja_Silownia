package com.example.testfirebase

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.widget.DatePicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.testfirebase.databinding.ActivityCalendarBinding
import com.example.testfirebase.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Calendar : AppCompatActivity() {
    private lateinit var binding: ActivityCalendarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.calendar.setOnClickListener {
            setDate()
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
}