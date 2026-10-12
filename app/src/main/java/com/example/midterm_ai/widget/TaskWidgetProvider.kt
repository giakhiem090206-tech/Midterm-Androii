package com.example.midterm_ai.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import com.example.midterm_ai.MainActivity
import com.example.midterm_ai.R
import com.example.midterm_ai.data.local.AppDatabase
import com.example.midterm_ai.data.repository.TaskRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_REFRESH_WIDGET -> updateWidget(context)

            // Người dùng chạm vào 1 công việc trên widget -> đảo trạng thái Xong/Chưa xong
            ACTION_TOGGLE_TASK -> {
                val taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
                if (taskId == -1) return
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val repository = TaskRepositoryImpl(AppDatabase.getInstance(context).taskDao())
                        repository.toggleDone(taskId)
                        updateWidget(context)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.midterm_ai.ACTION_REFRESH_WIDGET"
        const val ACTION_TOGGLE_TASK = "com.example.midterm_ai.ACTION_TOGGLE_TASK"
        const val EXTRA_TASK_ID = "com.example.midterm_ai.EXTRA_TASK_ID"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_task)

            // Gắn ListView với TaskRemoteViewsService (mỗi widget 1 Intent riêng)
            val serviceIntent = Intent(context, TaskRemoteViewsService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.widget_list_tasks, serviceIntent)
            views.setEmptyView(R.id.widget_list_tasks, R.id.widget_empty_view)

            // Chạm vào tiêu đề widget -> mở app.
            // (Không được gắn setOnClickPendingIntent lên ListView: Android sẽ báo lỗi và widget không hiện.)
            val openAppPendingIntent = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tv_widget_task_header, openAppPendingIntent)

            // Chạm vào từng dòng công việc -> gửi ACTION_TOGGLE_TASK (id công việc do từng dòng điền vào)
            val toggleTemplate = PendingIntent.getBroadcast(
                context, 1,
                Intent(context, TaskWidgetProvider::class.java).setAction(ACTION_TOGGLE_TASK),
                PendingIntent.FLAG_UPDATE_CURRENT or mutableFlag()
            )
            views.setPendingIntentTemplate(R.id.widget_list_tasks, toggleTemplate)

            // Nút làm mới
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context, 0,
                Intent(context, TaskWidgetProvider::class.java).setAction(ACTION_REFRESH_WIDGET),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_refresh, refreshPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list_tasks)
        }

        fun updateWidget(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, TaskWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_tasks)
        }

        /** Template của danh sách phải MUTABLE (Android 12+) để từng dòng điền được id công việc. */
        private fun mutableFlag(): Int =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
    }
}
