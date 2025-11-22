package com.iamashad.foxtodo

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.iamashad.foxtodo.data.local.AppDatabase
import com.iamashad.foxtodo.data.local.TaskDao
import com.iamashad.foxtodo.data.local.TaskEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TaskDaoInstrumentedTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: TaskDao

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.taskDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insert_and_getById() = runBlocking {
        val id = dao.insert(TaskEntity(title = "t"))
        val loaded = dao.getById(id)
        assertEquals("t", loaded?.title)
    }
}
