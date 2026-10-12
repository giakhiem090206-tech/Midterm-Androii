package com.example.midterm_ai

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.midterm_ai.data.local.AppDatabase
import com.example.midterm_ai.data.local.TaskDao
import com.example.midterm_ai.data.local.TaskEntity
import com.example.midterm_ai.data.repository.TaskRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * INTEGRATION TEST (tầng giữa Testing Pyramid).
 * Dùng Room THẬT nhưng tạo database trong bộ nhớ (in-memory):
 * mỗi test có 1 database sạch, không đụng tới dữ liệu thật của app.
 * Cần chạy trên emulator/thiết bị.
 */
@RunWith(AndroidJUnit4::class)
class TaskDaoIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: TaskDao
    private lateinit var repository: TaskRepositoryImpl

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.taskDao()
        repository = TaskRepositoryImpl(dao)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertTask_thenReadBack() = runBlocking<Unit> {
        dao.insertTask(TaskEntity(title = "Học Android"))

        val all = dao.getAllTasks()
        assertEquals(1, all.size)
        assertEquals("Học Android", all[0].title)
        assertFalse(all[0].isDone)
        assertTrue("Room phải tự sinh id > 0", all[0].id > 0)
    }

    @Test
    fun getAllTasks_isOrderedNewestFirst() = runBlocking<Unit> {
        dao.insertTask(TaskEntity(title = "Thứ nhất"))
        dao.insertTask(TaskEntity(title = "Thứ hai"))
        dao.insertTask(TaskEntity(title = "Thứ ba"))

        assertEquals(
            listOf("Thứ ba", "Thứ hai", "Thứ nhất"),
            dao.getAllTasks().map { it.title }
        )
    }

    @Test
    fun updateTask_savesDoneFlag() = runBlocking<Unit> {
        dao.insertTask(TaskEntity(title = "Nộp bài"))
        val saved = dao.getAllTasks().single()

        dao.updateTask(saved.copy(isDone = true))

        assertTrue(dao.getTaskById(saved.id)!!.isDone)
    }

    @Test
    fun deleteTaskById_removesRow() = runBlocking<Unit> {
        dao.insertTask(TaskEntity(title = "Sẽ bị xoá"))
        val id = dao.getAllTasks().single().id

        dao.deleteTaskById(id)

        assertNull(dao.getTaskById(id))
        assertTrue(dao.getAllTasks().isEmpty())
    }

    @Test
    fun getTaskById_unknownId_returnsNull() = runBlocking<Unit> {
        assertNull(dao.getTaskById(12345))
    }

    @Test
    fun repository_withRealDatabase_fullFlow() = runBlocking<Unit> {
        // Repository + Room thật làm việc cùng nhau: thêm -> đánh dấu xong -> xoá
        repository.addTask("  Làm slide thuyết trình  ")
        val task = repository.getAllTasks().single()
        assertEquals("Làm slide thuyết trình", task.title)

        repository.toggleDone(task.id)
        assertTrue(repository.getAllTasks().single().isDone)

        repository.deleteTask(task.id)
        assertTrue(repository.getAllTasks().isEmpty())
    }
}
