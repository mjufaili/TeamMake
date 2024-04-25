package com.example.teammake

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class BelbinResults : AppCompatActivity() {

    /**
     * Overrides the onCreate method for display
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_belbin_results)

        // Init sharedprefs
        val sharedPref = getSharedPreferences("TEAMMAKE", Context.MODE_PRIVATE)

        // Get role id strings from resource
        val roles = applicationContext.resources.getStringArray(R.array.role_ids)

        // Find which role has highest score and put all role and their totals in a hashmap
        var highScore = 0
        var topRole = ""
        val totals = mutableMapOf<Any?, Any?>()
        totals["specialist"] = 0
        for (role : String in roles) {
            val roleTotal : Int = intent.getIntExtra(role, 0)
            totals[role] = roleTotal
            if (roleTotal >= highScore) {
                highScore = roleTotal
                topRole = role
            }
        }

        // Show description on screen
        var description : String = ""
        if (topRole == "shaper") description = getString(R.string.sh_description)
        if (topRole == "coordinator") description = getString(R.string.co_description)
        if (topRole == "plant") description = getString(R.string.pl_description)
        if (topRole == "resource_investigator") description = getString(R.string.ri_description)
        if (topRole == "monitor_evaluator") description = getString(R.string.me_description)
        if (topRole == "implementer") description = getString(R.string.imp_description)
        if (topRole == "teamworker") description = getString(R.string.tw_description)
        if (topRole == "completer_finisher") description = getString(R.string.cf_description)
        findViewById<TextView>(R.id.descriptionText).text = description

        // Finish button event
        findViewById<Button>(R.id.finish).setOnClickListener() {
            val intent = Intent(this@BelbinResults, Homepage::class.java)
            startActivity(intent)
        }

        // Post results to database

        // Prepare request
        var mapJson: String = JSONObject(totals).toString()
        var json: String = "{\"user_id\":\"${sharedPref.getString("user_id", "")}\",\"belbin_traits\":$mapJson}"
        var requestBody: RequestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json)
        val request =
            Request.
            Builder().
            url("https://ozcr1vquz6.execute-api.us-east-1.amazonaws.com/alpha/db/update-belbin-traits").
            post(requestBody).
            build()

        // Init client
        var client = OkHttpClient()

        // Create a new threat an make request
        val thread = Thread {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Error uploading results.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.code == 200) {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Your results have been saved.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
        thread.start()
    }
}