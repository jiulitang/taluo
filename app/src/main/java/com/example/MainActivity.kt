package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.TarotAppViewModel
import com.example.ui.screens.CodexScreen
import com.example.ui.screens.DivinationScreen
import com.example.ui.screens.MentorChatScreen
import com.example.ui.screens.SanctuaryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AureliaTarotTheme
import com.example.ui.theme.LocalTarotColors

object TarotRoutes {
  const val SANCTUARY = "sanctuary"
  const val DIVINATION = "divination"
  const val MENTOR = "mentor"
  const val CODEX = "codex"
  const val SETTINGS = "settings"
}

private data class TarotNavItem(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
)

private val navItems =
  listOf(
    TarotNavItem(
      TarotRoutes.SANCTUARY,
      "圣殿",
      Icons.Rounded.NightsStay,
      Icons.Outlined.NightsStay,
    ),
    TarotNavItem(
      TarotRoutes.DIVINATION,
      "占卜",
      Icons.Rounded.AutoAwesome,
      Icons.Outlined.AutoAwesome,
    ),
    TarotNavItem(
      TarotRoutes.MENTOR,
      "导师",
      Icons.Rounded.Forum,
      Icons.Outlined.Forum,
    ),
    TarotNavItem(
      TarotRoutes.CODEX,
      "圣典",
      Icons.Rounded.MenuBook,
      Icons.Outlined.MenuBook,
    ),
    TarotNavItem(
      TarotRoutes.SETTINGS,
      "设置",
      Icons.Rounded.Settings,
      Icons.Outlined.Settings,
    ),
  )

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: TarotAppViewModel = viewModel()
      val apiSettings by viewModel.apiSettings.collectAsStateWithLifecycle()

      AureliaTarotTheme(themeMode = apiSettings.themeMode) {
        AureliaTarotApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun AureliaTarotApp(viewModel: TarotAppViewModel) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route ?: TarotRoutes.SANCTUARY
  val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val tarotColors = LocalTarotColors.current

  LaunchedEffect(toastMessage) {
    toastMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.clearToast()
    }
  }

  fun navigateToTab(route: String) {
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useNavigationRail = maxWidth >= 600.dp

    val backgroundBrush =
      Brush.verticalGradient(
        listOf(
          MaterialTheme.colorScheme.background,
          tarotColors.sanctuaryMist.copy(alpha = 0.25f),
          MaterialTheme.colorScheme.background,
        )
      )

    Scaffold(
      modifier = Modifier.fillMaxSize().background(backgroundBrush),
      containerColor = MaterialTheme.colorScheme.background,
      contentWindowInsets = WindowInsets.safeDrawing,
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      bottomBar = {
        if (!useNavigationRail) {
          NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            tonalElevation = 4.dp,
          ) {
            navItems.forEach { item ->
              val selected = currentRoute == item.route
              NavigationBarItem(
                selected = selected,
                onClick = { navigateToTab(item.route) },
                icon = {
                  Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.label,
                  )
                },
                label = { Text(item.label) },
                colors =
                  NavigationBarItemDefaults.colors(
                    selectedIconColor = tarotColors.goldAccent,
                    selectedTextColor = tarotColors.goldAccent,
                    indicatorColor = tarotColors.goldSoft,
                  ),
                modifier = Modifier.testTag("nav_${item.route}"),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        if (useNavigationRail) {
          NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
            navItems.forEach { item ->
              val selected = currentRoute == item.route
              NavigationRailItem(
                selected = selected,
                onClick = { navigateToTab(item.route) },
                icon = {
                  Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.label,
                  )
                },
                label = { Text(item.label) },
                modifier = Modifier.testTag("nav_${item.route}"),
              )
            }
          }
        }

        NavHost(
          navController = navController,
          startDestination = TarotRoutes.SANCTUARY,
          modifier = Modifier.weight(1f).fillMaxSize(),
        ) {
          composable(TarotRoutes.SANCTUARY) {
            SanctuaryScreen(
              viewModel = viewModel,
              onNavigateToDivination = { navigateToTab(TarotRoutes.DIVINATION) },
              onDiscussWithMentor = { record ->
                viewModel.attachReadingToChat(record)
                navigateToTab(TarotRoutes.MENTOR)
              },
              onOpenSettings = {
                viewModel.showToast("请先在「设置」中配置并保存 OpenAI 接口参数")
                navigateToTab(TarotRoutes.SETTINGS)
              },
            )
          }
          composable(TarotRoutes.DIVINATION) {
            DivinationScreen(
              viewModel = viewModel,
              onDiscussWithMentor = { record ->
                viewModel.attachReadingToChat(record)
                navigateToTab(TarotRoutes.MENTOR)
              },
              onOpenSettings = {
                viewModel.showToast("请先在「设置」中配置并保存 OpenAI 接口参数")
                navigateToTab(TarotRoutes.SETTINGS)
              },
            )
          }
          composable(TarotRoutes.MENTOR) {
            MentorChatScreen(
              viewModel = viewModel,
              onOpenSettings = {
                viewModel.showToast("请先在「设置」中配置并保存 OpenAI 接口参数")
                navigateToTab(TarotRoutes.SETTINGS)
              },
            )
          }
          composable(TarotRoutes.CODEX) {
            CodexScreen(
              viewModel = viewModel,
              onOpenSettings = {
                viewModel.showToast("请先在「设置」中配置并保存 OpenAI 接口参数")
                navigateToTab(TarotRoutes.SETTINGS)
              },
            )
          }
          composable(TarotRoutes.SETTINGS) {
            SettingsScreen(viewModel = viewModel)
          }
        }
      }
    }
  }
}
