package com.example.teammake

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class SignUp : AppCompatActivity() {
    /**
     * Overrides the onCreate method for display
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val sharedPref = getSharedPreferences("TEAMMAKE", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("user_id", null)
        if (userId != null) {
            val intent = Intent(this, Homepage::class.java)
            startActivity(intent)
        }

        val emailET = findViewById<EditText>(R.id.Email)
        val passwET = findViewById<EditText>(R.id.Password)
        val signupBtn = findViewById<Button>(R.id.btnSignUp)

        // Button invokes the signup endpoint
        signupBtn.setOnClickListener {
            val email = emailET.text.toString().trim()
            val passw = passwET.text.toString()

            if (email.isNotBlank() && passw.isNotBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    signupUser(email, passw)
                }
            }
        }
    }

    /**
     * Creates the user given the credentials
     * @param email Email of the user
     * @param password Password of the user
     */
    private suspend fun signupUser(email: String, password: String) {
        val url = URL("https://ozcr1vquz6.execute-api.us-east-1.amazonaws.com/alpha/db/" +
            "create-user")
        var res: JSONObject? = null
        (withContext(Dispatchers.IO) {
            url.openConnection()
        } as? HttpURLConnection)?.apply {
            try {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                outputStream.use {
                    it.write(
                        JSONObject()
                            .put("email", email)
                            .put("password", password)
                            .toString()
                            .toByteArray()
                    )
                }
                res = JSONObject(inputStream.bufferedReader().use { it.readText() })
            } catch (e: Exception) {
                Log.d("Activity_2", "Couldn't connect to backend", e)
                Toast.makeText(
                    this@SignUp,
                    "Failed to connect with server",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                disconnect()
            }
        }

        // Grab the status, and if 200 OK, put the user_id in intent to Homepage
        if (res == null) return

        when (res!!.getInt("status")) {
            200 -> {
                val body = JSONObject(res!!.optString("body"))
                val userId = body.optString("id")
                val intent = Intent(this@SignUp, Homepage::class.java)
                intent.putExtra("user_id", userId)
                startActivity(intent)
                finish()
            }

            409 -> {
                Toast.makeText(
                    this@SignUp,
                    "An account with that email already exists",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {
                Log.d("Activity_2", "Unexpected response status ${res!!.getInt("status")}")
                Toast.makeText(
                    this@SignUp,
                    "Signup failed with error code ${res!!.getInt("status")}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}