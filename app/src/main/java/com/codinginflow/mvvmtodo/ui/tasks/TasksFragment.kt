package com.codinginflow.mvvmtodo.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.data.SortOrder
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.databinding.FragmentTasksBinding
import com.codinginflow.mvvmtodo.util.exhaustive
import com.codinginflow.mvvmtodo.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tasks.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint  //As we want the viewModel to get injected into this fragment, as we want to call methods of viewModel. @AndroidEntryPoint cuz fragments and activities are special cases and can't be constructor injected.
//class TasksFragment : Fragment(R.layout.fragment_tasks) {
class TasksFragment : Fragment(R.layout.fragment_tasks), TaskAdapter.OnItemClickListener {
    private val viewModel : TasksViewModel by viewModels() //property delegation. Note that this instance is injected by dagger as we mentioned @AndroidEntryPoint.

    private lateinit var searchView : SearchView  //We're making this property of the class cuz, whenever the fragment is destroyed after configuration change, the searchView sends an empty string as i/p, which makes searchQuery value in our viewModel empty.  Hence we put searchView's query from viewModel back into the  searchItemWidget.

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {      //This func is called when the layout/fragment above was instantiated.
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentTasksBinding.bind(view)   //As the layout is already generated, we don't use .inflate() as before.

        val taskAdapter = TaskAdapter(this)  //The listener which we've to pass here is the fragment itself, as it implements the interface.

        binding.apply {
            recyclerViewTasks.apply {
                adapter = taskAdapter        //This is similar to binding.recyclerViewTasks.adapter = taskAdapter.
                layoutManager = LinearLayoutManager(requireContext())          //requireContext() is a func of fragment.
                setHasFixedSize(true)
            }

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){      //"or" : binary operator. In java : '|'
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = taskAdapter.currentList[viewHolder.adapterPosition]
                    //As the fragment should'nt be responsible for deciding what and how to delete, hence we should now pass on this responsibility to viewModel.
                    viewModel.onTaskSwiped(task)
                }
            }).attachToRecyclerView(recyclerViewTasks)

            fabAddTask.setOnClickListener {
                viewModel.onAddNewTaskClick()
            }    //Fragments should'nt be responsible for navigating to the addEditFrag, but we let the viewModel decide regarding this navigation.

        }

        setFragmentResultListener("add_edit_request") { //The other half to the fragment result.
            _, bundle ->
            val result = bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)  //Again delegate the logic.
        }

        viewModel.tasks.observe(viewLifecycleOwner) {
            taskAdapter.submitList(it)  //This lambda is the function which'll be executed when we are provided with new list of data. This is the trailing lambda syntax which is used when lambda is the last argument. submitList is the func of listAdapter which passes new list to it and then the old and new data is compared.
        }

        //Flow can only be collected from a coroutine. Cuz, the work inside a Flow/channel can suspend for eg when waiting for new values,etc and we don't want to block the UI thread, hence launch coroutine.
        //Notice that we're collecting flow in onCreateOptionsMenu as well using first(), but we use collect() here, as want the data to be refreshed constantly.

        viewLifecycleOwner.lifecycleScope.launchWhenStarted { //We're using lifecycleScope cuz, once this fragment(lifeCycle) goes into background, we don't care about displaying the taskEvents(snackBar,navigation,etc) when the view is not available. launchWhenStarted makes the scope of the coroutine smaller. When the fragment is in the background we don't listen for any events.
            viewModel.taskEvent.collect { //We're passed the stream of values from the viewModel, which we can use to do something. Here, taskEvents objects.
                event ->
                when(event){
                    is TasksViewModel.TaskEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(requireView(), "Task deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") //trailing lambda
                            {
                                viewModel.onUndoDeleteClick(event.task)  //smartCast This turns this event into not TaskEvent but the subClass ShowUndoDeleteTaskMessage, as we're in the when block.
                            }.show()
                    }
                    is TasksViewModel.TaskEvent.NavigateToAddTaskScreen -> {
                        val action = TasksFragmentDirections.actionTasksFragment2ToAddEditTaskFragment(null, "New Task")  //Whenever we want to navigatee from this mainFragment to the assEditFrag, it's mandatory to pass the title for it, as we made it non-nullable.
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TaskEvent.NavigateToEditTaskScreen -> {
                        val action = TasksFragmentDirections.actionTasksFragment2ToAddEditTaskFragment(event.task, "Edit task")
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TaskEvent.ShowTaskSavedConfirmationMessages -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                    }
                    //The deleteAllItemsDialog survives configuration changes cuz, we've wrapped it in a dialogFragment and used as a destination in our navGraph.
                    TasksViewModel.TaskEvent.NavigateToDeleteAllCompletedScreen -> {
                        val action = TasksFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment()
                        findNavController().navigate(action)
                    }
                }.exhaustive  //To get compile time safety
            }
        }

        setHasOptionsMenu(true) //For activating optionsMenu in the fragment.

    }

    override fun onItemClick(task: Task) {
        //delegate the work to viewModel
        viewModel.onTaskSelected(task)
    }

    override fun onCheckBoxClicked(task: Task, isChecked: Boolean) {
        //delegate the work to viewModel
        viewModel.onTaskCheckedChanged(task, isChecked)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_tasks, menu)     //inflater : Converts the layout file to actual menu object which can be used by code.

        val searchItem = menu.findItem(R.id.action_search)
//        val searchView = searchItem.actionView as SearchView ....... bug
        searchView = searchItem.actionView as SearchView  //We can use this search view as normal editText to do something with the text passed within the searchItem. We have to now use onQueryTextChangedListener() but it contains 2 method but only one is useful for us, hence we create extension function in ViewExt.

        val pendingQuery = viewModel.searchQuery.value
        if(pendingQuery != null && pendingQuery.isNotEmpty()){
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery, false)  //We're putting searchView's query from viewModel back into the  searchItemWidget.
        }

        searchView.onQueryTextChanged {
            //update search query
            viewModel.searchQuery.value = it
        } //onQueryTextChanged() is our self created extension func. Notice we're not passing arguments to the fuc normally. We're using trailing lambda feature.

        //We've to get the state of the checkBox of hideCompleted which have to retrieve from preferenceManager. But as we're reading the current state of the hideCompleted from the flow, we need to launch coroutine.
        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed_tasks).isChecked =
                viewModel.preferencesFlow.first().hideCompleted          //first() cuz, we just want to read the state once(after starting the app)and don't want to get this updated again and again as it's going to remain the same till it's manually changed. Hence coroutine will get cancelled for this state after first reading.
        }
        //viewLifecycleOwner.lifecycleScope is Coroutine scope which lives as long as the fragment is alive.
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {  //The below items are special case like searchView, hence we need to override this func.
        return when(item.itemId){
            R.id.action_sort_by_name -> {
//                viewModel.sortOrder.value = SortOrder.BY_NAME
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.action_sort_by_date_created ->{
//                viewModel.sortOrder.value = SortOrder.BY_DATE
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.action_hide_completed_tasks -> {
                item.isChecked = !item.isChecked    //This logic toggles the checkBox.
//                viewModel.hideCompleted.value = item.isChecked  //item.isChecked : current state of the checkBox.
                viewModel.onHideCompletedClick(item.isChecked)
                true
            }
            R.id.action_deleteAll_completed_tasks -> {
                viewModel.onDeleteAllCompletedClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        searchView.setOnQueryTextListener(null)  //Remove the listener so that we don't pass empty string after the viewDestruction of fragment.
    }

}