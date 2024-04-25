package com.example.teammake

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class TeamsList : AppCompatActivity() {
    var classList: ArrayList<Team> = arrayListOf()
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teams_list)

        recyclerView = findViewById(R.id.recyclerView)

        setAdapter()

        // create a network request to the API
        var json: String = "{\"class_id\":\"${intent.extras?.getString("class_id")}\"}"

        var requestBody: RequestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json)

        val request =
            Request.
            Builder().
            url("https://ozcr1vquz6.execute-api.us-east-1.amazonaws.com/alpha/db/get-class-data").
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
                                var bodyObject: JSONObject = jsonObject.getJSONObject("body").getJSONObject("info")

                                var teamsObject: JSONObject = bodyObject.getJSONObject("teams")

                                for (i in 0 until teamsObject.length()) {
                                    //val jsonObject: JSONObject = jsonParsed.getJSONObject(i)
                                    classList.add(Team("Team ${i + 1}", i))
                                }

                                runOnUiThread({
                                    updateClassTitle(bodyObject.getString("class_name"))
                                    setAdapter()
                                })
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
    }

    fun updateClassTitle(title: String) {
        var titleText: TextView = findViewById(R.id.classHeaderText)

        titleText.text = title
    }

    private fun setAdapter() {
        var adapter: TeamsListRecyclerAdapter = TeamsListRecyclerAdapter(classList)
        // layout manager
        var layoutMan: LayoutManager = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = layoutMan
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter
    }
}