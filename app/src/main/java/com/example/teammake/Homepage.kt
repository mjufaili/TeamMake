
package com.example.teammake

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class Homepage : AppCompatActivity() {

    private lateinit var classListLayout: LinearLayout
    private lateinit var classes: List<Classroom?>

    data class User(val email: String, val isBelbinDone: Boolean, val classIds: List<String>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        classListLayout = findViewById(R.id.classList)

        val addCourseBtn: ConstraintLayout = findViewById(R.id.addCourse)
        val logoutTv: TextView = findViewById(R.id.logoutTv)
        val emailTv: TextView = findViewById(R.id.userEmailTv)

        val sharedPref = getSharedPreferences("TEAMMAKE", Context.MODE_PRIVATE)
        var maybeUserId = sharedPref.getString("user_id", null)
        if (maybeUserId == null) {
            val loggedInUserId = intent.extras?.getString("user_id")
            if (loggedInUserId == null) {
                Toast.makeText(
                    this,
                    "Error authenticating user",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this, Landing::class.java)
                startActivity(intent)
            }

            maybeUserId = loggedInUserId
            sharedPref.edit().putString("user_id", maybeUserId).apply()
        }
        val userId = maybeUserId!!

        CoroutineScope(Dispatchers.IO).launch {
            val user = fetchUserData(userId)

            if (user != null) {
                if (!user.isBelbinDone) {
                    val belbinFragment = BelbinAlertFragment()

                    val frTrans = supportFragmentManager.beginTransaction()
                    frTrans.replace(
                        R.id.belbinAlertTarget,
                        belbinFragment
                    ).commit()
                }

                /*
                CITATION
                Saahir learned to create several concurrent coroutines at once from this Baeldung
                tutorial @ 25-Nov-2023. URL: https://www.baeldung.com/kotlin/parallel-coroutines
                 */
                classes = coroutineScope {
                    user.classIds.map { classId ->
                        async {
                            fetchClassData(classId, userId)
                        }
                    }.awaitAll()
                }

                withContext(Dispatchers.Main) {
                    classes.forEach { classroom ->
                        if (classroom == null) return@forEach

                        val fragment = ClassFragment.newInstance(classroom)
                        val fragmentViewId = View.generateViewId()
                        val fragmentContainer = FrameLayout(this@Homepage).apply {
                            id = fragmentViewId
                        }

                        classListLayout.addView(fragmentContainer)

                        supportFragmentManager.beginTransaction()
                            .add(fragmentViewId, fragment)
                            .commit()
                    }

                    emailTv.text = user.email
                }
            }
        }

        addCourseBtn.setOnClickListener {
            //if teacher or student got to create or join class activity
            // we might have to change intent to screen 5.1
            val intent = Intent(this, CreateOrJoinClass::class.java)
            startActivity(intent)
        }

        logoutTv.setOnClickListener {
            sharedPref.edit().remove("user_id").apply()
            val intent = Intent(this, Landing::class.java)
            startActivity(intent)
        }
    }

    private suspend fun fetchUserData(userId: String): User? {
        val url = URL("https://ozcr1vquz6.execute-api.us-east-1.amazonaws.com/alpha/db/" +
                "get-user-data")
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
                        .toString()
                        .toByteArray()
                )}
                res = JSONObject(inputStream.bufferedReader().use { it.readText() })
            } catch (e: Exception) {
                Log.d("Homepage", "Couldn't connect to backend", e)
                Toast.makeText(
                    this@Homepage,
                    "Failed to connect with server",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                disconnect()
            }
        }
        if (res == null) return null

        when (res!!.getInt("status")) {
            200 -> {
                val info = JSONObject(res!!.optString("body")).getJSONObject("info")
                val email = info.getString("email")

                val isBelbinDone = try {
                    info.getJSONObject("belbin_traits")
                    true
                } catch (e: JSONException) {
                    false
                }

                val registeredClassesJSON = info.getJSONArray("registered_classes")
                val ownedClassesJSON = info.getJSONArray("owned_classes")

                val classes = ArrayList<String>()
                for (i in 0 until registeredClassesJSON.length()) {
                    classes.add(registeredClassesJSON.getString(i))
                }
                for (i in 0 until ownedClassesJSON.length()) {
                    classes.add(ownedClassesJSON.getString(i))
                }

                return User(email, isBelbinDone, classes.toList())
            }

            404 -> {
                Toast.makeText(
                    this,
                    "Error authenticating user",
                    Toast.LENGTH_SHORT
                ).show()
                return null
            }

            else -> {
                Log.d("Homepage", "Unexpected response status ${res!!.getInt("status")}")
                Toast.makeText(
                    this,
                    "Failed to fetch user. Error code ${res!!.getInt("status")}",
                    Toast.LENGTH_SHORT
                ).show()
                return null
            }
        }
    }

    private suspend fun fetchClassData(classId: String, userId: String): Classroom? {
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
                        .put("class_id", classId)
                        .toString()
                        .toByteArray()
                )}
                res = JSONObject(inputStream.bufferedReader().use { it.readText() })
            } catch (e: Exception) {
                Log.d("Homepage", "Couldn't connect to backend", e)
                Toast.makeText(
                    this@Homepage,
                    "Failed to connect with server",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                disconnect()
            }
        }
        if (res == null) return null

        when (res!!.getInt("status")) {
            200 -> {
                val info = JSONObject(res!!.optString("body")).getJSONObject("info")
                val name = info.getString("class_name")
                val isCO = info.getString("class_organizer") == userId
                val nStudents = info.getJSONArray("students").length()
                val isTeamsGenerated = try {
                    info.getJSONObject("teams")
                    true
                } catch (e: JSONException) {
                    false
                }

                return Classroom(name, classId, nStudents, isCO, isTeamsGenerated)
            }

            else -> {
                Log.d("Homepage", "Unexpected response status ${res!!.getInt("status")}")
                Toast.makeText(
                    this,
                    "Failed to fetch class with id $classId. Error code ${res!!.getInt("status")}",
                    Toast.LENGTH_SHORT
                ).show()
                return null
            }
        }
    }
}
