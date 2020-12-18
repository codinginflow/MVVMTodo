package com.codinginflow.mvvmtodo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//this class should be abstract because we are not the one who implement DB Room Do this for us..
@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class ToDoDatabase : RoomDatabase() {

    /**
     * first we need to get handel of the function inside our TaskDao interface
     * so define this fun
     * abstract because we doesn't want to make any implementation.
     */
    abstract fun taskDao(): TaskDao

    /**
     * we need to have only one instance from the database(Singleton)
     * so make a companion object or create an object class..
     */
    companion object {
        @Volatile
        private var INSTANCE: ToDoDatabase? = null

        fun getDatabase(context: Context): ToDoDatabase {

            /**
             * Wrapping the code in synchronized(){}
             * Means, Only one thread can access the code at a time
             * So we make sure we have only one instace of DB
             * if the INSTANCE is not null, then return it,
             * if it is, then create the database
             */

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ToDoDatabase::class.java,
                    "ToDo_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}