package com.example.teammake

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class JoinClass : AppCompatActivity() {

    /**
     * Overrides the onCreate method for display
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_class)

        val passET: EditText = findViewById(R.id.joinClassInputPass)
        val classNameTV: TextView = findViewById(R.id.joinClassClassName)

        val sharedPref = getSharedPreferences("TEAMMAKE", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("user_id", null)
        val classId = intent.extras?.getString("class_id")
        val className = intent.extras?.getString("class_name")

        classNameTV.text = className

        // Assigns the user to the select class
        val joinClassBtn: Button = findViewById(R.id.joinClassBtn)
        joinClassBtn.setOnClickListener {
            val password = passET.text.toString()
            if (password.isBlank()) return@setOnClickListener

            CoroutineScope(Dispatchers.IO).launch {
                joinClass(userId, classId, password)
            }
        }

    }

    /**
     * Assigns this user to the selected class
     * @param userId User's ID
     * @param classId Class' ID
     * @param classPass: Class' password
     */
    // TODO: Change API invocation to OKHTTP
    private suspend fun joinClass(userId: String?, classId: String?, classPass: String?) {
        // Check input validity
        if (userId == null || classId == null) {
            Toast.makeText(
                this,
                "Error authenticating user",
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(this, Landing::class.java)
            startActivity(intent)
            return
        }

        // Assign user to class
        val url = URL("https://ozcr1vquz6.execute-api.us-east-1.amazonaws.com/alpha/db/" +
                "assign-user-to-class")
        var res: JSONObject? = null

        (withContext(Dispatchers.IO) {
            url.openConnection()
        } as? HttpURLConnection)?.apply {
            try {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                outputStream.use { it.write(
                    JSONObject()
                        .put("user_id", userId)
                        .put("class_id", classId)
                        .put("class_password", classPass)
                        .toString()
                        .toByteArray()
                )}
                res = JSONObject(inputStream.bufferedReader().use { it.readText() })
            } catch (e: Exception) {
                Log.d("JoinClass", "Couldn't connect to backend", e)
                Toast.makeText(
                    this@JoinClass,
                    "Failed to connect with server",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                disconnect()
            }
        }

        // If 200 OK, put the class id into the intent and move to the MembersList
        if (res == null) return
        when (res!!.getInt("status")) {
            200 -> {
                val intent = Intent(this, Homepage::class.java)
                intent.putExtra("class_id", classId)
                startActivity(intent)
            }

            400 -> {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@JoinClass,
                        "You are already registered for this class",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(this@JoinClass, CreateOrJoinClass::class.java)
                    startActivity(intent)
                }
                return
            }

            401 -> {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@JoinClass,
                        "Password is incorrect",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }

            else -> {
                Log.d("JoinClass", "Unexpected response status ${res!!.getInt("status")}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@JoinClass,
                        "Failed to join class. Error code ${res!!.getInt("status")}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }
}