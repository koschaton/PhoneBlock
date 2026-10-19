package com.prefixblocker.app

import android.app.Application
import com.prefixblocker.app.data.AppDatabase
import com.prefixblocker.app.data.BlockedPrefix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PrefixBlockerApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // เตรียมความพร้อมของฐานข้อมูลและตรวจสอบว่ามี Prefix 3 ตัวตั้งต้นครบถ้วนหรือไม่
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(this@PrefixBlockerApp)
            val dao = db.blockedPrefixDao()
            val defaultList = listOf(
                BlockedPrefix(prefix = "080070", label = "เบอร์มิจฉาชีพ (080070)"),
                BlockedPrefix(prefix = "080071", label = "เบอร์มิจฉาชีพ (080071)"),
                BlockedPrefix(prefix = "063863", label = "เบอร์มิจฉาชีพ (063863)"),
                BlockedPrefix(prefix = "061272", label = "เบอร์มิจฉาชีพ (061272)"),
                BlockedPrefix(prefix = "065349", label = "เบอร์มิจฉาชีพ (065349)")
            )
            dao.insertAll(defaultList)
        }
    }
}
