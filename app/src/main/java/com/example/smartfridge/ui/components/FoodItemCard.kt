package com.example.smartfridge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartfridge.data.local.entity.FoodItemEntity
import com.example.smartfridge.domain.model.ExpiryStatus
import com.example.smartfridge.ui.theme.ExpiryGreen
import com.example.smartfridge.ui.theme.ExpiryRed
import com.example.smartfridge.ui.theme.ExpiryYellow
import com.example.smartfridge.ui.theme.LocalSeniorMode

@Composable
fun FoodItemCard(
    foodItem: FoodItemEntity,
    onMarkConsumed: () -> Unit,
    onMarkDiscarded: () -> Unit,
    onEditClick: () -> Unit = {},
    onPermanentlyDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isSeniorMode = LocalSeniorMode.current
    val status = foodItem.getExpiryStatus()
    val remainingDays = foodItem.getRemainingDays()

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
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
                    text = "確定要徹底刪除【${foodItem.name}】的這筆紀錄嗎？此動作將從資料庫完全刪除且無法復原。",
                    fontSize = if (isSeniorMode) 18.sp else 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onPermanentlyDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("確定刪除", color = Color.White, fontSize = if (isSeniorMode) 18.sp else 14.sp)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("取消", fontSize = if (isSeniorMode) 18.sp else 14.sp)
                }
            }
        )
    }

    val (badgeBg, badgeText, badgeContentColor) = when (status) {
        ExpiryStatus.RED -> Triple(
            ExpiryRed,
            if (remainingDays <= 0) "已過期" else "剩 ${remainingDays} 天到期",
            Color.White
        )
        ExpiryStatus.YELLOW -> Triple(
            ExpiryYellow,
            "剩 ${remainingDays} 天到期",
            Color.Black
        )
        ExpiryStatus.GREEN -> Triple(
            ExpiryGreen,
            "剩 ${remainingDays} 天到期",
            Color.White
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isSeniorMode) 20.dp else 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = foodItem.name,
                            fontSize = if (isSeniorMode) 24.sp else 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = onEditClick
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "編輯食材",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(if (isSeniorMode) 24.dp else 20.dp)
                            )
                        }
                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier.size(if (isSeniorMode) 32.dp else 24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = "徹底刪除",
                                tint = Color.Gray,
                                modifier = Modifier.size(if (isSeniorMode) 20.dp else 16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "數量：${foodItem.quantity} ${foodItem.unit} | 存放：${foodItem.location.label}",
                        fontSize = if (isSeniorMode) 17.sp else 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (foodItem.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "備註：${foodItem.note}",
                            fontSize = if (isSeniorMode) 16.sp else 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 紅黃綠燈視覺標籤
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeContentColor,
                        fontSize = if (isSeniorMode) 16.sp else 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 高齡友善大按鈕
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onMarkDiscarded,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(
                        horizontal = if (isSeniorMode) 18.dp else 12.dp,
                        vertical = if (isSeniorMode) 12.dp else 8.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "已丟棄",
                        tint = ExpiryRed,
                        modifier = Modifier.size(if (isSeniorMode) 22.dp else 16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "已丟棄",
                        color = ExpiryRed,
                        fontSize = if (isSeniorMode) 18.sp else 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = onMarkConsumed,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(
                        horizontal = if (isSeniorMode) 20.dp else 14.dp,
                        vertical = if (isSeniorMode) 12.dp else 8.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "已食用",
                        tint = Color.White,
                        modifier = Modifier.size(if (isSeniorMode) 22.dp else 16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "已食用",
                        color = Color.White,
                        fontSize = if (isSeniorMode) 18.sp else 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
