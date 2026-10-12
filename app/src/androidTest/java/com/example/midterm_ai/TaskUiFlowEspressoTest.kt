package com.example.midterm_ai

import android.content.Context
import android.os.SystemClock
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.midterm_ai.data.local.AppDatabase
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI TEST (tầng đỉnh Testing Pyramid) — mô phỏng người dùng thật bấm trên màn hình To-Do.
 *
 * Ghi chú:
 * - Dùng replaceText thay vì typeText vì typeText không gõ được chữ có dấu tiếng Việt.
 * - Room chạy trên luồng nền mà Espresso không tự chờ, nên các kiểm tra kết quả
 *   được bọc trong eventually { } (thử lại tối đa 3 giây).
 * - Mỗi test dùng tên công việc riêng (có timestamp) vì app dùng database thật.
 */
@RunWith(AndroidJUnit4::class)
class TaskUiFlowEspressoTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun addTask_showsInListAndClearsInput() {
        val title = uniqueTitle("Thêm việc")

        addTaskThroughUi(title)

        eventually { onView(withId(R.id.edit_task_title)).check(matches(withText(""))) }
    }

    @Test
    fun emptyTitle_doesNotAddAnything() {
        val countBefore = taskCountInDatabase()

        onView(withId(R.id.edit_task_title)).perform(replaceText("   "), closeSoftKeyboard())
        onView(withId(R.id.btn_add_task)).perform(click())

        SystemClock.sleep(500)
        assertEquals(countBefore, taskCountInDatabase())
    }

    @Test
    fun tickCheckbox_marksTaskDone_onScreenAndInDatabase() {
        val title = uniqueTitle("Đánh dấu xong")
        addTaskThroughUi(title)

        onView(allOf(withId(R.id.chk_done), hasSibling(withText(title)))).perform(click())

        eventually {
            onView(allOf(withId(R.id.chk_done), hasSibling(withText(title))))
                .check(matches(isChecked()))
        }
        eventually {
            val saved = runBlocking { taskDao().getAllTasks() }.first { it.title == title }
            assertTrue("Database phải lưu isDone = true", saved.isDone)
        }
    }

    @Test
    fun deleteButton_removesTask_fromScreenAndDatabase() {
        val title = uniqueTitle("Xoá việc")
        addTaskThroughUi(title)

        onView(allOf(withId(R.id.btn_delete_task), hasSibling(withText(title)))).perform(click())

        eventually { onView(withText(title)).check(doesNotExist()) }
        eventually {
            val stillThere = runBlocking { taskDao().getAllTasks() }.any { it.title == title }
            assertTrue("Database không được còn công việc đã xoá", !stillThere)
        }
    }

    // ---------- Hàm hỗ trợ ----------

    private fun addTaskThroughUi(title: String) {
        onView(withId(R.id.edit_task_title)).perform(replaceText(title), closeSoftKeyboard())
        onView(withId(R.id.btn_add_task)).perform(click())
        eventually { onView(withText(title)).check(matches(isDisplayed())) }
    }

    private fun uniqueTitle(prefix: String) = "$prefix ${System.currentTimeMillis()}"

    private fun taskDao() =
        AppDatabase.getInstance(ApplicationProvider.getApplicationContext<Context>()).taskDao()

    private fun taskCountInDatabase(): Int = runBlocking { taskDao().getAllTasks().size }

    private fun eventually(timeoutMs: Long = 3000, assertion: () -> Unit) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (true) {
            try {
                assertion()
                return
            } catch (t: Throwable) {
                if (SystemClock.uptimeMillis() > deadline) throw t
                SystemClock.sleep(100)
            }
        }
    }
}
