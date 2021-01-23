package com.codinginflow.mvvmtodo.di

import android.app.Application
import androidx.room.Room
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.data.TaskDataBase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataBase(app: Application,callback:TaskDataBase.CallBack) =
        Room.databaseBuilder(app, TaskDataBase::class.java, "task_database")
            .fallbackToDestructiveMigration().addCallback(callback).build()

    @Provides
    fun providesTaskDao(db:TaskDataBase):TaskDao{
        return db.taskDao()
    }

    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

}