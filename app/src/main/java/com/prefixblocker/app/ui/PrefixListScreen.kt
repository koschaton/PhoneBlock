package com.prefixblocker.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prefixblocker.app.data.BlockedPrefix
import com.prefixblocker.app.ui.theme.*

@Composable
fun PrefixListScreen(
    prefixList: List<BlockedPrefix>,
    onAddPrefix: (prefix: String, label: String) -> Unit,
    onTogglePrefix: (BlockedPrefix) -> Unit,
    onDeletePrefix: (BlockedPrefix) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BgDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AccentRed,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "เพิ่ม Prefix")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FormatListNumbered,
                        contentDescription = null,
                        tint = PrimaryBlue
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "รายการเลขนำหน้าที่บล็อก",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "ทั้งหมด ${prefixList.size} รายการ (เปิดใช้งาน ${prefixList.count { it.isEnabled }})",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            if (prefixList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ยังไม่มีรายการ Prefix\nกดปุ่ม + ด้านล่างเพื่อเพิ่ม",
                        color = TextMuted,
                        fontSize = 15.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(prefixList, key = { it.id }) { item ->
                        PrefixItemCard(
                            item = item,
                            onToggle = { onTogglePrefix(item) },
                            onDelete = { onDeletePrefix(item) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPrefixDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { prefix, label ->
                onAddPrefix(prefix, label)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PrefixItemCard(
    item: BlockedPrefix,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (item.isEnabled) PrimaryBlue.copy(alpha = 0.3f) else CardBorder,
                RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (item.isEnabled) AccentRed.copy(alpha = 0.18f) else CardBorder,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${item.prefix}xxxx",
                            color = if (item.isEnabled) AccentRed else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.label.ifBlank { "ไม่ระบุชื่อ" },
                    color = if (item.isEnabled) TextPrimary else TextMuted,
                    fontSize = 13.sp
                )
            }

            // สวิตช์เปิด/ปิดรายตัว
            Switch(
                checked = item.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AccentRed
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // ปุ่มลบ
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "ลบรายการ",
                    tint = TextMuted
                )
            }
        }
    }
}

@Composable
fun AddPrefixDialog(
    onDismiss: () -> Unit,
    onConfirm: (prefix: String, label: String) -> Unit
) {
    var prefixInput by remember { mutableStateOf("") }
    var labelInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        title = {
            Text("เพิ่มเลขนำหน้า (Prefix)", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "ระบุตัวเลขนำหน้า เช่น 080070 หรือ 061272 (ระบบจะตัดสายทุกเบอร์ที่ขึ้นต้นด้วยเลขนี้)",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                OutlinedTextField(
                    value = prefixInput,
                    onValueChange = {
                        // กรองรับเฉพาะตัวเลข
                        val cleaned = it.replace("[^0-9]".toRegex(), "")
                        if (cleaned.length <= 10) {
                            prefixInput = cleaned
                            isError = false
                        }
                    },
                    label = { Text("เลขนำหน้า (เช่น 061272)") },
                    placeholder = { Text("061272") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isError,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text("กรุณาระบุเลขนำหน้าอย่างน้อย 3 ตัวขึ้นไป", color = AccentRed, fontSize = 11.sp)
                }

                OutlinedTextField(
                    value = labelInput,
                    onValueChange = { labelInput = it },
                    label = { Text("บันทึกช่วยจำ (เช่น เบอร์สแปม)") },
                    placeholder = { Text("เบอร์มิจฉาชีพ") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (prefixInput.length < 3) {
                        isError = true
                    } else {
                        val finalLabel = labelInput.ifBlank { "เบอร์สแปม ($prefixInput)" }
                        onConfirm(prefixInput, finalLabel)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
            ) {
                Text("บันทึก", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ยกเลิก", color = TextSecondary)
            }
        }
    )
}
