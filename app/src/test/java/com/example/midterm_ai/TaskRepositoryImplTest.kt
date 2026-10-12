package com.example.midterm_ai

import com.example.midterm_ai.data.local.TaskDao
import com.example.midterm_ai.data.local.TaskEntity
import com.example.midterm_ai.data.repository.TaskRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * DAO giả lưu dữ liệu trong bộ nhớ.
 * Nhờ nó, ta test được TaskRepositoryImpl THẬT (code chạy trong app)
 * mà không cần Room hay thiết bị Android -> chạy rất nhanh trên máy tính.
 */
private class InMemoryTaskDao : TaskDao {
    private val rows = mutableListOf<TaskEntity>()
    private var nextId = 1

    override suspend fun getAllTasks(): List<TaskEntity> =
        rows.sortedByDescending { it.id } // giống câu SQL: ORDER BY id DESC

    override suspend fun getTaskById(id: Int): TaskEntity? =
        rows.find { it.id == id }

    override suspend fun insertTask(task: TaskEntity) {
        val toSave = if (task.id == 0) task.copy(id = nextId++) else task
        rows.removeAll { it.id == toSave.id } // giống OnConflictStrategy.REPLACE
        rows.add(toSave)
    }

    override suspend fun deleteTaskById(id: Int) {
        rows.removeAll { it.id == id }
    }

    override suspend fun updateTask(task: TaskEntity) {
        val index = rows.indexOfFirst { it.id == task.id }
        if (index != -1) rows[index] = task
    }
}

/**
 * UNIT TEST (tầng đáy Testing Pyramid) cho TaskRepositoryImpl.
 */
class TaskRepositoryImplTest {

    private lateinit var dao: InMemoryTaskDao
    private lateinit var repository: TaskRepositoryImpl

    @Before
    fun setUp() {
        dao = InMemoryTaskDao()
        repository = TaskRepositoryImpl(dao)
    }

    @Test
    fun addTask_trimsWhitespace() = runBlocking<Unit> {
        repository.addTask("   Học Android   ")

        assertEquals("Học Android", repository.getAllTasks().single().title)
    }

    @Test
    fun addTask_blankTitle_isIgnored() = runBlocking<Unit> {
        repository.addTask("")
        repository.addTask("     ")

        assertTrue(repository.getAllTasks().isEmpty())
    }

    @Test
    fun addTask_newTaskIsNotDone() = runBlocking<Unit> {
        repository.addTask("Viết báo cáo")

        assertFalse(repository.getAllTasks().single().isDone)
    }

    @Test
    fun getAllTasks_returnsNewestFirst() = runBlocking<Unit> {
        repository.addTask("Việc cũ")
        repository.addTask("Việc mới")

        assertEquals(listOf("Việc mới", "Việc cũ"), repository.getAllTasks().map { it.title })
    }

    @Test
    fun toggleDone_twice_returnsToNotDone() = runBlocking<Unit> {
        repository.addTask("Quay video demo")
        val id = repository.getAllTasks().single().id

        repository.toggleDone(id)
        assertTrue(repository.getAllTasks().single().isDone)

        repository.toggleDone(id)
        assertFalse(repository.getAllTasks().single().isDone)
    }

    @Test
    fun toggleDone_unknownId_doesNothing() = runBlocking<Unit> {
        repository.addTask("Việc có thật")

        repository.toggleDone(999) // id không tồn tại -> không được crash

        assertFalse(repository.getAllTasks().single().isDone)
    }

    @Test
    fun deleteTask_removesOnlyThatTask() = runBlocking<Unit> {
        repository.addTask("Giữ lại")
        repository.addTask("Xoá đi")
        val idToDelete = repository.getAllTasks().first { it.title == "Xoá đi" }.id

        repository.deleteTask(idToDelete)

        val remaining = repository.getAllTasks()
        assertEquals(listOf("Giữ lại"), remaining.map { it.title })
        assertNull(dao.getTaskById(idToDelete))
    }
}
