package com.example.midterm_ai.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.midterm_ai.MainActivity
import com.example.midterm_ai.R
import com.example.midterm_ai.data.weather.WeatherApiService
import com.example.midterm_ai.data.weather.getWeatherConditionText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WeatherWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWeatherWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WEATHER) {
            updateAllWeatherWidgets(context)
        }
    }

    companion object {
        const val ACTION_REFRESH_WEATHER = "com.example.midterm_ai.ACTION_REFRESH_WEATHER"

        /** Cập nhật mọi Weather widget đang được ghim trên màn hình chính. */
        fun updateAllWeatherWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, WeatherWidgetProvider::class.java)
            for (appWidgetId in appWidgetManager.getAppWidgetIds(componentName)) {
                updateWeatherWidget(context, appWidgetManager, appWidgetId)
            }
        }

        fun updateWeatherWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_weather)

            // Click open app intent
            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPendingIntent = PendingIntent.getActivity(
                context, 0, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tv_weather_temp, openAppPendingIntent)

            // Click refresh intent
            val refreshIntent = Intent(context, WeatherWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_WEATHER
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context, 0, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_weather_refresh, refreshPendingIntent)

            // Show loading state
            views.setTextViewText(R.id.tv_weather_condition, "Đang tải dữ liệu...")
            appWidgetManager.updateAppWidget(appWidgetId, views)

            // Fetch real weather via Retrofit in background
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val apiService = WeatherApiService.create()
                    val response = apiService.getWeather()
                    if (response.isSuccessful && response.body()?.currentWeather != null) {
                        val weather = response.body()!!.currentWeather!!
                        val tempText = "${weather.temperature.toInt()}°C"
                        val conditionText = getWeatherConditionText(weather.weathercode)

                        withContext(Dispatchers.Main) {
                            views.setTextViewText(R.id.tv_weather_temp, tempText)
                            views.setTextViewText(R.id.tv_weather_condition, conditionText)
                            appWidgetManager.updateAppWidget(appWidgetId, views)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            views.setTextViewText(R.id.tv_weather_condition, "Không thể kết nối API")
                            appWidgetManager.updateAppWidget(appWidgetId, views)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        views.setTextViewText(R.id.tv_weather_condition, "Lỗi mạng / Ngoại tuyến")
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            }
        }
    }
}
