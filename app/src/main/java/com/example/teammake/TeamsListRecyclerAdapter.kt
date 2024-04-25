package com.example.teammake

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class TeamsListRecyclerAdapter(var classList: ArrayList<Team>) :
    RecyclerView.Adapter<TeamsListRecyclerAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val constraintLayout: ConstraintLayout

        init {
            // Define click listener for the ViewHolder's View
            textView = view.findViewById(R.id.className)
            constraintLayout = view.findViewById(R.id.constraintLayout)
        }
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.class_list_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        var currentClassText: String = classList.get(position).teamName

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = currentClassText

        viewHolder.constraintLayout.setOnClickListener({
            println("Could go to members list from here")
        })
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = classList.size
}