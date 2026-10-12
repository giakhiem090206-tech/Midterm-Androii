package com.example.midterm_ai

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.midterm_ai.data.local.AppDatabase
import com.example.midterm_ai.data.local.EventEntity
import com.example.midterm_ai.data.repository.TaskRepository
import com.example.midterm_ai.data.repository.TaskRepositoryImpl
import com.example.midterm_ai.data.weather.WeatherApiService
import com.example.midterm_ai.data.weather.getWeatherConditionText
import com.example.midterm_ai.music.MusicPlayerManager
import com.example.midterm_ai.ui.EventAdapter
import com.example.midterm_ai.ui.TaskAdapter
import com.example.midterm_ai.widget.CalendarWidgetProvider
import com.example.midterm_ai.widget.MusicWidgetProvider
import com.example.midterm_ai.widget.TaskWidgetProvider
import com.example.midterm_ai.widget.WeatherWidgetProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    // Tab Layouts
    private lateinit var layoutClock: View
    private lateinit var layoutWeather: View
    private lateinit var layoutTodo: View
    private lateinit var layoutMusic: View
    private lateinit var layoutCalendar: View
    private lateinit var bottomNav: BottomNavigationView

    // Weather Components
    private lateinit var tvWeatherTemp: TextView
    private lateinit var tvWeatherCondition: TextView
    private lateinit var btnWeatherRefresh: Button

    // To-Do Components
    private lateinit var taskRepository: TaskRepository
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var editTaskTitle: EditText
    private lateinit var btnAddTask: Button
    private lateinit var listTasks: RecyclerView

    // Music Components
    private lateinit var tvMusicTitle: TextView
    private lateinit var tvMusicStatus: TextView
    private lateinit var btnMusicPrev: Button
    private lateinit var btnMusicPlay: Button
    private lateinit var btnMusicNext: Button

    // Calendar Components
    private lateinit var database: AppDatabase
    private lateinit var eventAdapter: EventAdapter
    private lateinit var editEventTitle: EditText
    private lateinit var editEventDate: EditText
    private lateinit var editEventTime: EditText
    private lateinit var btnAddEvent: Button
    private lateinit var listEvents: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = AppDatabase.getInstance(applicationContext)
        taskRepository = TaskRepositoryImpl(database.taskDao())

        initViews()
        setupNavigation()
        setupTodoSection()
        setupWeatherSection()
        setupMusicSection()
        setupCalendarSection()

        // Tab mặc định: chọn trên thanh điều hướng để icon "To-Do" cũng sáng lên
        // (trước đây thanh dưới sáng "Clock" trong khi nội dung là To-Do)
        bottomNav.selectedItemId = R.id.nav_todo
    }

    override fun onResume() {
        super.onResume()
        // Công việc có thể vừa được đánh dấu xong từ widget -> tải lại danh sách
        if (layoutTodo.visibility == View.VISIBLE) loadTasks()
    }

    private fun initViews() {
        layoutClock = findViewById(R.id.layout_tab_clock)
        layoutWeather = findViewById(R.id.layout_tab_weather)
        layoutTodo = findViewById(R.id.layout_tab_todo)
        layoutMusic = findViewById(R.id.layout_tab_music)
        layoutCalendar = findViewById(R.id.layout_tab_calendar)
        bottomNav = findViewById(R.id.bottom_navigation)

        // Weather
        tvWeatherTemp = findViewById(R.id.main_tv_weather_temp)
        tvWeatherCondition = findViewById(R.id.main_tv_weather_condition)
        btnWeatherRefresh = findViewById(R.id.main_btn_weather_refresh)

        // To-Do
        editTaskTitle = findViewById(R.id.edit_task_title)
        btnAddTask = findViewById(R.id.btn_add_task)
        listTasks = findViewById(R.id.list_tasks)

        // Music
        tvMusicTitle = findViewById(R.id.main_tv_music_title)
        tvMusicStatus = findViewById(R.id.main_tv_music_status)
        btnMusicPrev = findViewById(R.id.main_btn_music_prev)
        btnMusicPlay = findViewById(R.id.main_btn_music_play)
        btnMusicNext = findViewById(R.id.main_btn_music_next)

        // Calendar
        editEventTitle = findViewById(R.id.edit_event_title)
        editEventDate = findViewById(R.id.edit_event_date)
        editEventTime = findViewById(R.id.edit_event_time)
        btnAddEvent = findViewById(R.id.btn_add_event)
        listEvents = findViewById(R.id.list_events)
    }

    private fun setupNavigation() {
        bottomNav.setOnItemSelectedListener { item ->
            showTab(item.itemId)
            true
        }
    }

    private fun showTab(itemId: Int) {
        layoutClock.visibility = if (itemId == R.id.nav_clock) View.VISIBLE else View.GONE
        layoutWeather.visibility = if (itemId == R.id.nav_weather) View.VISIBLE else View.GONE
        layoutTodo.visibility = if (itemId == R.id.nav_todo) View.VISIBLE else View.GONE
        layoutMusic.visibility = if (itemId == R.id.nav_music) View.VISIBLE else View.GONE
        layoutCalendar.visibility = if (itemId == R.id.nav_calendar) View.VISIBLE else View.GONE

        if (itemId == R.id.nav_weather) fetchWeather()
        if (itemId == R.id.nav_todo) loadTasks()
        if (itemId == R.id.nav_calendar) loadEvents()
    }

    private fun setupTodoSection() {
        taskAdapter = TaskAdapter(
            onToggleDone = { task ->
                lifecycleScope.launch {
                    taskRepository.toggleDone(task.id)
                    loadTasks()
                }
            },
            onDeleteTask = { task ->
                lifecycleScope.launch {
                    taskRepository.deleteTask(task.id)
                    loadTasks()
                }
            }
        )
        listTasks.layoutManager = LinearLayoutManager(this)
        listTasks.adapter = taskAdapter

        btnAddTask.setOnClickListener {
            val title = editTaskTitle.text.toString().trim()
            if (title.isNotEmpty()) {
                lifecycleScope.launch {
                    taskRepository.addTask(title)
                    editTaskTitle.setText("")
                    loadTasks()
                }
            } else {
                Toast.makeText(this, "Vui lòng nhập tên công việc", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadTasks() {
        lifecycleScope.launch {
            val tasks = taskRepository.getAllTasks()
            taskAdapter.submitList(tasks)
            TaskWidgetProvider.updateWidget(this@MainActivity)
        }
    }

    private fun setupWeatherSection() {
        btnWeatherRefresh.setOnClickListener {
            fetchWeather()
        }
    }

    private fun fetchWeather() {
        tvWeatherCondition.text = "Đang tải dữ liệu thời tiết..."
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = WeatherApiService.create().getWeather()
                if (response.isSuccessful && response.body()?.currentWeather != null) {
                    val weather = response.body()!!.currentWeather!!
                    val temp = "${weather.temperature.toInt()}°C"
                    val condition = getWeatherConditionText(weather.weathercode)

                    withContext(Dispatchers.Main) {
                        tvWeatherTemp.text = temp
                        tvWeatherCondition.text = condition
                        // Trước đây truyền id = 0 (không tồn tại) nên widget không được cập nhật
                        WeatherWidgetProvider.updateAllWeatherWidgets(this@MainActivity)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        tvWeatherCondition.text = "Không thể kết nối API thời tiết"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvWeatherCondition.text = "Lỗi mạng hoặc ngoại tuyến"
                }
            }
        }
    }

    private fun setupMusicSection() {
        updateMusicUI()

        btnMusicPlay.setOnClickListener {
            MusicPlayerManager.playPause(this)
            updateMusicUI()
        }

        btnMusicNext.setOnClickListener {
            MusicPlayerManager.nextTrack(this)
            updateMusicUI()
        }

        btnMusicPrev.setOnClickListener {
            MusicPlayerManager.previousTrack(this)
            updateMusicUI()
        }
    }

    private fun updateMusicUI() {
        val track = MusicPlayerManager.getCurrentTrack()
        val isPlaying = MusicPlayerManager.isPlaying

        if (track != null) {
            tvMusicTitle.text = track.title
            tvMusicStatus.text = if (isPlaying) "Đang phát..." else "Đã tạm dừng"
        } else {
            tvMusicTitle.text = "Music Player"
            tvMusicStatus.text = "Chưa phát nhạc"
        }
    }

    private fun setupCalendarSection() {
        eventAdapter = EventAdapter(
            onDeleteEvent = { event ->
                lifecycleScope.launch {
                    database.eventDao().deleteEventById(event.id)
                    loadEvents()
                }
            }
        )
        listEvents.layoutManager = LinearLayoutManager(this)
        listEvents.adapter = eventAdapter

        btnAddEvent.setOnClickListener {
            val title = editEventTitle.text.toString().trim()
            val date = editEventDate.text.toString().trim()
            val time = editEventTime.text.toString().trim()

            if (title.isNotEmpty() && date.isNotEmpty()) {
                lifecycleScope.launch {
                    database.eventDao().insertEvent(
                        EventEntity(title = title, date = date, time = time.ifEmpty { "Cả ngày" })
                    )
                    editEventTitle.setText("")
                    editEventDate.setText("")
                    editEventTime.setText("")
                    loadEvents()
                }
            } else {
                Toast.makeText(this, "Vui lòng nhập tên sự kiện và ngày", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadEvents() {
        lifecycleScope.launch {
            val events = database.eventDao().getAllEvents()
            eventAdapter.submitList(events)
            CalendarWidgetProvider.updateCalendarWidget(this@MainActivity)
        }
    }
}
