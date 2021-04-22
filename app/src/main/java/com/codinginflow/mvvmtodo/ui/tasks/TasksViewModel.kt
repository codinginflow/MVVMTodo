package com.codinginflow.mvvmtodo.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.codinginflow.mvvmtodo.data.PreferencesManager
import com.codinginflow.mvvmtodo.data.SortOrder
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.ui.ADD_TASK_RESULT_OK
import com.codinginflow.mvvmtodo.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/*ViewModel :-
It helps us to do separation of concerns.
It is lifecycle aware.
Configuration changes is well handled by viewModel. It survives configuration changes.
Configuration changes happens cuz :
So if your app requires different resources files/layout based on screen size or orientation.
Using Flow below the viewModel is the common pattern, cuz it has stream of values which enables, flexible, operators to manipulate data, switch threads between flows.
But LiveData is lifeCycle aware and helps to not get crashes and memory leak. It knows how to handle situation when fragments goes in background.
So :- Use flow below the viewModel and convert it into liveData in the viewModel so that fragments can observe the liveData.
*/


class TasksViewModel @ViewModelInject constructor(  //We're injecting the dependencies mentioned in this constructor. We're using the normal @Inject cuz, viewModel needs it's own annotation. But note that @Inject and @ViewModelInject have same effect.
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state : SavedStateHandle  // We want the SavedStateHandle to apply in the searchQuery as well. Note that we can't store flow inside the SavedStateInstance, but we can store liveData and convert later into Flow or vice-versa
) : ViewModel() {

    /*How our search filter works in mutableStateFlow:-
    i)The "searchQuery" instance holds the currently active searchQuery/input in the searchItem.
    ii)We've to pass it to the getTasks() in the DAO which does required operations to filter the SQLite.
    iii)Every time i/p is made in the searchItem, a new SQLite query is run that returns a flow whenever we type anything.
    */

    /*val searchQuery = MutableStateFlow("")*/ //We're creating stateFow for the search query. MutableStateFlow can hold a single value, unlike the parent Flow.. but we can use it as a Flow in this example. We pass empty string, as we don't want to filter for anything immediately.

    val searchQuery = state.getLiveData("searchQuery", "")  //As we can't store flow inside the savedSateInstance.
    //Now note that we don't have to override setter(when we use savedState with liveData), instead whenever we make changes to the searchQuery... it'll automatically persist inside the savedState. We just have to read it.

    /*val sortOrder = MutableStateFlow(SortOrder.BY_DATE)  //We're creating stateFow for the sort query.By default the data will be sorted by date.
    val hideCompleted = MutableStateFlow(false)  //Won't hide completed task by default.*/

    val preferencesFlow = preferencesManager.preferenceFlow   //We merged our both MutableState flows as now preferenceManager is taking care to use and update them.

    private val taskEventChannel = Channel<TaskEvent>() //This channel is made private for purpose cuz we don't want to expose this channel to the fragment directly, but in the form of Flow. Cuz, when we expose this channel, fragment can put things in this channel, which we don't want to. We only want the fragment to take objects out of this channel.
    val taskEvent = taskEventChannel.receiveAsFlow()  //converts channel into flow so that we can use this in fragments to get single events out.

    /*private val taskFlow = searchQuery.flatMapLatest {  //"flatMapLatest" is the flow operator. Whenever our searchQuery(flow) changes, use the "it"(which is the current value of the flow) to run the SQLite query again which returns another flow(updated), by passing it to getTasks(), so that we can live see the changes.
        taskDao.getTasks(it)
    }.flatMapLatest {
        taskDao.getTasks(it)
    }*/

    /*private val taskFlow = combine(     // combine() is the operator which belongs to the Flow library. This way we can combine multiple flows(in our case : searchQuery, sortQuery, hideCompletedQuery) into a singleFlow. This combine() always passes us the updated value of each flows.
            searchQuery,
            sortOrder,
            hideCompleted
    ) { query, sortOrder, hideCompleted ->
        Triple(query, sortOrder, hideCompleted)  //As a function in Kotlin only allows us to return a single value, we use this wrapper which wraps three arbitrary values. Now this taskFlow variable can return three values.
    }   .flatMapLatest { (query, sortOrder, hideCompleted) ->
//        taskDao.getTasksSortedByName(query, sortOrder, hideCompleted)
        taskDao.getTasks(query, sortOrder, hideCompleted)
    }*/

    private val taskFlow = combine(
//        searchQuery, .......As searchQuery is no longer a MutableFlow
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)  //Pair takes two arguments.
    }   .flatMapLatest { (query, filterPreferences) ->
//        taskDao.getTasks(query, sortOrder, hideCompleted)
        taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }

    val tasks = taskFlow.asLiveData()   //Instead of directly calling any dao operation getTasks() which filters data and shows on the screen, create taskFlow above and pass it on here as liveData. As mentioned before, it's better practice to let the UI elements observe liveData from the viewModel.

    //Below two functions are used by the fragments to update the values in the preference manager. Remember, updateSortOrder and onHideCompletedClick are suspend functions, hence we need to launch coroutine.
    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch { //viewModelScope lives as long as the viewModel is alive
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedClick(hideCompleted : Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted((hideCompleted))
    }

//    val tasks = taskDao.getTasks().asLiveData()

    //As we're updating the items by the below functions, we launch coroutine.
    //This will be called when we click an item in our recyclerView after it's passed from the fragment.
    fun onTaskSelected(task : Task) = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToEditTaskScreen(task)) //We are passing the task which was clicked in the mainFragment.
    }

    fun onTaskCheckedChanged(task : Task, isChecked : Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = isChecked))  //Properties of the data class Task are immutable. Hence we've to create copy of the updated task. copy() is available cuz we've used data class. Notice that all the properties will be copied including the id, but only the isChecked would change.
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        //Now the tricky part is that, we want to show snackBar as soon as the item is deleted, which will be decided by this viewModel. But actions will be shown in the UI as they have required methods to show UI changes. And we can't have reference of the fragment in this viewModel, as it will lead to memory leak, we can't call those UI updating methods here.
        //Instead we need to dispatch this event to trigger the snackBar in some other way. The first idea can be to use flow or mutableStateFlows. But the problem is that when the fragment gets recreated, the liveData, etc reconnects itself to this viewModel which will trigger the events again. Hence in every configuration change, the desired events will pop up.
        //Hence we'll use channels. Channels is used to send data between coroutines. Hence, we open a channel here. Send objects into it which represents the event we want to dispatch to the fragment. And fragments on the other side can take the objects and consume the events.
        taskEventChannel.send(TaskEvent.ShowUndoDeleteTaskMessage(task))
        // taskEventChannel.send : This is how we emit events to our viewModel so that our fragments can listen to them and take appropriate actions/ This is how the viewModel tells the UI layout what it has to do. This won't take place on the main thread.
    }

    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        taskDao.insert(task)  //As the passed task is exactly the same tasks object with ids and other properties, the item will be inserted again, as if nothing happened.
    }

    fun onAddNewTaskClick() = viewModelScope.launch {  //cuz we want to send an event to our channel and we do that only in the coroutine.
        taskEventChannel.send(TaskEvent.NavigateToAddTaskScreen)
    }

    fun onAddEditResult(result : Int){
        when(result){
            ADD_TASK_RESULT_OK -> showTaskSaveConfirmationMessage("Task added")
            EDIT_TASK_RESULT_OK -> showTaskSaveConfirmationMessage("Task updated")
        } //We can't use exhaustive here cuz, these are int values and not enums or classes.
    }

    private fun showTaskSaveConfirmationMessage(text : String) = viewModelScope.launch { //Launching a coroutine cuz, we want to send event to the fragment ... as fragments can only show snackBars.
        taskEventChannel.send(TaskEvent.ShowTaskSavedConfirmationMessages(text))

    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToDeleteAllCompletedScreen)
    }

    //We're creating this seal class which represents the different kinds of events which we want to be able to send to the fragments. This way we can distinguish between them without having to create separate channels between for each of them. Task means the screen we currently have.
    sealed class TaskEvent{  //sealed class is like an enum. It can represent closed combination of different values but the difference is that these can hold data.

        object NavigateToAddTaskScreen : TaskEvent() //When we don't want to pass any data to the class as a subClass of the seal class, we can create it as an object which will make things efficient as it'll create only one instance(singleton)

        data class NavigateToEditTaskScreen(val task: Task) : TaskEvent()

        data class ShowUndoDeleteTaskMessage(val task: Task) : TaskEvent()  //Note that we did'nt name ShowUndoDeleteTaskMessage as ShowUndoDeleteTaskSnackBar as this viewModel is'nt responsible for displaying the UI. The fragment can later decide to display dialog using this event. This way we decoupled the viewModel and the taskFragment properly.

        data class ShowTaskSavedConfirmationMessages(val msg : String) : TaskEvent()

        object NavigateToDeleteAllCompletedScreen : TaskEvent()   //We're not passing any argument, cuz.. we don't need any id, etc to deleteAllCompleted as it's general and not specific.
    }

    //We're putting these data classes into seal class cuz we'll get a warning later if the "when" statement(which we'll use later) is'nt exhaustive i.e if it does'nt cover all the events mentioned above(exhaustive) ... we'll be given error.

}

/*
enum class SortOrder{  //Our sortOrder will only have the below two states and this is how we can represent two distinct states
    BY_NAME, BY_DATE
}*/
