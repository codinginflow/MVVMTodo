package com.codinginflow.mvvmtodo.ui.tasks

import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import com.codinginflow.mvvmtodo.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@Keep
class TasksFragment() : Fragment(R.layout.fragment_task) {

    private val viewModel:TaskViewModel by viewModels()
}