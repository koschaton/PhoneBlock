package com.prefixblocker.app

import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.lifecycleScope
import com.prefixblocker.app.data.AppDatabase
import com.prefixblocker.app.data.AppPreferences
import com.prefixblocker.app.data.BlockedPrefix
import com.prefixblocker.app.ui.CallLogsScreen
import com.prefixblocker.app.ui.HomeScreen
import com.prefixblocker.app.ui.PrefixListScreen
import com.prefixblocker.app.ui.theme.BgDark
import com.prefixblocker.app.ui.theme.CardDark
import com.prefixblocker.app.ui.theme.PrefixCallBlockerTheme
import com.prefixblocker.app.ui.theme.PrimaryBlue
import com.prefixblocker.app.ui.theme.TextMuted
import com.prefixblocker.app.ui.theme.TextPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "หน้าหลัก", Icons.Default.Dashboard)
    object Prefixes : Screen("prefixes", "รายการเบอร์", Icons.Default.FormatListNumbered)
    object Logs : Screen("logs", "ประวัติ", Icons.Default.History)
}

class MainActivity : ComponentActivity() {

    private lateinit var appDatabase: AppDatabase
    private lateinit var prefs: AppPreferences

    private var isRoleHeldState = mutableStateOf(false)

    // ตัวรับผลลัพธ์การขอสิทธิ์ Caller ID & Spam Role
    private val requestRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        checkRoleStatus()
        if (isRoleHeld()) {
            Toast.makeText(this, "เปิดใช้งานระบบคัดกรองสายเรียบร้อยแล้ว!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "กำลังเปิดหน้าตั้งค่าแอปเริ่มต้นเพื่อเลือกสิทธิ์...", Toast.LENGTH_SHORT).show()
            openDefaultAppsSettings()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appDatabase = AppDatabase.getInstance(this)
        prefs = AppPreferences(this)

        checkRoleStatus()

        // ขอสิทธิ์อัตโนมัติหากยังไม่ได้รับสิทธิ์
        if (!isRoleHeld()) {
            requestCallScreeningRole()
        }

        setContent {
            PrefixCallBlockerTheme {
                MainApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkRoleStatus()
    }

    private fun isRoleHeld(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            roleManager != null &&
                    roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) &&
                    roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        } else {
            false
        }
    }

    private fun checkRoleStatus() {
        isRoleHeldState.value = isRoleHeld()
    }

    private fun requestCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                    try {
                        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                        requestRoleLauncher.launch(intent)
                    } catch (e: Exception) {
                        openDefaultAppsSettings()
                    }
                }
            } else {
                openDefaultAppsSettings()
            }
        } else {
            Toast.makeText(this, "ต้องการระบบ Android 10 (API 29) ขึ้นไป", Toast.LENGTH_LONG).show()
        }
    }

    private fun openDefaultAppsSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
                startActivity(intent)
            } catch (ex: Exception) {
                Toast.makeText(this, "กรุณาไปที่ การตั้งค่า -> แอปเริ่มต้น -> การระบุผู้โทรและสแปม", Toast.LENGTH_LONG).show()
            }
        }
    }

    @Composable
    private fun MainApp() {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

        val prefixList by appDatabase.blockedPrefixDao().getAllFlow().collectAsState(initial = emptyList())
        val callLogs by appDatabase.callLogDao().getAllFlow().collectAsState(initial = emptyList())
        val totalBlockedCount by appDatabase.callLogDao().getCountFlow().collectAsState(initial = 0)

        val activePrefixes = remember(prefixList) {
            prefixList.filter { it.isEnabled }
        }

        Scaffold(
            containerColor = BgDark,
            bottomBar = {
                NavigationBar(
                    containerColor = CardDark
                ) {
                    val items = listOf(Screen.Home, Screen.Prefixes, Screen.Logs)
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentScreen == screen,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BgDark,
                                selectedTextColor = PrimaryBlue,
                                indicatorColor = PrimaryBlue,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            onClick = { currentScreen = screen }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = BgDark
            ) {
                when (currentScreen) {
                    Screen.Home -> HomeScreen(
                        isRoleHeld = isRoleHeldState.value,
                        onRequestRole = { requestCallScreeningRole() },
                        onOpenSettings = { openDefaultAppsSettings() },
                        totalBlockedCount = totalBlockedCount,
                        activePrefixes = activePrefixes,
                        prefs = prefs
                    )
                    Screen.Prefixes -> PrefixListScreen(
                        prefixList = prefixList,
                        onAddPrefix = { prefix, label ->
                            lifecycleScope.launch(Dispatchers.IO) {
                                appDatabase.blockedPrefixDao().insert(
                                    BlockedPrefix(prefix = prefix, label = label)
                                )
                            }
                        },
                        onTogglePrefix = { item ->
                            lifecycleScope.launch(Dispatchers.IO) {
                                appDatabase.blockedPrefixDao().update(
                                    item.copy(isEnabled = !item.isEnabled)
                                )
                            }
                        },
                        onDeletePrefix = { item ->
                            lifecycleScope.launch(Dispatchers.IO) {
                                appDatabase.blockedPrefixDao().delete(item)
                            }
                        }
                    )
                    Screen.Logs -> CallLogsScreen(
                        callLogs = callLogs,
                        onClearLogs = {
                            lifecycleScope.launch(Dispatchers.IO) {
                                appDatabase.callLogDao().clearAll()
                            }
                        }
                    )
                }
            }
        }
    }
}
