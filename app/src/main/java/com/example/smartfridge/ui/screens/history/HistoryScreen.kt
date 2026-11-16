package com.example.smartfridge.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartfridge.data.local.entity.FoodItemEntity
import com.example.smartfridge.domain.model.ItemActionState
import com.example.smartfridge.ui.theme.ExpiryGreen
import com.example.smartfridge.ui.theme.ExpiryRed
import com.example.smartfridge.ui.theme.LocalSeniorMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSeniorMode = LocalSeniorMode.current
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.TAIWAN)

    var itemToDelete by remember { mutableStateOf<FoodItemEntity?>(null) }

    // 刪除確認對話框
    itemToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = {
                Text(
                    text = "徹底刪除紀錄",
                    fontSize = if (isSeniorMode) 22.sp else 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "確定要徹底刪除【${target.name}】的紀錄嗎？刪除後將無法復原。",
                    fontSize = if (isSeniorMode) 18.sp else 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.permanentlyDeleteItem(target)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("確定刪除", color = Color.White, fontSize = if (isSeniorMode) 18.sp else 14.sp)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { itemToDelete = null }) {
                    Text("取消", fontSize = if (isSeniorMode) 18.sp else 14.sp)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "歷史紀錄與浪費統計",
                        fontSize = if (isSeniorMode) 24.sp else 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            modifier = Modifier.size(if (isSeniorMode) 32.dp else 24.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(12.dp)
        ) {
            // 浪費率與數據統計卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${uiState.consumedCount}",
                            fontSize = if (isSeniorMode) 28.sp else 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = ExpiryGreen
                        )
                        Text("已食用", fontSize = if (isSeniorMode) 14.sp else 12.sp)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${uiState.discardedCount}",
                            fontSize = if (isSeniorMode) 28.sp else 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = ExpiryRed
                        )
                        Text("已丟棄", fontSize = if (isSeniorMode) 14.sp else 12.sp)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.TAIWAN, "%.1f%%", uiState.wastePercentage),
                            fontSize = if (isSeniorMode) 28.sp else 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.wastePercentage > 20f) ExpiryRed else MaterialTheme.colorScheme.primary
                        )
                        Text("食材浪費率", fontSize = if (isSeniorMode) 14.sp else 12.sp)
                    }
                }
            }

            // 狀態篩選 Chip (全部 / 已食用 / 已丟棄)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedStatusFilter == null,
                    onClick = { viewModel.onFilterSelected(null) },
                    label = { Text("全部", fontSize = if (isSeniorMode) 16.sp else 13.sp) }
                )
                FilterChip(
                    selected = uiState.selectedStatusFilter == ItemActionState.CONSUMED,
                    onClick = { viewModel.onFilterSelected(ItemActionState.CONSUMED) },
                    label = { Text("已食用", fontSize = if (isSeniorMode) 16.sp else 13.sp) }
                )
                FilterChip(
                    selected = uiState.selectedStatusFilter == ItemActionState.DISCARDED,
                    onClick = { viewModel.onFilterSelected(ItemActionState.DISCARDED) },
                    label = { Text("已丟棄", fontSize = if (isSeniorMode) 16.sp else 13.sp) }
                )
            }

            // 歷史紀錄明細列表
            if (uiState.filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "目前沒有任何歷史紀錄喔！",
                        fontSize = if (isSeniorMode) 20.sp else 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.filteredItems,
                        key = { it.id }
                    ) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(if (isSeniorMode) 16.dp else 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = item.name,
                                            fontSize = if (isSeniorMode) 22.sp else 17.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val dateStr = item.actionDateMillis?.let { dateFormat.format(Date(it)) } ?: "未知時間"
                                        Text(
                                            text = "紀錄時間：$dateStr | 數量：${item.quantity} ${item.unit}",
                                            fontSize = if (isSeniorMode) 15.sp else 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        color = if (item.status == ItemActionState.CONSUMED) ExpiryGreen else ExpiryRed,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = item.status.label,
                                            color = Color.White,
                                            fontSize = if (isSeniorMode) 15.sp else 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { itemToDelete = item },
                                        modifier = Modifier.size(if (isSeniorMode) 32.dp else 24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteForever,
                                            contentDescription = "徹底刪除",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(if (isSeniorMode) 22.dp else 18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    OutlinedButton(
                                        onClick = { viewModel.restoreToFridge(item) },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Undo,
                                            contentDescription = "按錯復原",
                                            modifier = Modifier.size(if (isSeniorMode) 20.dp else 16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("按錯復原", fontSize = if (isSeniorMode) 16.sp else 12.sp)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = { viewModel.addAgainToShoppingList(item) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddShoppingCart,
                                            contentDescription = "再買一次",
                                            tint = Color.White,
                                            modifier = Modifier.size(if (isSeniorMode) 20.dp else 16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("再買一次", fontSize = if (isSeniorMode) 16.sp else 12.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
