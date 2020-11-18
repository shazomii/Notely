package com.davenet.notely.service

import android.app.Application
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import com.davenet.notely.database.getDatabase
import com.davenet.notely.repository.NoteListRepository

class RescheduleAlarmsService : LifecycleService() {
    private lateinit var noteListRepository: NoteListRepository
    private lateinit var app: Application

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        app = requireNotNull(this.application)
        noteListRepository = NoteListRepository(getDatabase(app))
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