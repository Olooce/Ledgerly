package ke.ac.ku.ledgerly

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.identity.SignInClient
import ke.ac.ku.ledgerly.auth.presentation.AuthScreen
import ke.ac.ku.ledgerly.feature.add_transaction.AddTransaction
import ke.ac.ku.ledgerly.feature.budget.AddBudgetScreen
import ke.ac.ku.ledgerly.feature.budget.BudgetScreen
import ke.ac.ku.ledgerly.feature.home.HomeScreen
import ke.ac.ku.ledgerly.feature.settings.SettingsScreen
import ke.ac.ku.ledgerly.feature.stats.StatsScreen
import ke.ac.ku.ledgerly.feature.transactionlist.TransactionListScreen
import ke.ac.ku.ledgerly.ui.components.DrawerContent
import ke.ac.ku.ledgerly.ui.theme.ThemeViewModel
import ke.ac.ku.ledgerly.ui.theme.Zinc
import ke.ac.ku.ledgerly.utils.NavRouts
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavHostScreen(
    oneTapClient: SignInClient,
    themeViewModel: ThemeViewModel
) {
    val navController = rememberNavController()
    var bottomBarVisibility by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRouts.home
    val showMenuButton = currentRoute == NavRouts.home

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute != NavRouts.auth,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    navController = navController,
                    themeViewModel = themeViewModel,
                    onCloseDrawer = { scope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Box {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {},
                bottomBar = {
                    AnimatedVisibility(visible = bottomBarVisibility) {
                        NavigationBottomBar(
                            navController = navController,
                            items = listOf(
                                NavItem(NavRouts.home, R.drawable.ic_home),
                                NavItem(NavRouts.budget, R.drawable.ic_budget),
                                NavItem(NavRouts.allTransactions, R.drawable.ic_transaction),
                                NavItem(NavRouts.stats, R.drawable.ic_stats)
                            )
                        )
                    }
                }
            ) { padding ->
                NavHost(
                    navController = navController,
                    startDestination = NavRouts.auth,
                    modifier = Modifier.padding(padding)
                ) {
                    composable(NavRouts.auth) {
                        bottomBarVisibility = false
                        AuthScreen(
                            oneTapClient = oneTapClient,
                            onAuthSuccess = {
                                navController.navigate(NavRouts.home) {
                                    popUpTo(NavRouts.auth) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(NavRouts.home) {
                        bottomBarVisibility = true
                        HomeScreen(navController)
                    }

                    composable(NavRouts.addIncome) {
                        bottomBarVisibility = false
                        AddTransaction(navController, isIncome = true)
                    }

                    composable(NavRouts.addExpense) {
                        bottomBarVisibility = false
                        AddTransaction(navController, isIncome = false)
                    }

                    composable(NavRouts.stats) {
                        bottomBarVisibility = true
                        StatsScreen(navController)
                    }

                    composable(NavRouts.allTransactions) {
                        bottomBarVisibility = true
                        TransactionListScreen(navController)
                    }

                    composable(NavRouts.budget) {
                        bottomBarVisibility = true
                        BudgetScreen(navController)
                    }

                    composable(NavRouts.addBudget) {
                        bottomBarVisibility = false
                        AddBudgetScreen(navController)
                    }

                    composable(NavRouts.settings) {
                        bottomBarVisibility = false
                        SettingsScreen(navController, themeViewModel)
                    }
                }
            }

            // Floating top bar overlay
            if (showMenuButton) {
                CenterAlignedTopAppBar(
                    title = {},
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_menu),
                                contentDescription = "Menu"
                            )
                        }
                    },
                    modifier = Modifier
                        .zIndex(10f),
                CenterAlignedTopAppBar(
                    title = {},
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_menu),
                                contentDescription = "Menu"
                            )
                        }
                    },
                    modifier = Modifier
                        .zIndex(10f),
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                )
            }

        }
    }
}


data class NavItem(
    val route: String,
    val icon: Int
)

@Composable
fun NavigationBottomBar(
    navController: NavController,
    items: List<NavItem>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()

    val backgroundColor = if (isDarkTheme)
        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    else
        MaterialTheme.colorScheme.background.copy(alpha = 0.95f)

    val contentColor = if (isDarkTheme)
        MaterialTheme.colorScheme.onSurface
    else
        MaterialTheme.colorScheme.onBackground

    androidx.compose.material3.NavigationBar(
        containerColor = backgroundColor,
        tonalElevation = 6.dp,
        modifier = Modifier
            .padding(horizontal = 32.dp, vertical = 12.dp)
            .height(56.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
            .zIndex(10f)
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.route,
                        modifier = Modifier.size(if (selected) 28.dp else 24.dp),
                        tint = if (selected) Zinc else contentColor.copy(alpha = 0.6f)
                    )
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = Zinc,
                    unselectedIconColor = contentColor.copy(alpha = 0.6f)
                )
            )
        }
    }
}



