package com.codinginflow.mvvmtodo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codinginflow.mvvmtodo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

//Example of where tu use DI : I)When viewModel needs DAO, it should'nt be responsible for searching the DAO. Instead of passing over the constructor to the viewModel, we use dagger.

@Database(entities = [Task::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao() : TaskDao

    //The below inner class(Callback) is just written to show some dummy data and pass it on to the DI(in AppModule), where we are creating TaskDatabase.

    class CallBack @Inject constructor(   //We're using DI cuz, we've to tell dagger how to provide this callBack to TaskDatabase. We're constructor injecting the class cuz we own and create this class.
        private val database: Provider<TaskDatabase>,  //With providers interface, we can get dependencies lazily Now dagger will create database when the onCreate is executed which happens after the build() is finished in AppModule. This way we can get the database into the callback.
        @ApplicationScope private val applicationScope : CoroutineScope  //We're injecting the coroutineScope here. The applicationScope returns CoroutineScope, take a look at this in ApplicationModule. If we later added one more CoroutineScope, maybe at activityLevel, things would get mixed up for dagger. Hence by adding : @ApplicationScope, we prevent ourSelf from ambiguity between coroutineScope.
    ) : RoomDatabase.Callback(){

        //The issue : In order to do our database operations, we need DAO. In order to get DAO we need instance of taskDatabase. But taskDatabase is waiting for the callback to get dagger constructed. Hence we're stuck in circular dependency. Usually we'd do "private val database: TaskDatabase" in this callback's constructor. But we change it to : "private val database: Provider<TaskDatabase>"
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            //db operations :
            val dao = database.get().taskDao()  //This method belongs to the above Provider interface.

            //As the database operations are suspending functions, so they need coroutines to get executed. The coroutine also needs scope which'll tell how long the coroutine should work. As we want our database operations to be executed at appLevel, we create our own scope. We want dagger to create this scope so that we can can use it wherever required.
            applicationScope.launch {
                dao.insert(Task("Wash the dishes"))  //name is the only mandatory argument to pass in the constructor of this data class, as rest of them have default ones.
                dao.insert(Task("Do the laundry", important = true))
                dao.insert(Task("Call Elon musk", completed = true))
                dao.insert(Task("Call mom"))
                dao.insert(Task("Visit grandma", completed = true))
                dao.insert(Task("Buy groceries"))
            }  //We're launching the coroutine scope here and executing the suspend functions.

        }
    }

}