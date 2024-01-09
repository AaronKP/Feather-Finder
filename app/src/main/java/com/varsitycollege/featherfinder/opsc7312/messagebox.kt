package com.varsitycollege.featherfinder.opsc7312

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class messagebox : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val message = arguments?.getString("message")

        builder.setTitle("Information")
        builder.setMessage(message)

        // Set up the OK button without dismissing the dialog
        builder.setPositiveButton("OK", null)

        return builder.create()
    }
}


