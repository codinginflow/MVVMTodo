package com.codinginflow.mvvmtodo.ui.deleteallcompleted

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DeleteAllCompletedViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @ApplicationScope private val applicationScope : CoroutineScope   //On clicking a button, the dialog gets dismissed automatically. We're not navigating, but just making changes realTime. ViewModel will  get deleted from the memory once the items are deleted, as the dialog fragment is destroyed, hence using viewModelScope won't work. Due to this issue, we need larger scope. We're using @ApplicationScope instead of viewModelScope(like before) cuz, we don't want the things to perish once the viewModel is destroyed.
) : ViewModel() {
    fun onConfirmClick() = applicationScope.launch {
        taskDao.deleteCompletedTask()
    }
}