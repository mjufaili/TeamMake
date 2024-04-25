package com.example.teammake

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class ClassCreatedConfirmation : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_created_confirmation)

        val classId = intent.extras?.getInt("class_id").toString()

        val classCodeTv: TextView = findViewById(R.id.classCode)
        // add a space ' ' character in the middle
        classCodeTv.text = classId?.replaceRange(3, 3, " ")

        // if click on view class
        val viewClassBtn: Button = findViewById(R.id.viewClassBtn)
        //if teacher click on viewClassBtn got to sceen 6.1 not her (change)
        viewClassBtn.setOnClickListener {
            val intent = Intent(this, GenerateTeams::class.java)
            intent.putExtra("class_id", classId!!)
            startActivity(intent)
        }

        // if click on returnHomeBtn
        val returnHomeBtn: Button = findViewById(R.id.returnHomeBtn)
        //if teacher click on returnHomeBtn got to new user homepage
        returnHomeBtn.setOnClickListener {
            val intent = Intent(this, Homepage::class.java)
            startActivity(intent)
        }

    }
}