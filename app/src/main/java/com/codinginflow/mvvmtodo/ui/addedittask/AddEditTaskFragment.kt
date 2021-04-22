package com.codinginflow.mvvmtodo.ui.addedittask

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.databinding.FragmentAddEditTaskBinding
import com.codinginflow.mvvmtodo.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

//The basic idea we're using is that, we'll use the same fragment to add as well as edit the task. We distinguish them by, the task is sent or not from the mainFragment. If yes, then edit the task... if no then add a new task.

@AndroidEntryPoint  //As we want to inject viewModel
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task) {
    private val viewModel: AddEditTaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddEditTaskBinding.bind(view)

        binding.apply {
            editTextTaskName.setText(viewModel.taskName)  //This taskName is either the savedState or the name of the task we sent over from AddEditTaskViewModel or an empty string.
            checkBoxImportant.isChecked = viewModel.taskImportance
//            checkBoxImportant.jumpDrawablesToCurrentState() // Normally when we move to this fragment, we'll see the checkBoxState with an animation which is not appealing. Hence we avoid that by this.
            textViewDateCreated.isVisible =
                viewModel.task != null  // The date should  only be visible only if the task we sent to the AddEditTaskViewModel through the fragment argument is not null.
            textViewDateCreated.text =
                "Created : ${viewModel.task?.createdDateFormatted}"  // In case of textView, we just use .text, whereas in editText we sue setText(). viewModel.task? : We're checking if the viewModel passed us the date property

            //To update the viewModelData from the i/p in our UI.
            editTextTaskName.addTextChangedListener {
                viewModel.taskName = it.toString()
            }  //addTextChangedListener : We can do something whenever we type in the editText. viewModel will hold this i/p and save in the savedState's instance taskName.

            checkBoxImportant.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportance = isChecked
            }

            fabSaveTask.setOnClickListener {
                viewModel.onSaveClick()  //Delegate logic to the viewModel. Not that the i/p validation should be also done by the viewModel.
            }

        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditTaskEvent.collect { event ->
                when (event) {
                    is AddEditTaskViewModel.AddEditTaskEvent.NavigateBackWithResult -> {
                        binding.editTextTaskName.clearFocus()  //This will hide the keyboard when we navigate back.
                        setFragmentResult(  //API to send data between fragments, here we want to send result value to mainFragment so that it can show snackBar. Note that the receiving fragment will use setFragmentResultListener() to unwrap the data.
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result) // "to" :- map a key to it's result. "event.result" :- the flag we created.
                        )
                        findNavController().popBackStack() //To immediately remove the  fragment from the backStack and move to the previous one i.e ADD_RESULT_0K or EDIT_RESULT_OK.
                    }
                    is AddEditTaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                }.exhaustive

            }
        }

    }

}