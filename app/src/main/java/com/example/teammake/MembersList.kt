package com.example.teammake

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MembersList : AppCompatActivity() {

    private lateinit var linearLayout: LinearLayout
    private lateinit var teamTitleTextView: TextView

    /**
     * Overrides the onCreate method for display
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_members_list)
        linearLayout = findViewById(R.id.fragmentPlaceholder)
        teamTitleTextView = findViewById(R.id.title)

        val sharedPref = getSharedPreferences("TEAMMAKE", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("user_id", null)
        val classId = intent.extras?.getString("class_id")

        // TODO: Refactor this into a private function
        // Get the class data to find all the group members within the team as the user
        CoroutineScope(Dispatchers.IO).launch {
            val url = URL("https://ozcr1vquz6.execute-api.us-east-1.amazonaws.com/alpha/db/get-class-data")
            (url.openConnection() as? HttpURLConnection)?.run {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")

                val jsonBody = JSONObject()
                jsonBody.put("class_id", classId)

                outputStream.use { os ->
                    os.write(jsonBody.toString().toByteArray())
                }

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    inputStream.bufferedReader().use {
                        val response = it.readText()
                        val jsonResponse = JSONObject(response)
                        val body = jsonResponse.getJSONObject("body")
                        val info = body.getJSONObject("info")
                        val className = info.getString("class_name") // Extract class name
                        val teams = info.getJSONObject("teams")

                        withContext(Dispatchers.Main) {
                            // Set the class name as the title
                            teamTitleTextView.text = className

                            // Find the team of the user and create fragments for the team members
                            teams.keys().forEach { teamKey ->
                                val team = teams.getJSONArray(teamKey)
                                if (team.toString().contains("\"$userId\"")) {
                                    // Convert team number from API to human-readable form (Team 1, Team 2, etc.)
                                    val teamNumber = teamKey.toInt() + 1
                                    teamTitleTextView.text = "$className - Team $teamNumber"


                                    // Get emails for all team members
                                    for (j in 0 until team.length()) {
                                        val memberID = team.getString(j)
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val email = getUserEmail(memberID)
                                            email?.let {
                                                withContext(Dispatchers.Main) {
                                                    // Create and add the MemberItemFragment for each team member
                                                    val memberFragment = MemberItemFragment.newInstance(email)
                                                    supportFragmentManager.beginTransaction()
                                                        .add(linearLayout.id, memberFragment)
                                                        .commit()
                                                }
                                            }
                                        }
                                    }
                                    return@forEach
                                }
                            }
                        }
                    }
                } else {
                    // Handle error
                    Log.e("MembersList", "HTTP error code: $responseCode")
                }
            }
        }
    }

    /**
     * Gets the user's email from the ID
     * @param userId ID of the user
     */
    private fun getUserEmail(userId: String): String? {
        val url = URL("https://ozcr1vquz6.execute-api.us-east-1.amazonaws.com/alpha/db/get-user-data")
        var userEmail: String? = null
        (url.openConnection() as? HttpURLConnection)?.run {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")

            val jsonBody = JSONObject()
            jsonBody.put("user_id", userId)

            outputStream.use { os ->
                os.write(jsonBody.toString().toByteArray())
            }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream.bufferedReader().use {
                    val response = it.readText()
                    val jsonResponse = JSONObject(response)
                    val body = jsonResponse.getJSONObject("body")
                    val info = body.getJSONObject("info")
                    userEmail = info.getString("email") // Extracting the email from the correct path
                }
            } else {
                Log.e("MembersList", "HTTP error code: $responseCode")
            }
        }
        return userEmail
    }

}