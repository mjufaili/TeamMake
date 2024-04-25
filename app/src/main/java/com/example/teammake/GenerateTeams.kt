package com.example.teammake

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class GenerateTeams : AppCompatActivity() {

    var maxTeamSize: Int = 4

    lateinit var classId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_teams)

        classId = intent.extras?.getString("class_id")!!

        // if click on generateTeamsButton
        val generateTeamsButton: Button = findViewById(R.id.generateTeamsButton)
        //if teacher click on generateTeamsButton got to Screen 7.2 (List of teams) ****(change)
        generateTeamsButton.setOnClickListener {
            val maxTeamInput: TextView = findViewById(R.id.maxNo)

            if (maxTeamInput.text.isNullOrEmpty() || maxTeamInput.text.isNullOrBlank())
            {
                Toast.makeText(applicationContext, "Please provide a number.", Toast.LENGTH_LONG).show()
            } else {
                maxTeamSize = Integer.parseInt(maxTeamInput.text.toString())
                sendTeam()
            }
        }


        // if click on instructions
        /*
        val instructions: TextView = findViewById(R.id.instructions)
        //if teacher click on instructions got to ****(change)
        instructions.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        */
    }

    fun sendTeam() : Boolean {
        // create a network request to the API
        var json: String = "{\"class_id\":\"$classId\", \"max_team_size\":$maxTeamSize}"

        var requestBody: RequestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json)

        val request =
            Request.
            Builder().
            url("https://ozcr1vquz6.execute-api.us-east-1.amazonaws.com/alpha/db/start-class").
            post(requestBody).
            build()

        var client = OkHttpClient()


        // define callback methods
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // don't load anything into DB, load what DB contents exist already.
                runOnUiThread {
                    Toast.makeText(applicationContext, "UNABLE TO CONNECT TO NETWORK", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    if (response.code == 200) {
                        // we got a success; delete existing DB entries, and load new entries.
                        response.body?.let {
                            // parse json response, update recycleradapter
                            var jsonObject: JSONObject = JSONObject(it.string())
                            if (jsonObject.getInt("status") == 200) {
                                startNextActivity()
                            } else {
                                runOnUiThread {
                                    Toast.makeText(applicationContext, "UNABLE TO FETCH DETAILS", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
            }
        })

        return false
    }

    fun startNextActivity() {
        val intent = Intent(this, TeamsList::class.java)

        intent.putExtra("class_id", classId)
        startActivity(intent)
    }
}