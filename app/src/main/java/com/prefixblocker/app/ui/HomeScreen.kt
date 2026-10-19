package com.prefixblocker.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prefixblocker.app.data.AppPreferences
import com.prefixblocker.app.data.BlockedPrefix
import com.prefixblocker.app.ui.theme.*
import com.prefixblocker.app.util.PhoneUtil

@Composable
fun HomeScreen(
    isRoleHeld: Boolean,
    onRequestRole: () -> Unit,
    onOpenSettings: () -> Unit,
    totalBlockedCount: Int,
    activePrefixes: List<BlockedPrefix>,
    prefs: AppPreferences
) {
    var isEnabled by remember { mutableStateOf(prefs.isServiceEnabled) }
    var blockPrivate by remember { mutableStateOf(prefs.blockPrivateNumbers) }
    var hideSystemLog by remember { mutableStateOf(prefs.hideFromSystemCallLog) }

    // State สำหรับกล่องทดสอบเบอร์โทร
    var testNumberInput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // หัวข้อหน้า
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AccentRed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = AccentRed,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Prefix Call Blocker",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Text(
                    text = "ตัดสายอัตโนมัติ ไม่เด้งกวนใจตอนเล่นเกม",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        // การแจ้งเตือนสิทธิ์ RoleManager
        if (!isRoleHeld) {
            Card(
                colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WarningAmber.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ยังไม่ได้เปิดสิทธิ์ดักสาย (จำเป็น)",
                            fontWeight = FontWeight.Bold,
                            color = WarningAmber
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ระบบต้องการสิทธิ์ 'แอปเริ่มต้น: การระบุผู้โทรและสแปม' เพื่อให้สามารถตัดสายก่อนที่หน้าจอจะเด้งเตือนได้",
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onRequestRole,
                        colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("1. กดอนุญาตสิทธิ์ (เด้งหน้าต่าง)", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onOpenSettings,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber)
                    ) {
                        Text("2. เปิดหน้าตั้งค่าแอปเริ่มต้น (สำหรับ Xiaomi)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ระบบตัดสายทำงานสมบูรณ์ (โหมดตัดเงียบ)",
                        color = SuccessGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // กล่องสวิตช์เปิด/ปิดหลัก
        Card(
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "เปิดใช้งานระบบบล็อกสาย",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (isEnabled) "กำลังตรวจจับเบอร์ตามเลขนำหน้า" else "ระบบถูกปิดไว้ชั่วคราว",
                        color = if (isEnabled) SuccessGreen else TextMuted,
                        fontSize = 13.sp
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = {
                        isEnabled = it
                        prefs.isServiceEnabled = it
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SuccessGreen
                    )
                )
            }
        }

        // สถิติแสดงผลแบบสรุป
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ตัดสายไปแล้ว", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalBlockedCount สาย",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentRed
                    )
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Prefix ที่เฝ้าระวัง", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${activePrefixes.size} รายการ",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }
        }

        // กล่องจำลองทดสอบเบอร์โทร (Phone Number Simulator)
        Card(
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PhoneIphone, contentDescription = null, tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ทดสอบการทำงานของเบอร์โทร",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "พิมพ์เบอร์โทรลงไปเพื่อเช็คว่าแอปจะบล็อกหรือไม่ (รองรับทั้ง 08x และ +66)",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = testNumberInput,
                    onValueChange = { testNumberInput = it },
                    placeholder = { Text("เช่น 0800701234 หรือ +66800719999", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (testNumberInput.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val normalized = PhoneUtil.normalizeNumber(testNumberInput)
                    val matched = activePrefixes.firstOrNull { PhoneUtil.matchesPrefix(normalized, it.prefix) }

                    if (matched != null && isEnabled) {
                        Surface(
                            color = AccentRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CallEnd, contentDescription = null, tint = AccentRed)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "🚫 จะถูกตัดสายทันที!",
                                        color = AccentRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "ตรงกับ Prefix: ${matched.prefix} (${matched.label}) ไม่เด้งบังจอเกมแน่นอน",
                                        color = TextPrimary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    } else {
                        Surface(
                            color = SuccessGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = SuccessGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "✅ ปล่อยผ่านปกติ",
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (!isEnabled) "ระบบถูกปิดอยู่" else "ไม่ตรงกับรายการ Prefix ที่บล็อกไว้",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // การตั้งค่าเพิ่มเติม
        Card(
            colors = CardDefaults.cardColors(containerColor = CardDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ตัวเลือกเพิ่มเติม",
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("บล็อกเบอร์ไม่แสดงตัวตน", color = TextPrimary, fontSize = 14.sp)
                        Text("เช่น Private / Unknown Number", color = TextMuted, fontSize = 12.sp)
                    }
                    Switch(
                        checked = blockPrivate,
                        onCheckedChange = {
                            blockPrivate = it
                            prefs.blockPrivateNumbers = it
                        }
                    )
                }

                HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ซ่อนจากประวัติการโทรของเครื่อง", color = TextPrimary, fontSize = 14.sp)
                        Text("ไม่ให้บันทึกลง System Call Log", color = TextMuted, fontSize = 12.sp)
                    }
                    Switch(
                        checked = hideSystemLog,
                        onCheckedChange = {
                            hideSystemLog = it
                            prefs.hideFromSystemCallLog = it
                        }
                    )
                }
            }
        }
    }
}
