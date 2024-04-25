package com.example.teammake

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class CreateOrJoinClass : AppCompatActivity() {
    /**
     * Overrides the onCreate method for display
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_or_join_class)

        // "Create or Join Class" button takes the user to CreateClass
        val createClassBtn: Button = findViewById(R.id.createClassBtn)
        createClassBtn.setOnClickListener {
            val intent = Intent(this, CreateClass::class.java)
            startActivity(intent)
        }

        // List of courses available to the user
        val joinET: EditText = findViewById(R.id.editTextJoinCode)
        joinET.doOnTextChanged { text, _, _, _ ->
            if (text == null) return@doOnTextChanged

            if (text.length == 6) {
                CoroutineScope(Dispatchers.IO).launch {
                    joinClass(text.toString())
                }
            }
        }
    }

    /**
     * Creates the user given the credentials
     * @param code String
     * @param password Password of the user
     */
    // TODO: Change API invocation to OKHTTP
    private suspend fun joinClass(code: String) {
        val url = URL("https://ozcr1vquz6.execute-api.us-east-1.amazonaws.com/alpha/db/" +
                "get-class-data")
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
                        .put("class_id", code)
                        .toString()
                        .toByteArray()
                )}
                res = JSONObject(inputStream.bufferedReader().use { it.readText() })
            } catch (e: Exception) {
                Log.d("CreateOrJoinClass", "Couldn't connect to backend", e)
                Toast.makeText(
                    this@CreateOrJoinClass,
                    "Failed to connect with server",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                disconnect()
            }
        }

        // Grab the status, and if 200 OK, put class data into an intent and move to JoinClass
        if (res == null) return

        when (res!!.getInt("status")) {
            200 -> {
                val info = JSONObject(res!!.optString("body")).getJSONObject("info")
                val name = info.getString("class_name")

                val intent = Intent(this@CreateOrJoinClass, JoinClass::class.java)
                intent.putExtra("class_name", name)
                intent.putExtra("class_id", code)
                startActivity(intent)
            }

            404 -> {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@CreateOrJoinClass,
                        "Class does not exist",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }

            else -> {
                Log.d("Homepage", "Unexpected response status ${res!!.getInt("status")}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@CreateOrJoinClass,
                        "Failed to join class. Error code ${res!!.getInt("status")}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }
}