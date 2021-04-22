package com.codinginflow.mvvmtodo.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PreferencesManager"

enum class SortOrder{
    BY_NAME, BY_DATE
}

data class FilterPreferences(val sortOrder: SortOrder, val hideCompleted : Boolean) //To be able to return two values from preferenceFlow.

//We're constructor injecting cuz, viewModel will use this class which is created by us(look in the constructor of viewModel).
@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context : Context){ //We're passing @ApplicationContext context : Context ...cuz, PreferencesManager does'nt belong to a particular activity but to the whole application.

    private val dataStore = context.createDataStore("user_preferences")  //We're creating the dataStore using the context passed.

    val preferenceFlow = dataStore.data
        .catch { exception ->            //catch is the flow operator to catch exceptions.
            if(exception is IOException){
                Log.e(TAG, "Error reading preferences ", exception )
                emit(emptyPreferences()) //emptyPreferences() is from dataStore library. This is used, so that app does'nt crash and just returns default settings.
            }else{
                throw exception
            }

        }
        .map { preferences ->  //The flow coming from the dataStore is verbose. Hence we're transforming it in such a way that it's easier to use. By using .map we now use the map transformation block to take each value that comes through the flow(dataStore.data) and transform into a easy to use way. The preferences flow will return this transformed value.
            //PreferencesKeys are the keys which helps us to distinguish between different values we store into the dataStore.
            val sortOrder = SortOrder.valueOf(    //valueOf converts the SortOrder enum into a String.
                preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.BY_DATE.name  //.name property converts and gives us String.
            )
            val hideCompleted = preferences[PreferencesKeys.HIDE_COMPLETED] ?: false //As this one is not an enum.
            FilterPreferences(sortOrder, hideCompleted)  //This is getting returned from the preferenceFlow instance.
        }

    //Update the existing data i.e when the sortOrder or HideCompleted changes. As the update is an IO operation which may consume mainThread.
    suspend fun updateSortOrder(sortOrder: SortOrder){
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updateHideCompleted(hideCompleted: Boolean){
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIDE_COMPLETED] = hideCompleted
        }
    }

    //PreferencesKeys are the keys which helps us to distinguish between different values we store into the dataStore.
    private object PreferencesKeys{
        val SORT_ORDER = preferencesKey<String>("sort_order")  //Notice that the SortOrder is an enum, which we'll convert into string later.
        val HIDE_COMPLETED = preferencesKey<Boolean>("hide_completed")
    }

}