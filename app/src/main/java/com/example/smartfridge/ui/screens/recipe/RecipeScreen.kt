package com.example.smartfridge.ui.screens.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartfridge.ui.theme.ExpiryGreen
import com.example.smartfridge.ui.theme.ExpiryRed
import com.example.smartfridge.ui.theme.LocalSeniorMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(
    viewModel: RecipeViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSeniorMode = LocalSeniorMode.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "清冰箱智慧食譜",
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
            Text(
                text = "優先推薦包含【快過期食材】與【適合長輩】的家常菜：",
                fontSize = if (isSeniorMode) 18.sp else 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = uiState.recommendations,
                    key = { it.recipe.id }
                ) { rec ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(if (isSeniorMode) 18.dp else 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Restaurant,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(if (isSeniorMode) 28.dp else 22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = rec.recipe.title,
                                        fontSize = if (isSeniorMode) 22.sp else 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = rec.recipe.seniorTag,
                                        fontSize = if (isSeniorMode) 14.sp else 11.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = rec.recipe.description,
                                fontSize = if (isSeniorMode) 16.sp else 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 食材匹配標籤
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "冰箱在庫食材：",
                                    fontSize = if (isSeniorMode) 16.sp else 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (rec.matchedIngredients.isEmpty()) "無" else rec.matchedIngredients.joinToString("、"),
                                    fontSize = if (isSeniorMode) 16.sp else 13.sp,
                                    color = ExpiryGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (rec.missingIngredients.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "缺少食材：",
                                        fontSize = if (isSeniorMode) 16.sp else 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = rec.missingIngredients.joinToString("、"),
                                        fontSize = if (isSeniorMode) 16.sp else 13.sp,
                                        color = ExpiryRed
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 烹調步驟
                            Text(
                                text = "料理步驟：",
                                fontSize = if (isSeniorMode) 16.sp else 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            rec.recipe.steps.forEachIndexed { idx, step ->
                                Text(
                                    text = "${idx + 1}. $step",
                                    fontSize = if (isSeniorMode) 16.sp else 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 操作按鈕列
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.speakRecipeSteps(rec.recipe.title, rec.recipe.steps) },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "朗讀做法",
                                        modifier = Modifier.size(if (isSeniorMode) 22.dp else 16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("朗讀做法", fontSize = if (isSeniorMode) 16.sp else 12.sp)
                                }

                                if (rec.missingIngredients.isNotEmpty()) {
                                    Button(
                                        onClick = { viewModel.addMissingIngredientsToShopping(rec.missingIngredients) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddShoppingCart,
                                            contentDescription = "缺料加採買",
                                            tint = Color.White,
                                            modifier = Modifier.size(if (isSeniorMode) 22.dp else 16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "缺料加採買",
                                            fontSize = if (isSeniorMode) 16.sp else 12.sp,
                                            color = Color.White
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
}
