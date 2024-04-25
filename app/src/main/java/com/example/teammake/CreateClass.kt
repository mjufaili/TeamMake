package com.example.teammake

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Website.URL
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
import org.json.JSONObject.NULL
import java.net.HttpURLConnection
import java.net.URL

class CreateClass : AppCompatActivity() {


    private lateinit var editTextClassName: EditText
    private lateinit var editTextTextPassword: EditText
    private lateinit var submitNewClassBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_class)


        editTextClassName = findViewById(R.id.editTextClassName)
        submitNewClassBtn = findViewById(R.id.submitNewClassBtn)
        editTextTextPassword= findViewById(R.id.editTextTextPassword)
        // if click on finish (submit new class)
        //if teacher finish creating new course got to class created confirmation Activity
        // Finish button leads to confirmation page
        val submitNewClassBtn: Button = findViewById(R.id.submitNewClassBtn)

        submitNewClassBtn.setOnClickListener {
            val className = editTextClassName.text.toString()
            val classPass = editTextTextPassword.text.toString()

            // Ensure none of the fields are empty
            if (className.isEmpty() || classPass.isEmpty()) {
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
            } else {
                // Retrieve the class organizer's ID from SharedPreferences
                val sharedPref = getSharedPreferences("TEAMMAKE", Context.MODE_PRIVATE)
                val classOrganizer = sharedPref.getString("user_id", null)

                if (classOrganizer != null) {
                    // Launch coroutine to send data
                    CoroutineScope(Dispatchers.IO).launch {
                        val success = sendClassDataToApi(className, classPass,classOrganizer)

                        // Switch back to the main thread to update UI
                        withContext(Dispatchers.Main) {
                            if (success != NULL) {
                                val intent = Intent(this@CreateClass, ClassCreatedConfirmation::class.java)
                                intent.putExtra("class_id", success)
                                startActivity(intent)

                            } else {
                                Toast.makeText(this@CreateClass, "Failed to create class", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this@CreateClass, "Class organizer ID not found", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private suspend fun sendClassDataToApi(
        className: String,
        classPass: String,
        classOrganizer: String
    ): Int? {
        // URL and JSON keys
        val url = URL("https://ozcr1vquz6.execute-api.us-east-1.amazonaws.com/alpha/db/create-class")

        return withContext(Dispatchers.IO) {
            (url.openConnection() as? HttpURLConnection)?.run {
                try {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    val classData = JSONObject().apply {
                        put("class_name", className)
                        put("class_password", classPass)
                        put("class_organizer", classOrganizer)
                    }
                    outputStream.use { it.write(classData.toString().toByteArray()) }
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val responseBody = inputStream.bufferedReader().use { it.readText() }
                        val jsonResponse = JSONObject(responseBody)
                        jsonResponse.getJSONObject("body").getInt("id")
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    Log.e("CreateClass", "Failed to send class data", e)
                    null
                } finally {
                    disconnect()
                }
            }
        }
    }
}
