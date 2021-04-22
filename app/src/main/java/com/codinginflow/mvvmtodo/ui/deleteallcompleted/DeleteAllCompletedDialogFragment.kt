package com.codinginflow.mvvmtodo.ui.deleteallcompleted

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

/*The dialog box which we want to create does'nt need layout file as we can use the default layout provided by the system. This kotlin class is enough.
We can add the dialog to the project as a completely separate destination of out navGraph. Also we'll provide a completely separate viewModel to this logic so that this dialog will be handled as a separate fragment. Cuz it'll be 100% reusable.
We can easily use this dialog in a completely different screen, just have to navigate this dialog destination.
Note that in the dialogFrag, we make this fragment global.
*/
@AndroidEntryPoint  //As we'll inject it;s separate viewModel.
class DeleteAllCompletedDialogFragment : DialogFragment() {

    private val viewModel : DeleteAllCompletedViewModel by viewModels()  //Property of the viewModel.

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())     //requireContext() : Fragment's method.
            .setTitle("Confirm deletion")
            .setMessage("Do you really want to delete all the completed tasks")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Yes") { _, _ ->   //As we're getting two parameters from this lambda and we don;t want any.
                viewModel.onConfirmClick()
            }.create()
}