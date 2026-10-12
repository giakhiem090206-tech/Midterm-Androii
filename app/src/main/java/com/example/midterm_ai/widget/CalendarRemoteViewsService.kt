package com.example.midterm_ai.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.midterm_ai.R
import com.example.midterm_ai.data.local.AppDatabase
import com.example.midterm_ai.data.local.EventEntity
import kotlinx.coroutines.runBlocking

class CalendarRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return CalendarRemoteViewsFactory(this.applicationContext)
    }
}

class CalendarRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var events: List<EventEntity> = emptyList()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        val database = AppDatabase.getInstance(context)
        runBlocking {
            events = database.eventDao().getAllEvents()
        }
    }

    override fun onDestroy() {
        events = emptyList()
    }

    override fun getCount(): Int = events.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position < 0 || position >= events.size) {
            return RemoteViews(context.packageName, R.layout.widget_calendar_item)
        }

        val event = events[position]
        val views = RemoteViews(context.packageName, R.layout.widget_calendar_item)

        views.setTextViewText(R.id.tv_event_title, event.title)
        views.setTextViewText(R.id.tv_event_date_time, "${event.date} • ${event.time}")

        // Chạm vào sự kiện -> mở app (dùng template do CalendarWidgetProvider gắn lên ListView)
        views.setOnClickFillInIntent(R.id.widget_event_item_container, Intent())

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        return if (position in events.indices) events[position].id.toLong() else position.toLong()
    }

    override fun hasStableIds(): Boolean = true
}
