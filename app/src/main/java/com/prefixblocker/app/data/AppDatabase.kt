package com.prefixblocker.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [BlockedPrefix::class, CallLogRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun blockedPrefixDao(): BlockedPrefixDao
    abstract fun callLogDao(): CallLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prefix_call_blocker.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // บันทึกเบอร์เริ่มต้น 3 รายการตามที่ผู้ใช้กำหนดทันทีเมื่อเปิดใช้งานครั้งแรก
                        CoroutineScope(Dispatchers.IO).launch {
                            val defaultPrefixes = listOf(
                                BlockedPrefix(prefix = "080070", label = "เบอร์มิจฉาชีพ/สแปม (080070)"),
                                BlockedPrefix(prefix = "080071", label = "เบอร์มิจฉาชีพ/สแปม (080071)"),
                                BlockedPrefix(prefix = "063863", label = "เบอร์มิจฉาชีพ/สแปม (063863)"),
                                BlockedPrefix(prefix = "061272", label = "เบอร์มิจฉาชีพ/สแปม (061272)"),
                                BlockedPrefix(prefix = "065349", label = "เบอร์มิจฉาชีพ/สแปม (065349)")
                            )
                            getInstance(context).blockedPrefixDao().insertAll(defaultPrefixes)
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
