package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.SettingsCell
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.SettingsCell
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.screens.BlasterScreen
import com.example.ui.screens.CarrierScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.TemplatesScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.viewmodel.SmsBlastViewModel

enum class NavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    BLASTER("Blaster", Icons.Filled.ElectricBolt, Icons.Outlined.ElectricBolt, "tab_blaster"),
    TEMPLATES("Templates", Icons.Filled.ListAlt, Icons.Outlined.ListAlt, "tab_templates"),
    HISTORY("History", Icons.Filled.History, Icons.Outlined.History, "tab_history"),
    CARRIER("Carrier", Icons.Filled.SettingsCell, Icons.Outlined.SettingsCell, "tab_carrier")
}

class MainActivity : ComponentActivity() {

    private val viewModel: SmsBlastViewModel by viewModels {
        SmsBlastViewModel.Factory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: SmsBlastViewModel) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    var hasSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                viewModel.getApplication(),
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasContactsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                viewModel.getApplication(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasSmsPermission = isGranted
        if (isGranted) {
            viewModel.toggleSimulationMode(false)
        }
    }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasContactsPermission = isGranted
        if (isGranted) {
            viewModel.loadDeviceContacts()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SMS BLAST PRO",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = ElectricCyan
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = ElectricCyan
                ),
                modifier = Modifier.border(0.5.dp, DarkSurfaceBorder)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(0.5.dp, DarkSurfaceBorder)
                    .navigationBarsPadding()
            ) {
                NavigationTab.entries.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricCyan,
                            unselectedIconColor = TextSecondaryDark,
                            selectedTextColor = ElectricCyan,
                            unselectedTextColor = TextSecondaryDark,
                            indicatorColor = CyberCyan.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        },
        containerColor = DarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (NavigationTab.entries[selectedTab]) {
                NavigationTab.BLASTER -> BlasterScreen(
                    viewModel = viewModel,
                    hasSmsPermission = hasSmsPermission,
                    onRequestSmsPermission = {
                        smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                    }
                )
                NavigationTab.TEMPLATES -> TemplatesScreen(
                    viewModel = viewModel,
                    onTemplateLoaded = {
                        selectedTab = NavigationTab.BLASTER.ordinal
                    }
                )
                NavigationTab.HISTORY -> HistoryScreen(
                    viewModel = viewModel,
                    onResendToBlaster = {
                        selectedTab = NavigationTab.BLASTER.ordinal
                    }
                )
                NavigationTab.CARRIER -> CarrierScreen(
                    hasSmsPermission = hasSmsPermission,
                    hasContactsPermission = hasContactsPermission,
                    onRequestSmsPermission = {
                        smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                    },
                    onRequestContactsPermission = {
                        contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }
                )
            }
        }
    }
}
