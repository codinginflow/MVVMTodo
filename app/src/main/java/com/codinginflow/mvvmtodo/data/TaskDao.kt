package com.codinginflow.mvvmtodo.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

//Note : Our task table is represented by our task data class.
@Dao
interface TaskDao {

    fun getTasks(query: String, sortOrder: SortOrder, hideCompleted: Boolean) : Flow<List<Task>> =  //Due to this change in getTasks() we can simultaneously search as well as sort, which can't be done if we had different functions for searchQuery, sortName, etc.
        when(sortOrder){ //Notice that we're going to get sortOrder, query, hideCompleted even if one is triggered by the user, cuz we've used combine in viewModel while calling this func.
            SortOrder.BY_DATE -> getTasksSortedByDateCreated(query, hideCompleted)
            SortOrder.BY_NAME -> getTasksSortedByName(query, hideCompleted)
        }

    /*//This is the way to get the data out of our Room dataBase.
    @Query("SELECT * FROM task_table WHERE name LIKE '%' || :searchQuery || '%' ORDER BY important DESC") // name cuz, we want to search on the basis of name property. LIKE cuz, the i/p does'nt have to be exactly matching as of name property, but anywhere in between. "||" : append operator, we want build string out of multiple strings."searchQuery" parameter of the getTasks() func. The percentage sign means the i/p can be anywhere in the middle or end or beginning.
    fun getTasks(searchQuery : String) : Flow<List<Task>>   //Flow is a asynchronous stream of data which returns not only one list of tasks, but many. The flow can be used or collected within the coroutines, hence we don't mention suspend*/

    //As we have 3 flow : searchQuery, sortQuery and hideCompleted... we have to make different functions and no longer can use getTasks() as above. Cuz, we cam't pass columnName(properties) as a variable to the room query for e.g : completed, name, etc. Instead the below functions will be used as lambda in the new getTasks().
    @Query("SELECT * FROM task_table WHERE(completed != :hideCompleted OR completed == 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC, name") //First the tasks will be sorted by the important state and then by the name simultaneously. "completed" is the column name of the task table. We're adding it in parentheses cuz, AND has preference over everything due to which our query will malfunction.
    fun getTasksSortedByName(searchQuery : String, hideCompleted : Boolean) : Flow<List<Task>>

//    completed != :hideCompleted OR completed == 0 :- If hideCompleted is true, then show all uncompleted tasks, else show all completed tasks. Also, if hideCompleted is false, then we want to show all tasks along with completed ones as well as unCompleted. '0' means false in sqLite. Basically show all unCompleted tasks under all possible circumstances.

    @Query("SELECT * FROM task_table WHERE(completed != :hideCompleted OR completed == 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC, created")
    fun getTasksSortedByDateCreated(searchQuery : String, hideCompleted : Boolean) : Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)  //The code after the @Insert is just metadata, which tells room what to do if the ids of the task are same.
    //The suspend modifier turns this function into a suspend function(Belongs to kotlin coroutine feature). This allows us to use the function in a different thread i.e background thread, without freezing the mainThread, inserting, updating, etc into the database is an IO operation which means that it'll take some moments. This is the easiest way to use background threads in kotlin. suspend functions can only be called from another suspend function or from a coroutine.
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM task_table WHERE completed = 1")  //We're not using * cuz, we want ot delete the whole row and not specific columns. 1 : true.
    suspend fun deleteCompletedTask()

}