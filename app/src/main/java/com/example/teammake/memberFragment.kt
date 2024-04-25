package com.example.teammake

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class MemberItemFragment : Fragment() {

    companion object {
        private const val ARG_MEMBER_NAME = "memberName"

        fun newInstance(memberName: String): MemberItemFragment {
            val fragment = MemberItemFragment()
            val args = Bundle()
            args.putString(ARG_MEMBER_NAME, memberName)
            fragment.arguments = args
            return fragment
        }
    }

    /**
     * Inflater for view
     * @param inflater LayoutInflater
     * @param container ViewGroup?
     * @param savedInstanceState Bundle?
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.member_fragment, container, false)
    }

    /**
     * Dynamically update the views
     * @param view View
     * @param savedInstanceState Bundle?
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the name from the arguments
        val memberName = arguments?.getString(ARG_MEMBER_NAME)

        // Find the TextView by its ID and set the member name
        val textView = view.findViewById<TextView>(R.id.memberName)
        textView.text = memberName
    }
}
