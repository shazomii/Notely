package com.davenet.notely.database

import android.content.Context
import androidx.room.*

/**
 * Database for the offline cache
 */
@Database(entities = [DatabaseNote::class], version = 1)
abstract class NotesDatabase: RoomDatabase() {
    abstract val noteDao: NoteDao
}

/**
 * Singleton INSTANCE to prevent having multiple instances of
 * the same database opened at the same time.
 */
private lateinit var INSTANCE: NotesDatabase

/**
 * Method to initialize and return the instance if it is not
 * already initialized
 */
fun getDatabase(context: Context): NotesDatabase {
    synchronized(NotesDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                NotesDatabase::class.java,
                "notes").build()
        }
    }
    return INSTANCE
}