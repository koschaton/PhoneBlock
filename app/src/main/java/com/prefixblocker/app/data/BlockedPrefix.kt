package com.prefixblocker.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "blocked_prefixes",
    indices = [Index(value = ["prefix"], unique = true)]
)
data class BlockedPrefix(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val prefix: String,           // เช่น "080070", "080071", "063863"
    val label: String,            // ชื่อกำกับ เช่น "แก๊งสแปม 080070"
    val isEnabled: Boolean = true, // เปิดหรือปิดการบล็อกของ Prefix นี้
    val createdAt: Long = System.currentTimeMillis()
)
