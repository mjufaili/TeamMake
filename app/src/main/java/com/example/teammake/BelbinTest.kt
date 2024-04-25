package com.example.teammake

import android.R.attr.value
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BelbinTest : AppCompatActivity() {

    lateinit var roles : Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_belbin_test)

        var sectionIndex : Int = 0

        // Get role ids from resource
        roles = applicationContext.resources.getStringArray(R.array.role_ids)

        // Get prev/next views
        val previous : Button = findViewById(R.id.previous)
        val next : Button = findViewById(R.id.next)

        // Generate quiz and get the list of sections
        val sections : ArrayList<Section> = generateQuiz()
        renderAnswers(sections, sectionIndex)

        // Previous event listener
        previous.setOnClickListener() {
            if (sectionIndex > 0) {
                // Decrement sectionIndex and render previous questions
                sectionIndex--
                renderAnswers(sections, sectionIndex)
            }
        }

        // Next event listener
        next.setOnClickListener() {
            if (sectionIndex < 6) {
                // Increment sectionIndex and render next questions
                sectionIndex++
                renderAnswers(sections, sectionIndex)
            } else {
                // If at last section, navigate to next activity
                // Get total points for each role
                val totals : MutableMap<String, Int> = calculateTotals(sections)

                // Go to quiz complete screen
                val intent = Intent(this@BelbinTest, BelbinResults::class.java).apply {
                    for (role : String in roles) {
                        putExtra(role, totals[role])
                    }
                }
                startActivity(intent)
                finish()
            }
        }
    }

    // Generate the object structure for the quiz, including all the questions and answers
    fun generateQuiz() : ArrayList<Section> {
        // Get questions and answers from resource arrays
        val questions = applicationContext.resources.getStringArray(R.array.questions)
        val answers = listOf<AnswerStrings>(
            AnswerStrings(applicationContext.resources.getStringArray(R.array.sh_answers), "shaper"),
            AnswerStrings(applicationContext.resources.getStringArray(R.array.co_answers), "coordinator"),
            AnswerStrings(applicationContext.resources.getStringArray(R.array.pl_answers), "plant"),
            AnswerStrings(applicationContext.resources.getStringArray(R.array.ri_answers), "resource_investigator"),
            AnswerStrings(applicationContext.resources.getStringArray(R.array.me_answers), "monitor_evaluator"),
            AnswerStrings(applicationContext.resources.getStringArray(R.array.imp_answers), "implementer"),
            AnswerStrings(applicationContext.resources.getStringArray(R.array.tw_answers), "teamworker"),
            AnswerStrings(applicationContext.resources.getStringArray(R.array.cf_answers), "completer_finisher")
        )

        // Init sections arraylist
        val sections = ArrayList<Section>()

        // Loop through all sections/questions
        for (i in 0..6) {
            // Shuffle answers
            val shuffledAnswers: List<AnswerStrings> = answers.shuffled()

            // Init section answers
            val answersForSection = ArrayList<Answer>()

            // Add answers to section
            for (answersArray: AnswerStrings in shuffledAnswers) {
                answersForSection.add(
                    Answer(answersArray.answerStrings[i], 0, answersArray.role)

                    // DEBUG: You can uncomment the line below to see the role printed next to the answer text
                    //Answer("${answersArray.answerStrings[i]} (${answersArray.role})", 0, answersArray.role)
                )
            }

            // Create section object and add to list
            val section = Section(questions[i], answersForSection)
            sections.add(section)
        }

        return sections
    }

    // Populate the recyclerview for the given section
    fun renderAnswers(sections: ArrayList<Section>, sectionIndex: Int) {
        // Update heading
        val heading = findViewById<TextView>(R.id.title)
        heading.text = sections[sectionIndex].question

        // Init recycler view
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = AnswerAdapter(sections[sectionIndex].answers)

        // Update progress bar
        updateProgressBar(sectionIndex)
    }

    // Update the progress bar (sectionIndex / total sections * 100)
    fun updateProgressBar(sectionIndex : Int) {
        val progressBar : ProgressBar = findViewById(R.id.progressBar)
        progressBar.progress = (((sectionIndex).toFloat() / 7.0) * 100).toInt()
    }

    // Return the totals for all roles as a map
    fun calculateTotals(sections: ArrayList<Section>) : MutableMap<String, Int> {
        val totals = mutableMapOf<String, Int>()

        // Loop through all the roles
        for (role : String in roles) {
            var total = 0

            // Find the total for this role across all sections
            for (section : Section in sections) { // loop through sections
                total += section.answers.filter{ it.role == role }[0].score // add score to total for this role
            }

            // Add total to map
            totals[role] = total
        }

        println(totals.map { "${it.key}: ${it.value}" }.joinToString(", ")) // DEBUG
        return totals
    }
}

// Small data class for associating answer string resources with their corresponding roles
data class AnswerStrings(val answerStrings : Array<String>, val role : String)