package com.example.teammake

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class AnswerAdapter(private val answers: List<Answer>): RecyclerView.Adapter<AnswerAdapter.ViewHolder>() {
    // Adapter for showing answers with recycler view

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        // Get text views for answer text and score
        val answerTextView: TextView = itemView.findViewById(R.id.answer_text)
        val scoreEditText: TextView = itemView.findViewById(R.id.answer_score)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_answer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = answers[position]

        // Populate text views
        holder.answerTextView.text = item.text
        holder.scoreEditText.text = item.score.toString()

        // textWatcher for score
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    item.score = Integer.parseInt(s.toString())
                } else {
                    item.score = 0
                }
            }

            override fun afterTextChanged(s: Editable) {}
        }

        holder.scoreEditText.addTextChangedListener(textWatcher)
    }

    override fun getItemCount(): Int {
        return answers.size
    }
}