package com.example.academicleveling

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.academicleveling.data.AppState
import com.example.academicleveling.ui.auth.LoginScreen
import com.example.academicleveling.ui.auth.SignupScreen
import com.example.academicleveling.ui.dungeon.DungeonStartTarget
import com.example.academicleveling.ui.dungeon.DungeonScreen
import com.example.academicleveling.ui.inventory.InventoryScreen
import com.example.academicleveling.ui.profile.ProfileScreen
import com.example.academicleveling.ui.quests.QuestsScreen
import com.example.academicleveling.ui.shared.SoundManager
import com.example.academicleveling.ui.timer.TimerScreen
import com.example.academicleveling.ui.theme.*
import androidx.compose.ui.res.vectorResource

private enum class Screen {
    LOGIN, SIGNUP, QUESTS, BAZAAR, DUNGEON, TIMER, PROFILE, RESET_PASSWORD
}

private val MAIN_SCREENS = setOf(
    Screen.QUESTS, Screen.BAZAAR, Screen.DUNGEON, Screen.TIMER, Screen.PROFILE
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        AppState.init(this)

        val data: android.net.Uri? = intent?.data
        var token: String? = null
        var email: String? = null

        if (data != null && data.scheme == "academicleveling" && data.host == "reset-password") {
            token = data.getQueryParameter("token")
            email = data.getQueryParameter("email")
        }

        setContent {
            AcademicLevelingTheme {
                AcademicLevelingApp(deepLinkToken = token, deepLinkEmail = email)
            }
        }
    }
}

@Composable
private fun AcademicLevelingApp(
    deepLinkToken: String? = null,
    deepLinkEmail: String? = null
) {
    var currentScreen by remember {
        mutableStateOf(
            if (deepLinkToken != null && deepLinkEmail != null) Screen.RESET_PASSWORD
            else if (AppState.loggedIn) Screen.QUESTS
            else Screen.LOGIN
        )
    }
    var dungeonStartTarget by remember { mutableStateOf(DungeonStartTarget.HUB) }

    if (currentScreen in MAIN_SCREENS) {
        Scaffold(
            containerColor = BgPrimary,
            bottomBar = {
                BottomNavBar(selected = currentScreen) { screen ->
                    SoundManager.navigate()
                    if (screen != Screen.DUNGEON) {
                        dungeonStartTarget = DungeonStartTarget.HUB
                    }
                    currentScreen = screen
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                when (currentScreen) {
                    Screen.QUESTS  -> QuestsScreen(
                        onOpenCommunity = {
                            dungeonStartTarget = DungeonStartTarget.COMMUNITY
                            currentScreen = Screen.DUNGEON
                        },
                        onEnterCode = {
                            dungeonStartTarget = DungeonStartTarget.CODE
                            currentScreen = Screen.DUNGEON
                        },
                        onOpenMyQuizzes = {
                            dungeonStartTarget = DungeonStartTarget.MY
                            currentScreen = Screen.DUNGEON
                        },
                        onOpenHistory = {
                            dungeonStartTarget = DungeonStartTarget.HISTORY
                            currentScreen = Screen.DUNGEON
                        },
                        onOpenShop = { currentScreen = Screen.BAZAAR },
                        onOpenProfile = { currentScreen = Screen.PROFILE }
                    )
                    Screen.BAZAAR  -> InventoryScreen()
                    Screen.DUNGEON -> DungeonScreen(
                        startTarget = dungeonStartTarget,
                        onStartTargetConsumed = { dungeonStartTarget = DungeonStartTarget.HUB }
                    )
                    Screen.TIMER   -> TimerScreen()
                    Screen.PROFILE -> ProfileScreen(
                        onLogout = { AppState.logout(); currentScreen = Screen.LOGIN }
                    )
                    else -> QuestsScreen()
                }
            }
        }
    } else {
        when (currentScreen) {
            Screen.LOGIN  -> LoginScreen(
                onLogin  = { currentScreen = Screen.QUESTS },
                onSignup = { currentScreen = Screen.SIGNUP }
            )
            Screen.SIGNUP -> SignupScreen(
                onSignup = { currentScreen = Screen.QUESTS },
                onLogin  = { currentScreen = Screen.LOGIN }
            )
            Screen.RESET_PASSWORD -> com.example.academicleveling.ui.auth.ResetPasswordScreen(
                token = deepLinkToken ?: "",
                email = deepLinkEmail ?: "",
                onSuccess = { currentScreen = Screen.LOGIN }
            )
            else -> LoginScreen(
                onLogin  = { currentScreen = Screen.QUESTS },
                onSignup = { currentScreen = Screen.SIGNUP }
            )
        }
    }
}

private data class NavItem(
    val screen: Screen,
    val icon:   Any,
    val label:  String
)

private val NAV_ITEMS = listOf(
    NavItem(Screen.QUESTS,  Icons.Default.Home, "Home"),
    NavItem(Screen.BAZAAR,  Icons.Default.Storefront, "Shop"),
    NavItem(Screen.DUNGEON, Icons.Default.Quiz, "Quizzes"),
    NavItem(Screen.TIMER,   Icons.Default.Timer, "Timer"),
    NavItem(Screen.PROFILE, Icons.Default.Person, "Profile")
)

@Composable
fun NavigationIcon(icon: Any, contentDescription: String, tint: Color, modifier: Modifier) {
    when (icon) {
        is ImageVector -> {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = modifier
            )
        }
        is Int -> {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = contentDescription,
                tint = Color.Unspecified,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun BottomNavBar(selected: Screen, onSelect: (Screen) -> Unit) {
    Box(
        Modifier.fillMaxWidth()
            .background(Color(0xFF0D0D1A))
            .navigationBarsPadding()
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().height(62.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            NAV_ITEMS.forEach { item ->
                val isActive = selected == item.screen
                Column(
                    modifier            = Modifier.weight(1f).fillMaxHeight()
                        .clickable { onSelect(item.screen) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    if (isActive) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            NavigationIcon( // Ginamit ang helper function
                                icon = item.icon,
                                contentDescription = item.label,
                                tint = Teal,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                item.label, fontSize = 8.sp,
                                color = Teal, fontWeight = FontWeight.ExtraBold
                            )
                        }
                    } else {
                        NavigationIcon( // Ginamit ang helper function
                            icon = item.icon,
                            contentDescription = item.label,
                            tint = Color.White.copy(.45f),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(item.label, fontSize = 8.sp, color = Color.White.copy(.4f))
                    }
                    }
                }
            }
        }
    }

