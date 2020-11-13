package com.davenet.notely.service

import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import com.davenet.notely.database.getDatabase
import com.davenet.notely.repository.NoteListRepository

class RescheduleAlarmsService : LifecycleService() {
    private val noteListRepository = NoteListRepository(getDatabase(application))

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        noteListRepository.notes.observe(this, {
            it?.let {
                for (note in it) {
                    if (note.started) {
                        noteListRepository.schedule(baseContext, note)
                    }
                }
            }
        })
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }
}