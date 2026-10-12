package com.example.midterm_ai

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskActivityEspressoTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testViewsExistWithFixedIDs() {
        // Kiểm tra các View ID cố định đã được thỏa thuận giữa Người A và Người B
        onView(withId(R.id.edit_task_title)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_add_task)).check(matches(isDisplayed()))
        onView(withId(R.id.list_tasks)).check(matches(isDisplayed()))
    }

    @Test
    fun testAddTaskFlow() {
        val taskName = "Công việc Espresso Test"

        // Type task name and click Add
        onView(withId(R.id.edit_task_title))
            .perform(replaceText(taskName), closeSoftKeyboard())

        onView(withId(R.id.btn_add_task))
            .perform(click())

        // Verify task appears on UI
        onView(withText(taskName)).check(matches(isDisplayed()))
    }
}
