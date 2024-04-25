package com.example.teammake

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.lang.StringBuilder

// the fragment initialization parameters
private const val ARG_NAME = "class_name"
private const val ARG_ID = "class_id"
private const val ARG_STUDENTS = "class_number_of_students"
private const val ARG_IS_CO = "class_is_class_organizer"
private const val ARG_IS_TEAMS_GEN = "class_is_teams_generated"

data class Classroom(
    val name: String = "",
    val id: String = "",
    val nStudents: Int = 0,
    val isCO: Boolean = false,
    val isTeamsGenerated: Boolean = false
)

class ClassFragment : Fragment() {
    private var name: String? = ""
    private var id: String? = ""
    private var students: Int? = 0
    private var isCO: Boolean = false
    private var isTeamsGen: Boolean = false

    /**
     * Overrides the onCreate method for display
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            name = it.getString(ARG_NAME)
            id = it.getString(ARG_ID)
            students = it.getInt(ARG_STUDENTS)
            isCO = it.getBoolean(ARG_IS_CO)
            isTeamsGen = it.getBoolean(ARG_IS_TEAMS_GEN)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_class, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val classNameTv: TextView = view.findViewById(R.id.className)
        val classStatusTv: TextView = view.findViewById(R.id.classStatus)

        val status = StringBuilder()
        if (isCO) {
            status.appendLine("Class code: ${id?.padStart(6, '0')}")
        }
        if (students == 1) {
            status.appendLine("1 student currently enrolled")
        } else {
            status.appendLine("$students students currently enrolled")
        }
        if (isTeamsGen) {
            status.append("Teams generated")
        } else {
            status.append("Teams NOT generated")
        }

        classNameTv.text = name
        classStatusTv.text = status.toString()

        view.setOnClickListener {
            if (isCO) {
                if (isTeamsGen) {
                    val intent = Intent(view.context, TeamsList::class.java)
                    intent.putExtra("class_id", id)
                    startActivity(intent)
                } else {
                    val intent = Intent(view.context, GenerateTeams::class.java)
                    intent.putExtra("class_id", id)
                    startActivity(intent)
                }
            } else {
                if (isTeamsGen) {
                    val intent = Intent(view.context, MembersList::class.java)
                    intent.putExtra("class_id", id)
                    startActivity(intent)
                } else {
                    Toast.makeText(context, "Teams not generated. Come back later!",
                        Toast.LENGTH_LONG).show()
                }
            }

        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param classroom classroom with class params
         * @return A new instance of fragment ClassFragment.
         */
        @JvmStatic
        fun newInstance(classroom: Classroom) =
            ClassFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NAME, classroom.name)
                    putString(ARG_ID, classroom.id)
                    putInt(ARG_STUDENTS, classroom.nStudents)
                    putBoolean(ARG_IS_CO, classroom.isCO)
                    putBoolean(ARG_IS_TEAMS_GEN, classroom.isTeamsGenerated)
                }
            }
    }


}