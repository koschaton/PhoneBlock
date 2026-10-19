package com.prefixblocker.app.service

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.annotation.RequiresApi
import com.prefixblocker.app.data.AppDatabase
import com.prefixblocker.app.data.AppPreferences
import com.prefixblocker.app.data.CallLogRecord
import com.prefixblocker.app.util.PhoneUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Service หัวใจหลักที่ดักจับสายเรียกเข้าก่อนเครื่องจะสั่นหรือเด้งหน้าต่างสายเข้า
 * ต้องได้รับสิทธิ์ ROLE_CALL_SCREENING จากผู้ใช้ก่อนทำงาน
 */
@RequiresApi(Build.VERSION_CODES.Q)
class CallBlockerScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val prefs = AppPreferences(applicationContext)

        // 1. ถ้าผู้ใช้ปิดระบบหลักในแอป ให้ปล่อยผ่านทันที
        if (!prefs.isServiceEnabled) {
            allowCall(callDetails)
            return
        }

        val handle = callDetails.handle
        val rawNumber = handle?.schemeSpecificPart ?: ""

        // 2. กรณีเป็นเบอร์ไม่ระบุตัวตน (Private/Unknown Number)
        if (rawNumber.isBlank()) {
            if (prefs.blockPrivateNumbers) {
                blockCall(
                    callDetails = callDetails,
                    rawNumber = "เบอร์ไม่แสดงตัวตน",
                    matchedPrefix = "PRIVATE_NUMBER",
                    hideFromSystemCallLog = prefs.hideFromSystemCallLog
                )
            } else {
                allowCall(callDetails)
            }
            return
        }

        // 3. แปลงเบอร์ให้อยู่ในมาตรฐานเดียวกัน (ตัดขีด ช่องว่าง แปลง +66 เป็น 0)
        val normalizedNumber = PhoneUtil.normalizeNumber(rawNumber)

        // 4. ตรวจสอบกับรายการ Prefix ที่เปิดใช้งานในฐานข้อมูล
        val db = AppDatabase.getInstance(applicationContext)
        val activePrefixes = db.blockedPrefixDao().getActivePrefixesSync()

        val matchedPrefix = activePrefixes.firstOrNull { item ->
            PhoneUtil.matchesPrefix(normalizedNumber, item.prefix)
        }

        if (matchedPrefix != null) {
            // เบอร์ตรงกับลิสต์ที่ต้องการบล็อก (เช่น 080070, 080071, 063863)
            blockCall(
                callDetails = callDetails,
                rawNumber = rawNumber,
                matchedPrefix = matchedPrefix.prefix,
                hideFromSystemCallLog = prefs.hideFromSystemCallLog
            )
        } else {
            // ไม่ตรง ปล่อยให้สายเรียกเข้าตามปกติ
            allowCall(callDetails)
        }
    }

    private fun blockCall(
        callDetails: Call.Details,
        rawNumber: String,
        matchedPrefix: String,
        hideFromSystemCallLog: Boolean
    ) {
        val response = CallResponse.Builder()
            .setDisallowCall(true)        // ปฏิเสธการรับสาย
            .setRejectCall(true)          // สั่งตัดสายทิ้งทันที
            .setSkipNotification(true)    // ไม่ให้เด้ง Pop-up หรือ Notification กวนใจขณะเล่นเกม
            .setSkipCallLog(hideFromSystemCallLog) // เลือกว่าจะซ่อนจาก Call Log หลักของระบบหรือไม่
            .build()

        respondToCall(callDetails, response)

        // บันทึกลงในฐานข้อมูลประวัติของแอปแบบ Asynchronous
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                db.callLogDao().insertSync(
                    CallLogRecord(
                        rawNumber = rawNumber,
                        matchedPrefix = matchedPrefix
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun allowCall(callDetails: Call.Details) {
        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSkipNotification(false)
            .build()
        respondToCall(callDetails, response)
    }
}
