package com.example.smartfridge.ui.screens.shopping

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartfridge.ui.theme.LocalSeniorMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(
    viewModel: ShoppingViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSeniorMode = LocalSeniorMode.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "智慧採買清單",
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
            // 新增待買物品輸入列
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.newItemName,
                    onValueChange = { viewModel.onNewItemNameChanged(it) },
                    placeholder = { Text("要買什麼？(如：鮮奶、吐司)", fontSize = if (isSeniorMode) 18.sp else 14.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.addShoppingItem() },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(
                        horizontal = if (isSeniorMode) 20.dp else 14.dp,
                        vertical = if (isSeniorMode) 16.dp else 12.dp
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "新增")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 清單列表
            if (uiState.shoppingItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "目前採買清單是空的喔！",
                        fontSize = if (isSeniorMode) 20.sp else 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.shoppingItems,
                        key = { it.id }
                    ) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(if (isSeniorMode) 16.dp else 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.isChecked,
                                    onCheckedChange = { viewModel.toggleChecked(item) }
                                )
                                Text(
                                    text = item.name,
                                    fontSize = if (isSeniorMode) 22.sp else 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                // 一鍵補回冰箱庫存按鈕
                                IconButton(onClick = { viewModel.restockToFridge(item) }) {
                                    Icon(
                                        imageVector = Icons.Default.Kitchen,
                                        contentDescription = "入庫冰箱",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(if (isSeniorMode) 28.dp else 22.dp)
                                    )
                                }

                                // 刪除按鈕
                                IconButton(onClick = { viewModel.deleteItem(item) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "刪除",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(if (isSeniorMode) 26.dp else 20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
