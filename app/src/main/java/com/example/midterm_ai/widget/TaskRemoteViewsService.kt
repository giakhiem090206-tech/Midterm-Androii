package com.example.midterm_ai.widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.midterm_ai.R
import com.example.midterm_ai.data.local.AppDatabase
import com.example.midterm_ai.data.model.Task
import com.example.midterm_ai.data.repository.TaskRepositoryImpl
import kotlinx.coroutines.runBlocking

class TaskRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TaskRemoteViewsFactory(this.applicationContext)
    }
}

class TaskRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var tasks: List<Task> = emptyList()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        // Query tasks synchronously inside runBlocking for Widget Factory
        val database = AppDatabase.getInstance(context)
        val repository = TaskRepositoryImpl(database.taskDao())
        runBlocking {
            tasks = repository.getAllTasks()
        }
    }

    override fun onDestroy() {
        tasks = emptyList()
    }

    override fun getCount(): Int = tasks.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position < 0 || position >= tasks.size) {
            return RemoteViews(context.packageName, R.layout.widget_task_item)
        }

        val task = tasks[position]
        val views = RemoteViews(context.packageName, R.layout.widget_task_item)

        views.setTextViewText(R.id.tv_widget_task_title, task.title)
        if (task.isDone) {
            views.setTextViewText(R.id.tv_widget_task_status, "✓ Đã xong")
            views.setTextColor(R.id.tv_widget_task_status, Color.parseColor("#38A169"))
        } else {
            views.setTextViewText(R.id.tv_widget_task_status, "○ Chưa xong")
            views.setTextColor(R.id.tv_widget_task_status, Color.parseColor("#E53E3E"))
        }

        // Chạm vào dòng này -> TaskWidgetProvider nhận id công việc để đánh dấu Xong/Chưa xong
        val fillInIntent = Intent().putExtra(TaskWidgetProvider.EXTRA_TASK_ID, task.id)
        views.setOnClickFillInIntent(R.id.widget_item_container, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        return if (position in tasks.indices) tasks[position].id.toLong() else position.toLong()
    }

    override fun hasStableIds(): Boolean = true
}
