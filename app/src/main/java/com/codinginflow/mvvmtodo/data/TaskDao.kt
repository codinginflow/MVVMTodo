package com.codinginflow.mvvmtodo.data

import androidx.constraintlayout.helper.widget.Flow
import androidx.room.*

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task:Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete()

    //@Query("SELECT * From task_table")
    //fun get(): Flow<List<Task>>

    // test
}