package com.codinginflow.mvvmtodo.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete()

    //here we didn't use suspend because we already use flow..
    @Query("SELECT * From task_table")
    fun getAllTasks(): Flow<List<Task>>

}