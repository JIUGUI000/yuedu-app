package com.legado.lite.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.legado.lite.data.dao.BookDao
import com.legado.lite.data.dao.BookSourceDao
import com.legado.lite.data.dao.ChapterDao
import com.legado.lite.data.dao.ReadHistoryDao
import com.legado.lite.data.dao.SearchHistoryDao
import com.legado.lite.data.entity.BookEntity
import com.legado.lite.data.entity.BookSourceEntity
import com.legado.lite.data.entity.ChapterEntity
import com.legado.lite.data.entity.ReadHistoryEntity
import com.legado.lite.data.entity.SearchHistoryEntity

@Database(
    entities = [
        BookSourceEntity::class,
        BookEntity::class,
        ChapterEntity::class,
        ReadHistoryEntity::class,
        SearchHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LegadoDatabase : RoomDatabase() {
    abstract fun bookSourceDao(): BookSourceDao
    abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun readHistoryDao(): ReadHistoryDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        @Volatile private var instance: LegadoDatabase? = null
        fun get(context: Context): LegadoDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                LegadoDatabase::class.java,
                "legado.db"
            )
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
    }
}
