package com.example.midterm_ai.ui

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.midterm_ai.R
import com.example.midterm_ai.data.model.Task

class TaskAdapter(
    private var tasks: List<Task> = emptyList(),
    private val onToggleDone: (Task) -> Unit,
    private val onDeleteTask: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    fun submitList(newList: List<Task>) {
        tasks = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_task_title)
        private val chkDone: CheckBox = itemView.findViewById(R.id.chk_done)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_task)

        fun bind(task: Task) {
            // Đặt lại thuộc tính mặc định để không bị lỗi khi tái sử dụng view
            itemView.alpha = 1f
            itemView.translationX = 0f

            tvTitle.text = task.title
            chkDone.setOnCheckedChangeListener(null) // Xóa listener cũ
            chkDone.isChecked = task.isDone

            if (task.isDone) {
                tvTitle.paintFlags = tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                tvTitle.paintFlags = tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            chkDone.setOnClickListener {
                if (chkDone.isChecked) {
                    // Khi người dùng tick vào (đã làm) -> Gạch ngang chữ
                    tvTitle.paintFlags = tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    
                    // Tạo hiệu ứng trượt sang phải và mờ dần
                    itemView.animate()
                        .alpha(0f)
                        .translationX(itemView.width.toFloat())
                        .setDuration(400)
                        .withEndAction {
                            // Sau khi hiệu ứng kết thúc thì xóa task đi
                            onDeleteTask(task)
                        }
                        .start()
                } else {
                    // Bỏ tick
                    tvTitle.paintFlags = tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    onToggleDone(task)
                }
            }

            btnDelete.setOnClickListener {
                // Hiệu ứng khi bấm nút xóa
                itemView.animate()
                    .alpha(0f)
                    .translationX(-itemView.width.toFloat())
                    .setDuration(300)
                    .withEndAction {
                        onDeleteTask(task)
                    }
                    .start()
            }
        }
    }
}
