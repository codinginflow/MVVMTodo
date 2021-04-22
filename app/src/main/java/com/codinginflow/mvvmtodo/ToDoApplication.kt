package com.codinginflow.mvvmtodo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp  //To activate dagger hilt. Now go to manifest file and add this class as name attribute.
class ToDoApplication : Application() {

}