package com.munifahsan.biosheapp.ui.notif

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.munifahsan.biosheapp.databinding.ActivityNotifBinding

class NotifActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotifBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotifBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.backIcon.setOnClickListener {
            finish()
        }
    }
}