package com.prefixblocker.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {

    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllFlow(): Flow<List<CallLogRecord>>

    @Query("SELECT COUNT(*) FROM call_logs")
    fun getCountFlow(): Flow<Int>

    @Insert
    fun insertSync(record: CallLogRecord): Long

    @Query("DELETE FROM call_logs")
    suspend fun clearAll()
}
