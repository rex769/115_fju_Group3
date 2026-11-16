package com.example.smartfridge.ui.screens.fridge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartfridge.domain.model.FoodSortField
import com.example.smartfridge.domain.model.FoodSortOrder
import com.example.smartfridge.domain.model.StorageLocation
import com.example.smartfridge.ui.components.FoodItemCard
import com.example.smartfridge.ui.theme.ExpiryGreen
import com.example.smartfridge.ui.theme.ExpiryRed
import com.example.smartfridge.ui.theme.ExpiryYellow
import com.example.smartfridge.ui.theme.LocalSeniorMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FridgeScreen(
    viewModel: FridgeViewModel = viewModel(),
    onNavigateToAddFood: () -> Unit = {},
    onNavigateToEditFood: (Long) -> Unit = {},
    onNavigateToShopping: () -> Unit = {},
    onNavigateToRecipe: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSeniorMode = LocalSeniorMode.current

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // 第一行：標題在正上方 (字體放大獨佔一行)
                    Text(
                        text = "智慧冰箱庫存",
                        fontSize = if (isSeniorMode) 26.sp else 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 第二行：功能按鈕在下面一行
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 歷史紀錄
                            IconButton(
                                onClick = onNavigateToHistory,
                                modifier = Modifier.size(if (isSeniorMode) 42.dp else 36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "歷史紀錄",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(if (isSeniorMode) 28.dp else 22.dp)
                                )
                            }

                            // 清冰箱食譜
                            IconButton(
                                onClick = onNavigateToRecipe,
                                modifier = Modifier.size(if (isSeniorMode) 42.dp else 36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = "清冰箱食譜",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(if (isSeniorMode) 28.dp else 22.dp)
                                )
                            }

                            // 採買清單
                            IconButton(
                                onClick = onNavigateToShopping,
                                modifier = Modifier.size(if (isSeniorMode) 42.dp else 36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "採買清單",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(if (isSeniorMode) 28.dp else 22.dp)
                                )
                            }

                            // 語音播報
                            IconButton(
                                onClick = { viewModel.speakTodayExpiringSummary() },
                                modifier = Modifier.size(if (isSeniorMode) 42.dp else 36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "語音提醒",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(if (isSeniorMode) 28.dp else 22.dp)
                                )
                            }
                        }

                        // 高齡大字模式切換開關
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "大字",
                                fontSize = if (isSeniorMode) 16.sp else 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = uiState.isSeniorMode,
                                onCheckedChange = { viewModel.toggleSeniorMode(it) }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddFood,
                icon = { Icon(Icons.Default.Add, contentDescription = "新增食材") },
                text = {
                    Text(
                        text = "新增食材",
                        fontSize = if (isSeniorMode) 20.sp else 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 頂部狀態統計面板 (紅/黃/綠燈統計)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusSummaryItem(
                        label = "快過期 (紅)",
                        count = uiState.redCount,
                        color = ExpiryRed,
                        isSenior = isSeniorMode
                    )
                    StatusSummaryItem(
                        label = "需注意 (黃)",
                        count = uiState.yellowCount,
                        color = ExpiryYellow,
                        isSenior = isSeniorMode
                    )
                    StatusSummaryItem(
                        label = "新鮮 (綠)",
                        count = uiState.greenCount,
                        color = ExpiryGreen,
                        isSenior = isSeniorMode
                    )
                }
            }

            // 分類篩選 Chip (全部 / 冷藏 / 冷凍 / 常溫)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedLocation == null,
                    onClick = { viewModel.onLocationFilterSelected(null) },
                    label = { Text("全部", fontSize = if (isSeniorMode) 15.sp else 12.sp) }
                )
                StorageLocation.entries.forEach { location ->
                    FilterChip(
                        selected = uiState.selectedLocation == location,
                        onClick = { viewModel.onLocationFilterSelected(location) },
                        label = { Text(location.label, fontSize = if (isSeniorMode) 15.sp else 12.sp) }
                    )
                }
            }

            // 下拉式排序選單列 (排序依據 + 排序順序)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 下拉選單 1：排序依據
                var expandFieldMenu by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                        onClick = { expandFieldMenu = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "依據：${uiState.sortField.label}",
                            fontSize = if (isSeniorMode) 15.sp else 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(if (isSeniorMode) 22.dp else 18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expandFieldMenu,
                        onDismissRequest = { expandFieldMenu = false }
                    ) {
                        FoodSortField.entries.forEach { field ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = field.label,
                                        fontSize = if (isSeniorMode) 18.sp else 14.sp,
                                        fontWeight = if (uiState.sortField == field) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    viewModel.onSortFieldSelected(field)
                                    expandFieldMenu = false
                                }
                            )
                        }
                    }
                }

                // 下拉選單 2：排序順序
                var expandOrderMenu by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                        onClick = { expandOrderMenu = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "順序：${uiState.sortOrder.label}",
                            fontSize = if (isSeniorMode) 15.sp else 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(if (isSeniorMode) 22.dp else 18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expandOrderMenu,
                        onDismissRequest = { expandOrderMenu = false }
                    ) {
                        FoodSortOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = order.label,
                                        fontSize = if (isSeniorMode) 18.sp else 14.sp,
                                        fontWeight = if (uiState.sortOrder == order) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    viewModel.onSortOrderSelected(order)
                                    expandOrderMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 食材卡片列表
            if (uiState.filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "目前沒有任何食材紀錄喔！",
                        fontSize = if (isSeniorMode) 20.sp else 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(
                        items = uiState.filteredItems,
                        key = { it.id }
                    ) { item ->
                        FoodItemCard(
                            foodItem = item,
                            onMarkConsumed = { viewModel.markAsConsumed(item) },
                            onMarkDiscarded = { viewModel.markAsDiscarded(item) },
                            onEditClick = { onNavigateToEditFood(item.id) },
                            onPermanentlyDelete = { viewModel.permanentlyDeleteItem(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusSummaryItem(
    label: String,
    count: Int,
    color: Color,
    isSenior: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            fontSize = if (isSenior) 28.sp else 22.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = if (isSenior) 14.sp else 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
