package com.codinginflow.mvvmtodo.ui.addedittask

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.ui.ADD_TASK_RESULT_OK
import com.codinginflow.mvvmtodo.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddEditTaskViewModel @ViewModelInject constructor(  //Dagger injection cuz, we want dagger to inject savedStateHandle to viewModel.
    private val taskDao: TaskDao,
    @Assisted private val state: SavedStateHandle  //@Assisted : dagger annotation. Note that this SavedStateHandle also contains the navigation argument.Cuz, we'll inject this viewModel to assEditTaskFragment... the savedState will automatically contain the navigation argument. This is convenience setup pre-prepared for us.
) : ViewModel() {
    val task =
        state.get<Task>("task")  //The argument string we're passing in here should be exact same as that of the navGraph navigation argument.

    //We want to split up the above task object. We want to be able to change taskName and taskImportance in the addEditScreen, but as we made the task variable immutable, we can't change them directly. Hence we create separate variables for the taskName and the taskImportance. We'll store them in the savedInstanceState to survive process death.

    var taskName = state.get<String>("taskName") ?: task?.name
    ?: "" //By get() : we're retrieving the value/property from the savedInstanceState. If we don't have a savedInstance state, the value/name will be null. Hence use elvis operator and get the value from the task we sent over. And if we don't send a task over here, then null string.
        set(value) {  //We override the setter method cuz, as soon as the i/p is inserted in the field we want to save it in the savedState.
            field = value
            state.set("taskName", value)
        }

    var taskImportance = state.get<Boolean>("taskImportance") ?: task?.important ?: false
        set(value) {
            field = value
            state.set("taskImportance", value)
        }

    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()


    fun onSaveClick() {
        if (taskName.isBlank()) {
            // show invalid input message
            showInvalidInputMessage("Name can't be empty")
            return  //IMP.
        }

        if (task != null) {  //If task is'nt null, that means we have to edit the incoming task, else create newTask and it to the database.
            //As task is immutable we've to create and send a completely new object to our database.
            val updatedTask = task.copy(name = taskName, important = taskImportance)
            updateTask(updatedTask)
        } else {
            val newTask = Task(name = taskName, important = taskImportance)
            createTask(newTask)
        }

    }

    private fun createTask(task: Task) =
        viewModelScope.launch { //As we want to make database operations.
            taskDao.insert(task)
            addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))  //Note that we can't show the snackBar os updateDialog in this assEditFrag but the mainFragment is the right screen. Hence we want to navigate back to the mainFragment as soon as updating the received task or adding the newTask to the database. We're sending it as an event cuz, only the fragment can execute the navigation event and show the snackBar.
        }

    private fun updateTask(task: Task) = viewModelScope.launch {
        taskDao.update(task)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))  //As we want to navigate back to the mainFragment as soon as updating the sent task or adding the newTask to thr database.
    }

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(text))
    }

    sealed class AddEditTaskEvent {
        data class ShowInvalidInputMessage(val msg: String) : AddEditTaskEvent()  //ShowInvalidInputMessage : Again passive naming as we want the fragment to decide how to show the message of invalid i/p via the "msg".
        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()  //"result" is just a flag to identify the success.
    }

}