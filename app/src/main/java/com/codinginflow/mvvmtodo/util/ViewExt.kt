package com.codinginflow.mvvmtodo.util

import androidx.appcompat.widget.SearchView
/*Util : Contains utility classes which can be used in different places of our code.
We'll create extension func for different views.
Procedure :
* inline keyword is for efficiency.
i)Take the className of which we want to create extension func and then .theNameWeWantToGive
ii)Pass the parameters as per need(here lambda).
iii)this
#crossinline makes sure that we don't/can't return anything after this extension function is used.
*/

inline fun SearchView.onQueryTextChanged(crossinline listener : (String) -> Unit){    //listener is the func which has parameter of string and returns nothing(Unit).
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener { //anonymous innerClass
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true  //This is the function which we did'nt want to implement and hence we created this manual extension function.
        }

        override fun onQueryTextChange(newText: String?): Boolean {  //We don't want confirmation for filtering, but the filtering should happen realTime, as we write anything in the searchItem. This method will take care about that as the item will be triggered as soon as we write anything.
            listener(newText.orEmpty())  //We are constantly passing whats written in the searchItem to the listener func. orEmpty() returns null if nothing is written in the searchItem.
            return true
        }
    })
}