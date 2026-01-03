package ke.ac.ku.ledgerly

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.identity.SignInClient
import ke.ac.ku.ledgerly.base.AuthEvent
import ke.ac.ku.ledgerly.data.constants.NavRouts
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.data.repository.UserPreferencesRepository
import ke.ac.ku.ledgerly.data.security.BiometricAuthenticationManager
import ke.ac.ku.ledgerly.domain.SessionTimeoutManager
import ke.ac.ku.ledgerly.presentation.add_transaction.AddTransaction
import ke.ac.ku.ledgerly.presentation.auth.AuthScreen
import ke.ac.ku.ledgerly.presentation.auth.AuthViewModel
import ke.ac.ku.ledgerly.presentation.auth.BiometricOptInScreen
import ke.ac.ku.ledgerly.presentation.auth.ReauthenticationScreen
import ke.ac.ku.ledgerly.presentation.budget.AddBudgetScreen
import ke.ac.ku.ledgerly.presentation.budget.BudgetScreen
import ke.ac.ku.ledgerly.presentation.categories.CategoryManagementScreen
import ke.ac.ku.ledgerly.presentation.home.HomeScreen
import ke.ac.ku.ledgerly.presentation.onboarding.OnboardingScreen
import ke.ac.ku.ledgerly.presentation.settings.SettingsScreen
import ke.ac.ku.ledgerly.presentation.settings.SettingsViewModel
import ke.ac.ku.ledgerly.presentation.stats.StatsScreen
import ke.ac.ku.ledgerly.presentation.transactions.TransactionViewModel
import ke.ac.ku.ledgerly.presentation.transactions.TransactionsScreen
import ke.ac.ku.ledgerly.ui.components.DrawerContent
import ke.ac.ku.ledgerly.ui.theme.ThemeViewModel
import ke.ac.ku.ledgerly.ui.theme.Zinc
import ke.ac.ku.ledgerly.ui.transitions.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavHostScreen(
    oneTapClient: SignInClient,
    themeViewModel: ThemeViewModel,
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel = hiltViewModel(),
    userPreferencesRepository: UserPreferencesRepository,
    sessionTimeoutManager: SessionTimeoutManager? = null,
    transactionViewModel: TransactionViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = LocalActivity.current as? FragmentActivity

    val authState by authViewModel.state.collectAsState()
    val transactionState by transactionViewModel.transactionsState.collectAsState()

    val biometricManager = remember { BiometricAuthenticationManager(context) }

    var showReauthScreen by remember { mutableStateOf(false) }
    var isReAuthLoading by remember { mutableStateOf(false) }

    LaunchedEffect(sessionTimeoutManager) {
        sessionTimeoutManager?.sessionTimeoutEvent?.collect {
            showReauthScreen = true
        }
    }
    LaunchedEffect(authState.isAuthenticated) {
        if (!authState.isAuthenticated) {
            showReauthScreen = false
            isReAuthLoading = false
        }
    }

    val startDestination = if (!authState.isAuthenticated) NavRouts.auth else NavRouts.auth

    var bottomBarVisible by remember { mutableStateOf(false) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showMenuButton = currentRoute in listOf(
        NavRouts.home,
        NavRouts.budget,
        NavRouts.allTransactions,
        NavRouts.stats
    )

    Box(modifier = Modifier.fillMaxSize()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = currentRoute != NavRouts.auth && !showReauthScreen,
            drawerContent = {
                ModalDrawerSheet {
                    DrawerContent(
                        navController = navController,
                        themeViewModel = themeViewModel,
                        onCloseDrawer = { scope.launch { drawerState.close() } },
                        authViewModel = authViewModel,
                        transactionData = transactionState.transactions.map { it as Any } as List<TransactionEntity>
                    )
                }
            }
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {},
                bottomBar = {
                    AnimatedVisibility(
                        visible = bottomBarVisible && !showReauthScreen,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) + fadeIn(),
                        exit = slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(300)
                        ) + fadeOut()
                    ) {
                        NavigationBottomBar(
                            navController = navController,
                            items = listOf(
                                NavItem(NavRouts.allTransactions, R.drawable.ic_transaction),
                                NavItem(NavRouts.budget, R.drawable.ic_budget),
                                NavItem(NavRouts.home, R.drawable.ic_home),
                                NavItem(NavRouts.stats, R.drawable.ic_stats),
                                NavItem(NavRouts.settings, R.drawable.ic_settings)
                            )
                        )
                    }
                }
            ) { contentPadding ->
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                ) {
                    composable(NavRouts.auth) {
                        bottomBarVisible = false

                        LaunchedEffect(authState.isAuthenticated, authState.isLoading) {
                            if (authState.isAuthenticated && !authState.isLoading) {
                                if (authState.showBiometricOptIn) {
                                    navController.navigate(NavRouts.biometricOptIn) {
                                        popUpTo(NavRouts.auth) { inclusive = true }
                                    }
                                } else {
                                    val isOnboardingComplete =
                                        userPreferencesRepository.onboardingCompleted.first()

                                    val destination = if (isOnboardingComplete) {
                                        NavRouts.home
                                    } else {
                                        NavRouts.onboarding
                                    }

                                    navController.navigate(destination) {
                                        popUpTo(NavRouts.auth) { inclusive = true }
                                    }
                                }
                            }
                        }

                        AuthScreen(
                            oneTapClient = oneTapClient,
                            viewModel = authViewModel,
                            onAuthSuccess = {}
                        )
                    }

                    composable(NavRouts.biometricOptIn) {
                        bottomBarVisible = false
                        BiometricOptInScreen(
                            onContinue = {
                                scope.launch {
                                    val isOnboardingComplete =
                                        userPreferencesRepository.onboardingCompleted.first()

                                    val destination = if (isOnboardingComplete) {
                                        NavRouts.home
                                    } else {
                                        NavRouts.onboarding
                                    }

                                    navController.navigate(destination) {
                                        popUpTo(NavRouts.biometricOptIn) { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    composable(NavRouts.onboarding) {
                        bottomBarVisible = false
                        OnboardingScreen(
                            onComplete = {
                                navController.navigate(NavRouts.home) {
                                    popUpTo(NavRouts.onboarding) { inclusive = true }
                                }
                            },
                            onExit = {
                                authViewModel.onEvent(AuthEvent.SignOut)
                                navController.navigate(NavRouts.auth) {
                                    popUpTo(NavRouts.onboarding) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(NavRouts.home) {
                        bottomBarVisible = true
                        HomeScreen(navController)
                    }

                    composable(NavRouts.addIncome) {
                        bottomBarVisible = false
                        AddTransaction(navController, isIncome = true)
                    }

                    composable(NavRouts.addExpense) {
                        bottomBarVisible = false
                        AddTransaction(navController, isIncome = false)
                    }

                    composable(NavRouts.stats) {
                        bottomBarVisible = true
                        StatsScreen(navController)
                    }

                    composable(NavRouts.allTransactions) {
                        bottomBarVisible = true
                        TransactionsScreen(navController)
                    }

                    composable(NavRouts.budget) {
                        bottomBarVisible = true
                        BudgetScreen(navController)
                    }

                    composable(NavRouts.addBudget) {
                        bottomBarVisible = false
                        AddBudgetScreen(navController)
                    }

                    composable(NavRouts.settings) {
                        bottomBarVisible = true
                        SettingsScreen(navController, themeViewModel, settingsViewModel)
                    }

                    composable(NavRouts.categoryManagement) {
                        bottomBarVisible = false
                        CategoryManagementScreen(navController)
                    }
                }
            }

            if (showMenuButton && !showReauthScreen) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .systemBarsPadding()
                        .padding(8.dp)
                        .zIndex(20f)
                ) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_menu),
                            contentDescription = "Menu"
                        )
                    }
                }
            }
        }

        // Reauthentication overlay
        if (showReauthScreen) {
            ReauthenticationScreen(
                isVisible = showReauthScreen,
                isBiometricAvailable = biometricManager.isBiometricAvailable(),
                isLoading = isReAuthLoading,
                userName = authViewModel.currentUserName,
                onPasswordSubmit = { password ->
                    isReAuthLoading = true
                    scope.launch {
                        try {
                            val email = authViewModel.currentUserEmail ?: ""
                            val result = authViewModel.repository.signInWithEmail(email, password)
                            if (result.isSuccess) {
                                sessionTimeoutManager?.recordUserActivity()
                                showReauthScreen = false
                                isReAuthLoading = false
                            } else {
                                isReAuthLoading = false
                            }
                        } catch (e: Exception) {
                            isReAuthLoading = false
                        }
                    }
                },
                onBiometricAuth = {
                    if (activity != null) {
                        isReAuthLoading = true
                        scope.launch {
                            val result = biometricManager.authenticate(
                                activity = activity,
                                title = "Unlock Ledgerly",
                                subtitle = "Verify your identity to continue",
                                negativeButtonText = "Use Password"
                            )

                            when (result) {
                                is BiometricAuthenticationManager.BiometricAuthResult.Success -> {
                                    sessionTimeoutManager?.recordUserActivity()
                                    showReauthScreen = false
                                    isReAuthLoading = false
                                }
                                is BiometricAuthenticationManager.BiometricAuthResult.Error -> {
                                    isReAuthLoading = false
                                                               }
                                is BiometricAuthenticationManager.BiometricAuthResult.Failed -> {
                                    isReAuthLoading = false
                                                               }
                                is BiometricAuthenticationManager.BiometricAuthResult.Fallback -> {
                                    isReAuthLoading = false
                                }
                            }
                        }
                    }
                },
                onSignOut = {
                    authViewModel.onEvent(AuthEvent.SignOut)
                    showReauthScreen = false
                    isReAuthLoading = false
                    navController.navigate(NavRouts.auth) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
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
    if (items.size != 5) return

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val leftItems = items.subList(0, 2)
    val centerItem = items[2]
    val rightItems = items.subList(3, 5)

    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme)
        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    else
        MaterialTheme.colorScheme.background.copy(alpha = 0.98f)

    val contentColor = if (isDarkTheme)
        MaterialTheme.colorScheme.onSurface
    else
        MaterialTheme.colorScheme.onBackground

    val barHeight = 64.dp

    Surface(
        color = backgroundColor,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(barHeight)
            .zIndex(10f)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
        ) {
            leftItems.forEach { item ->
                NavBarIcon(
                    item = item,
                    isSelected = currentRoute == item.route,
                    contentColor = contentColor,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            val isSelectedCenter = currentRoute == centerItem.route
            NavBarIcon(
                item = centerItem,
                isSelected = isSelectedCenter,
                contentColor = contentColor,
                onClick = {
                    navController.navigate(centerItem.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

            rightItems.forEach { item ->
                NavBarIcon(
                    item = item,
                    isSelected = currentRoute == item.route,
                    contentColor = contentColor,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun NavBarIcon(
    item: NavItem,
    isSelected: Boolean,
    contentColor: Color,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_scale"
    )

    val iconSize by animateDpAsState(
        targetValue = if (isSelected) 26.dp else 24.dp,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "icon_size"
    )

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
    ) {
        Icon(
            painter = painterResource(id = item.icon),
            contentDescription = item.route,
            modifier = Modifier.size(iconSize),
            tint = if (isSelected) Zinc else contentColor.copy(alpha = 0.6f)
        )
    }
}