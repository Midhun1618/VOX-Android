package com.voxcom.vox.data

import com.voxcom.vox.data.model.Task
import com.voxcom.vox.data.model.TaskState

object TaskManager {

    private var tasks: List<Task> = emptyList()

    private val listeners = mutableListOf<(List<Task>) -> Unit>()

    fun update(newTasks: List<Task>) {
        tasks = newTasks
        notifyAllListeners()
    }

    fun observe(listener: (List<Task>) -> Unit) {
        listeners.add(listener)
        listener(tasks)
    }

    private fun notifyAllListeners() {
        listeners.forEach { it(tasks) }
    }

    fun active() = tasks.filter { it.state() == TaskState.ACTIVE }

    fun history() = tasks.filter { it.state() != TaskState.ACTIVE }

    fun stats(): Triple<Int, Int, Int> {
        println("TASKS INSIDE MANAGER = ${tasks.size}")
        val total = tasks.size
        val completed = tasks.count { it.state() == TaskState.COMPLETED }
        val missed = tasks.count { it.state() == TaskState.MISSED }
        return Triple(total, completed, missed)
    }
}
