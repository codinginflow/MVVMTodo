package com.codinginflow.mvvmtodo.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat


// Class to handle each task data
@Entity(tableName = "table_task")
@Parcelize
data class Task(
    val name: String,
    val important: Boolean = false,
    val completed: Boolean = false,
    val created: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id:Int = 0
):Parcelable {
    val createdDateFormatted: String
        get() = DateFormat.getDateInstance().format(created)
}
