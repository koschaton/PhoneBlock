package com.prefixblocker.app.util

object PhoneUtil {

    /**
     * แปลงเบอร์โทรศัพท์ให้อยู่ในฟอร์แมตมาตรฐาน
     * - ตัดอักขระพิเศษ เว้นวรรค ขีด (-) วงเล็บ
     * - แปลงรหัสประเทศ +66 ให้เป็น 0 (เช่น +66800701234 -> 0800701234)
     */
    fun normalizeNumber(rawNumber: String?): String {
        if (rawNumber.isNullOrBlank()) return ""

        var clean = rawNumber.trim().replace("[^0-9+]".toRegex(), "")

        // หากขึ้นต้นด้วย +66 ให้เปลี่ยนเป็น 0
        if (clean.startsWith("+66")) {
            clean = "0" + clean.substring(3)
        } else if (clean.startsWith("66") && clean.length >= 10) {
            clean = "0" + clean.substring(2)
        }

        return clean
    }

    /**
     * ตรวจสอบว่าเบอร์โทรเข้าตรงกับ Prefix หรือไม่
     */
    fun matchesPrefix(normalizedNumber: String, prefix: String): Boolean {
        val cleanPrefix = prefix.trim().replace("[^0-9]".toRegex(), "")
        if (cleanPrefix.isEmpty() || normalizedNumber.isEmpty()) return false
        return normalizedNumber.startsWith(cleanPrefix)
    }
}
