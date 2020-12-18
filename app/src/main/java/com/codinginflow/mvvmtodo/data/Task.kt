package com.codinginflow.mvvmtodo.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat

/**
 * using data class in kotlin make you have predefined fun like setter/getters also equal and hashcode
 * use parcelize because you want to send Object between fragments..
 */
@Entity(tableName = "task_table")
@Parcelize
data class Task(
    //if you want the name of coulum in db is diffrent from the name used here then use  @ColumnInfo()
    // Only Name is mandatory here the others have default data..
    @ColumnInfo(name = "Task_Name")
    val name: String,
    val important: Boolean = false,
    val completed: Boolean = false,
    val created: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Int = 0
) : Parcelable {
    //CALL THIS METHOD TO RETURN THE created date in specific format.
    val createdDateFormatted: String
        get() = DateFormat.getDateTimeInstance().format(created)
}