package com.prefixblocker.app.service

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
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
        try {
            val prefs = AppPreferences(applicationContext)

            // 1. ถ้าผู้ใช้ปิดระบบหลักในแอป ให้ปล่อยผ่านทันที
            if (!prefs.isServiceEnabled) {
                Log.d(TAG, "Service is disabled by user, allowing call")
                allowCall(callDetails)
                return
            }

            // ดึงเบอร์จาก handle หรือ gatewayInfo
            val handle = callDetails.handle
            var rawNumber = handle?.schemeSpecificPart ?: ""
            if (rawNumber.isBlank()) {
                rawNumber = callDetails.gatewayInfo?.originalAddress?.schemeSpecificPart ?: ""
            }

            Log.i(TAG, "Incoming call rawNumber: '$rawNumber'")

            // 2. กรณีเป็นเบอร์ไม่ระบุตัวตน (Private/Unknown Number)
            if (rawNumber.isBlank()) {
                if (prefs.blockPrivateNumbers) {
                    Log.i(TAG, "Blocking private/unknown number")
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
            Log.i(TAG, "Normalized incoming number: '$normalizedNumber'")

            // 4. ดึงรายการ Prefix: อ่านจากแคชใน SharedPreferences ก่อน (เร็ว < 1ms)
            //    และดึงจาก Room DB เสริม
            val cachedPrefixes = prefs.getCachedPrefixes()
            val matchedFromCache = cachedPrefixes.firstOrNull { prefix ->
                PhoneUtil.matchesPrefix(normalizedNumber, prefix)
            }

            if (matchedFromCache != null) {
                Log.i(TAG, "MATCHED from cache: '$matchedFromCache' -> Blocking call immediately!")
                blockCall(
                    callDetails = callDetails,
                    rawNumber = rawNumber,
                    matchedPrefix = matchedFromCache,
                    hideFromSystemCallLog = prefs.hideFromSystemCallLog
                )
                return
            }

            // ลองเช็คจาก DB อีกชั้นเผื่อมีการเพิ่มเข้ามาใหม่
            val db = AppDatabase.getInstance(applicationContext)
            val activePrefixes = db.blockedPrefixDao().getActivePrefixesSync()

            val matchedFromDb = activePrefixes.firstOrNull { item ->
                PhoneUtil.matchesPrefix(normalizedNumber, item.prefix)
            }

            if (matchedFromDb != null) {
                Log.i(TAG, "MATCHED from DB: '${matchedFromDb.prefix}' -> Blocking call immediately!")
                blockCall(
                    callDetails = callDetails,
                    rawNumber = rawNumber,
                    matchedPrefix = matchedFromDb.prefix,
                    hideFromSystemCallLog = prefs.hideFromSystemCallLog
                )
            } else {
                Log.i(TAG, "No prefix match found for '$normalizedNumber', allowing call")
                allowCall(callDetails)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error in onScreenCall, allowing call to prevent dialer hang", t)
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
        Log.i(TAG, "Call rejected successfully for $rawNumber")

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
                Log.e(TAG, "Failed to insert call log", e)
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

    companion object {
        private const val TAG = "CallBlockerService"
    }
}
