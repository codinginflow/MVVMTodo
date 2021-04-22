package com.codinginflow.mvvmtodo.util

//To make sure that the when statement/expression, for the the events is exhaustive.
val <T> T.exhaustive : T //This is an extension property. This basically returns the same object.
    get() = this