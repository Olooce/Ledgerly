package ke.ac.ku.ledgerly.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ke.ac.ku.ledgerly.R
import ke.ac.ku.ledgerly.base.HomeNavigationEvent
import ke.ac.ku.ledgerly.base.NavigationEvent
import ke.ac.ku.ledgerly.data.constants.NavRouts
import ke.ac.ku.ledgerly.ui.components.TransactionList
import ke.ac.ku.ledgerly.ui.theme.Typography
import ke.ac.ku.ledgerly.ui.widget.MultiFloatingActionButton
import ke.ac.ku.ledgerly.ui.widget.TransactionTextView
import ke.ac.ku.ledgerly.utils.Utils

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    val homeState by viewModel.homeState.collectAsState()
    val userName by viewModel.userName.collectAsState(initial = "User")

    val lazyListState = rememberLazyListState()

    // Auto-load more when near the end
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo }
            .collect { layoutInfo ->
                val shouldLoadMore =
                    layoutInfo.visibleItemsInfo.lastOrNull()?.index?.let { lastVisibleIndex ->
                        lastVisibleIndex >= layoutInfo.totalItemsCount - 3
                    } ?: false

                if (shouldLoadMore && homeState.paginationState.hasNext && !homeState.paginationState.isLoading) {
                    viewModel.loadTransactions()
                }
            }
    }

    // Clear transactions when leaving screen
//    DisposableEffect(Unit) {
//        onDispose {
//            viewModel.clearTransactions()
//        }
//    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                NavigationEvent.NavigateBack -> navController.popBackStack()
                HomeNavigationEvent.NavigateToSeeAll -> {
                    navController.navigate(NavRouts.allTransactions)
                }

                HomeNavigationEvent.NavigateToAddIncome -> navController.navigate(NavRouts.addIncome)
                HomeNavigationEvent.NavigateToAddExpense -> navController.navigate(NavRouts.addExpense)
                else -> {}
            }
        }
    }

    val greeting = remember { Utils.getTimeBasedGreeting() }

    Surface(modifier = Modifier.fillMaxSize()) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (nameRow, list, card, topBar, add) = createRefs()

            Image(
                painter = painterResource(id = R.drawable.ic_topbar),
                contentDescription = null,
                modifier = Modifier.constrainAs(topBar) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 64.dp, start = 16.dp, end = 16.dp)
                    .constrainAs(nameRow) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_user),
                            contentDescription = "Profile",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        TransactionTextView(
                            text = greeting,
                            style = Typography.bodyMedium,
                            color = Color.White
                        )
                        TransactionTextView(
                            text = userName,
                            style = Typography.titleLarge,
                            color = Color.White
                        )
                    }
                }

                Image(
                    painter = painterResource(id = R.drawable.ic_notification),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(26.dp)
                        .clickable { },
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }

            CardItem(
                modifier = Modifier.constrainAs(card) {
                    top.linkTo(nameRow.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                balance = homeState.balance,
                income = homeState.totalIncome,
                expense = homeState.totalExpense,
                currentMonth = homeState.currentMonth,
                isNegative = homeState.isBalanceNegative
            )

            TransactionList(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(list) {
                        top.linkTo(card.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        height = Dimension.fillToConstraints
                    },
                list = homeState.transactions,
                onSeeAllClicked = {
                    viewModel.onEvent(HomeUiEvent.OnSeeAllClicked)
                },
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .constrainAs(add) {
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    },
                contentAlignment = Alignment.BottomEnd
            ) {
                MultiFloatingActionButton(
                    modifier = Modifier,
                    onAddExpenseClicked = {
                        viewModel.onEvent(HomeUiEvent.OnAddExpenseClicked)
                    },
                    onAddIncomeClicked = {
                        viewModel.onEvent(HomeUiEvent.OnAddIncomeClicked)
                    }
                )
            }
        }
    }
}

@Composable
fun CardItem(
    modifier: Modifier,
    balance: String,
    income: String,
    expense: String,
    currentMonth: String,
    isNegative: Boolean
) {
    Box(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(220.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(48.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF009650).copy(alpha = 0.3f),
                            Color(0xFF009670).copy(alpha = 0.3f),
                            Color(0xFF009690).copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(1.dp)
                .clip(RoundedCornerShape(48.dp))
                .background(Color(0xFF1E1E1E).copy(alpha = 0.7f))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.White.copy(alpha = 0.1f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(48.dp)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            TransactionTextView(
                text = currentMonth,
                style = Typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TransactionTextView(
                    text = "Balance",
                    style = Typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.size(8.dp))
                TransactionTextView(
                    text = balance,
                    style = Typography.headlineLarge,
                    color = if (isNegative) Color(0xFFFF4B4B) else Color(0xFF10B981)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Income Card
                RowItem(
                    modifier = Modifier.weight(1f),
                    title = "Income",
                    amount = income,
                    image = R.drawable.ic_income,
                    gradientColors = listOf(
                        Color(0xFF10B981).copy(alpha = 0.2f),
                        Color(0xFF059669).copy(alpha = 0.1f)
                    )
                )

                Spacer(modifier = Modifier.size(12.dp))

                // Expense Card
                RowItem(
                    modifier = Modifier.weight(1f),
                    title = "Expense",
                    amount = expense,
                    image = R.drawable.ic_expense,
                    gradientColors = listOf(
                        Color(0xFFEF4444).copy(alpha = 0.2f),
                        Color(0xFFDC2626).copy(alpha = 0.1f)
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .size(150.dp)
                .offset(x = 200.dp, y = (-50).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun RowItem(
    modifier: Modifier,
    title: String,
    amount: String,
    image: Int,
    gradientColors: List<Color>
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = gradientColors
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = image),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
                Spacer(modifier = Modifier.size(8.dp))
                TransactionTextView(
                    text = title,
                    style = Typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.size(8.dp))
            TransactionTextView(
                text = amount,
                style = Typography.titleSmall,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(rememberNavController())
}