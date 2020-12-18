package com.codinginflow.mvvmtodo.ui.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.codinginflow.mvvmtodo.data.Task
import kotlinx.coroutines.launch

class TaskViewModel(private val taskRepo: TaskRepository) : ViewModel() {

    val tasks: LiveData<List<Task>> = taskRepo.getAllTask().asLiveData()

    fun insert(task: Task) = viewModelScope.launch {
        taskRepo.insert(task)
    }

}