package com.example.smartfridge.ui.screens.add_edit

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartfridge.domain.model.StorageLocation
import com.example.smartfridge.ui.theme.LocalSeniorMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFoodScreen(
    foodItemId: Long = 0,
    viewModel: AddEditFoodViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val isSeniorMode = LocalSeniorMode.current

    var showVoiceDialog by remember { mutableStateOf(false) }
    var voiceInputText by remember { mutableStateOf("") }

    LaunchedEffect(foodItemId) {
        if (foodItemId > 0) {
            viewModel.loadFoodItemForEdit(foodItemId)
        }
    }

    LaunchedEffect(uiState.isSavedSuccess) {
        if (uiState.isSavedSuccess) {
            onNavigateBack()
        }
    }

    // 保存提醒視窗 (馬鈴薯/香蕉 放錯位置提醒)
    if (uiState.warningMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissWarning() },
            icon = { Icon(Icons.Default.Warning, contentDescription = "警告", tint = MaterialTheme.colorScheme.error) },
            title = {
                Text(
                    text = "最佳保存溫馨提醒",
                    fontSize = if (isSeniorMode) 22.sp else 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = uiState.warningMessage ?: "",
                    fontSize = if (isSeniorMode) 18.sp else 14.sp
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissWarning() }) {
                    Text("我知道了", fontSize = if (isSeniorMode) 18.sp else 14.sp)
                }
            }
        )
    }

    // 語音輸入對話框 (模擬語音打字)
    if (showVoiceDialog) {
        AlertDialog(
            onDismissRequest = { showVoiceDialog = false },
            icon = { Icon(Icons.Default.Mic, contentDescription = "語音輸入", tint = MaterialTheme.colorScheme.primary) },
            title = {
                Text(
                    text = "請說出食材與到期日",
                    fontSize = if (isSeniorMode) 22.sp else 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "範例：『牛奶 11月25日到期』或『吐司 3天到期』",
                        fontSize = if (isSeniorMode) 16.sp else 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = voiceInputText,
                        onValueChange = { voiceInputText = it },
                        placeholder = { Text("在此輸入或說出內容...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.processVoiceText(voiceInputText)
                        voiceInputText = ""
                        showVoiceDialog = false
                    }
                ) {
                    Text("解析填入", fontSize = if (isSeniorMode) 18.sp else 14.sp)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showVoiceDialog = false }) {
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
                        text = if (uiState.isEditing) "編輯食材" else "新增食材",
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
                },
                actions = {
                    // 語音快打按鈕
                    IconButton(onClick = { showVoiceDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "語音快打",
                            tint = MaterialTheme.colorScheme.primary,
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 常用一鍵補貨模板列
            Text(
                text = "常用食材一鍵補貨：",
                fontSize = if (isSeniorMode) 20.sp else 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                templates.forEach { template ->
                    SuggestionChip(
                        onClick = { viewModel.applyQuickTemplate(template) },
                        label = {
                            Text(
                                text = "+ ${template.name}",
                                fontSize = if (isSeniorMode) 16.sp else 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 食材名稱輸入
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChanged(it) },
                label = { Text("食材名稱 (如：牛奶、高麗菜)", fontSize = if (isSeniorMode) 18.sp else 14.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 食材備註說明輸入 (選填)
            OutlinedTextField(
                value = uiState.note,
                onValueChange = { viewModel.onNoteChanged(it) },
                label = { Text("備註說明 (選填：如特價購入、煮湯用)", fontSize = if (isSeniorMode) 18.sp else 14.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 數量選擇器 (- 1 +)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "數量：",
                    fontSize = if (isSeniorMode) 20.sp else 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { viewModel.onQuantityChanged(-1) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("-", fontSize = if (isSeniorMode) 24.sp else 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = " ${uiState.quantity} ${uiState.unit} ",
                        fontSize = if (isSeniorMode) 22.sp else 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    OutlinedButton(
                        onClick = { viewModel.onQuantityChanged(1) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("+", fontSize = if (isSeniorMode) 24.sp else 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 存放位置選擇
            Text(
                text = "存放位置：",
                fontSize = if (isSeniorMode) 20.sp else 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StorageLocation.entries.forEach { loc ->
                    FilterChip(
                        selected = uiState.location == loc,
                        onClick = { viewModel.onLocationChanged(loc) },
                        label = {
                            Text(
                                text = loc.label,
                                fontSize = if (isSeniorMode) 18.sp else 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 保存期限天數 (手動數字輸入)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "預計可保存天數：",
                    fontSize = if (isSeniorMode) 18.sp else 15.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = uiState.expiryDaysText,
                    onValueChange = { viewModel.onExpiryDaysInputChanged(it) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("天", fontSize = if (isSeniorMode) 16.sp else 13.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.width(if (isSeniorMode) 130.dp else 110.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 快速天數快選鈕
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(7 to "+7天", 30 to "+30天 (1個月)", 90 to "+90天 (3個月)", 180 to "+180天 (半年)", 365 to "+365天 (1年)").forEach { (days, label) ->
                    AssistChip(
                        onClick = { viewModel.onExpiryDaysChanged(days) },
                        label = {
                            Text(
                                text = label,
                                fontSize = if (isSeniorMode) 15.sp else 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }

            Slider(
                value = uiState.expiryDays.coerceIn(1, 365).toFloat(),
                onValueChange = { viewModel.onExpiryDaysChanged(it.toInt()) },
                valueRange = 1f..365f
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 提交按鈕
            Button(
                onClick = { viewModel.saveFoodItem() },
                enabled = uiState.name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isSeniorMode) 60.dp else 48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (uiState.isEditing) "儲存修改" else "完成建檔",
                    fontSize = if (isSeniorMode) 22.sp else 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
