package com.codinginflow.mvvmtodo.ui.tasks

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.codinginflow.mvvmtodo.data.TaskDao
import javax.inject.Inject

class TaskViewModel @ViewModelInject constructor(val taskDao: TaskDao): ViewModel() {
}