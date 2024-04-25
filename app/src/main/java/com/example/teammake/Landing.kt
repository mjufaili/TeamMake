package com.example.teammake

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


class Landing : AppCompatActivity() {

    /**
     * Overrides the onCreate method for display
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        val sharedPref = getSharedPreferences("TEAMMAKE", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("user_id", null)
        if (userId != null) {
            val intent = Intent(this, Homepage::class.java)
            startActivity(intent)
        }

        val emailEditText = findViewById<EditText>(R.id.landing_edit_email)
        val passwordEditText = findViewById<EditText>(R.id.landing_edit_password)
        val loginButton = findViewById<Button>(R.id.login_btn)
        val signUpButton = findViewById<Button>(R.id.signup_link)

        // Moves to the homepage
        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        // Login logic
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT)
                    .show()
            } else {
                // Proceed to authenticate user
                authenticateUser(email, password)
            }
        }
    }

    /**
     * Authenticates this user
     * @param email User's email
     * @param password User's password
     */
    private fun authenticateUser(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = invokeAuthentication(email, password)
            withContext(Dispatchers.Main) {
                when (response.getInt("status")) {
                    200 -> {
                        val body = JSONObject(response.optString("body"))
                        val userId = body.optString("id")
                        // Navigate to MainActivity
                        val intent = Intent(this@Landing, Homepage::class.java).apply {
                            putExtra("user_id", userId)
                        }
                        startActivity(intent)
                        finish() // Close the current activity
                    }

                    401 -> {
                        Toast.makeText(
                            this@Landing,
                            "Wrong password. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    404 -> {
                        Toast.makeText(
                            this@Landing,
                            "User not found. Please sign up.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {
                        Toast.makeText(
                            this@Landing,
                            "Login failed with error code ${response.getInt("status")}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    }

    /**
     * Helper function for authenticateUser
     * @param email User's email
     * @param password User's password
     */
    // TODO: Change API invocation to OKHTTP
    private fun invokeAuthentication(email: String, password: String): JSONObject {
        // Invoke API for boolean
        val url = URL("https://ozcr1vquz6.execute-api.us-east-1.amazonaws.com/alpha/db/authenticate-user")
        (url.openConnection() as? HttpURLConnection)?.apply {
            try {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                outputStream.use { it.write(JSONObject().put("email", email).put("password", password).toString().toByteArray()) }
                return JSONObject(inputStream.bufferedReader().use { it.readText() })
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                disconnect()
            }
        }
        return JSONObject().put("statusCode", -1) // Error status
    }
}