package com.example.teammake

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.teammake.R

class BelbinTutorial : AppCompatActivity() {

    /**
     * Overrides the onCreate method for display
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_belbin_tutorial)

        // Add click event for begin button
        findViewById<Button>(R.id.begin).setOnClickListener() {
            val intent = Intent(this@BelbinTutorial, BelbinTest::class.java)
            startActivity(intent)
        }
    }
}