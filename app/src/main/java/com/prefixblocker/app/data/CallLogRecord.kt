package com.prefixblocker.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_logs")
data class CallLogRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rawNumber: String,        // เบอร์จริงที่โทรเข้า
    val matchedPrefix: String,    // Prefix ที่ตรวจพบ
    val timestamp: Long = System.currentTimeMillis()
)
