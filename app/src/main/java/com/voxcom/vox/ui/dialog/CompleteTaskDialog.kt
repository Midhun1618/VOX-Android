package com.voxcom.vox.ui.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.voxcom.vox.R
import com.voxcom.vox.data.repository.TaskRepository
import com.voxcom.vox.system.SoundPlayer

class CompleteTaskDialog(
    private val context: Context,
    private val taskId: String){
    fun show() {

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_complete_task, null)

        val dialog = AlertDialog.Builder(context).setView(view).create()

        view.findViewById<TextView>(R.id.btnYes).setOnClickListener {
            SoundPlayer.wake(context, R.raw.onclick01_sfx)
            TaskRepository.markCompleted(taskId)
            dialog.dismiss()
        }

        view.findViewById<TextView>(R.id.btnNo).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
