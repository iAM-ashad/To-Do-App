package com.iamashad.foxtodo.di

import android.content.Context
import androidx.room.Room
import com.iamashad.foxtodo.data.local.AppDatabase
import com.iamashad.foxtodo.data.local.TaskDao
import com.iamashad.foxtodo.data.repository.TaskRepositoryImpl
import com.iamashad.foxtodo.domain.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase {
        return Room.databaseBuilder(ctx, AppDatabase::class.java, "todo_db")
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    @Singleton
    fun provideTaskDao(db: AppDatabase) = db.taskDao()

    @Provides
    @Singleton
    fun provideTaskRepository(dao: TaskDao): TaskRepository {
        return TaskRepositoryImpl(dao)
    }
}
