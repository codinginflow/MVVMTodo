package com.codinginflow.mvvmtodo.di

import android.app.Application
import androidx.room.Room
import com.codinginflow.mvvmtodo.data.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton
//To get the Dao in the required places, we're using dependency injection while creating the taskDatabase. We're using the providesMethod way of DI and not constructor injecting cuz, we don't own the below classes(Room)
@Module
@InstallIn(ApplicationComponent::class)   //As we've to use the same TaskDatabase throughout the app(singleton).
object AppModule {   //"object" is used to create objects of an anonymous class known as anonymous objects. They are used if you need to create an object of a slight modification of some class or interface without declaring a subclass for it.

    @Provides
    @Singleton
    fun provideDatabase(
        app : Application,
        callback : TaskDatabase.CallBack   //For separation of concerns, we create this callBack in taskDatabase, without DI we used to do in the same way i.e we used to create the callback in databaseClass.
    ) = Room.databaseBuilder(app, TaskDatabase::class.java, "task_database")
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()

    @Provides
    fun provideTaskDao(db : TaskDatabase) = db.taskDao()

    @ApplicationScope  //This way, we tell dagger that this the below scope of coroutine is not just any CoroutineScope, but also applicationScope.
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob()) //This is how we create coroutine scope that lives as long as the application lives.
    //With the help of SupervisorJob, we are telling the Coroutine that when any of it's child fails i.e any one operation fails, keep the other child/operation running.

}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope