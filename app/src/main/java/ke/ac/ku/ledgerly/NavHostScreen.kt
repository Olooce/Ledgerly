package ke.ac.ku.ledgerly

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
import ke.ac.ku.ledgerly.feature.stats.StatsScreen
import ke.ac.ku.ledgerly.feature.transactionlist.TransactionListScreen
import ke.ac.ku.ledgerly.ui.theme.Zinc
import ke.ac.ku.ledgerly.utils.NavRouts

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavHostScreen(
    oneTapClient: SignInClient
) {
    val navController = rememberNavController()
    var bottomBarVisibility by remember {
        mutableStateOf(false)
    }
    
    Scaffold(bottomBar = {
        AnimatedVisibility(visible = bottomBarVisibility) {
            NavigationBottomBar(
                navController = navController,
                items = listOf(
                    NavItem(route = NavRouts.home, icon = R.drawable.ic_home),
                    NavItem(route = NavRouts.budget, icon = R.drawable.ic_budget),
                    NavItem(route = NavRouts.allTransactions, icon = R.drawable.ic_transaction),
                    NavItem(route = NavRouts.stats, icon = R.drawable.ic_stats)
                )
            )
        }
    }) {
        NavHost(
            navController = navController,
            startDestination = NavRouts.auth,
            modifier = Modifier.padding(it)
        ) {
            composable(route = NavRouts.auth) {
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
            
            composable(route = NavRouts.home) {
                bottomBarVisibility = true
                HomeScreen(navController)
            }

            composable(route = NavRouts.addIncome) {
                bottomBarVisibility = false
                AddTransaction(navController, isIncome = true)
            }
            
            composable(route = NavRouts.addExpense) {
                bottomBarVisibility = false
                AddTransaction(navController, isIncome = false)
            }

            composable(route = NavRouts.stats) {
                bottomBarVisibility = true
                StatsScreen(navController)
            }
            
            composable(route = NavRouts.allTransactions) {
                bottomBarVisibility = true
                TransactionListScreen(navController)
            }

            composable(route = NavRouts.budget) {
                bottomBarVisibility = true
                BudgetScreen(navController)
            }

            composable("add_budget") {
                AddBudgetScreen(navController = navController)
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
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    BottomAppBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedTextColor = Zinc,
                    selectedIconColor = Zinc,
                    unselectedTextColor = Color.Gray,
                    unselectedIconColor = Color.Gray
                )
            )
        }
    }
}


