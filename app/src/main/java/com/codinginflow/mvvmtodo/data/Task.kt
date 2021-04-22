package com.codinginflow.mvvmtodo.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat

@Entity(tableName = "task_table")
@Parcelize  //This is an interface which helps us to send object(task) between the fragments. We use this later to send navigation argument to AddEditTaskViewModel.
data class Task(
    //Notice that we're making the below properties immutable, so that we've to later create a completely new object and update the database. This is important for debugging. This makes easier to compare objects.
    val name: String,
    val important: Boolean = false,
    val completed: Boolean = false,
    val created: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Int = 0
) : Parcelable {
    val createdDateFormatted: String  //We're manually formatting the data within the body cuz the formatted date is'nt something we want to pass over the constructor, or want it to be fixed.
        get() = DateFormat.getDateTimeInstance().format(created)  //By using getter method, we're saying that whenever we are accessing the createdDateFormatted property, execute the following code automatically.
}