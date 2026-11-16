package com.example.smartfridge.data.repository

import com.example.smartfridge.data.local.entity.FoodItemEntity
import com.example.smartfridge.domain.model.ExpiryStatus
import com.example.smartfridge.domain.model.Recipe
import com.example.smartfridge.domain.model.RecipeIngredient

data class RecipeRecommendation(
    val recipe: Recipe,
    val matchedIngredients: List<String>,
    val missingIngredients: List<String>,
    val matchScore: Int,
    val usesExpiringItems: Boolean
)

class RecipeRepository {

    private val presetRecipes = listOf(
        Recipe(
            id = 1,
            title = "鮮奶滑嫩蒸蛋",
            description = "質地細緻綿密，富含優質蛋白質，非常適合長輩輕鬆享用。",
            category = "蛋類料理",
            seniorTag = "好咀嚼",
            ingredients = listOf(
                RecipeIngredient("洗選雞蛋", "2顆"),
                RecipeIngredient("全脂鮮乳", "100ml"),
                RecipeIngredient("鹽", "少許")
            ),
            steps = listOf(
                "將雞蛋打散，加入鮮奶與少許鹽均勻過篩。",
                "倒入碗中蓋上保鮮膜或蓋子。",
                "電鍋外鍋放1杯水，蒸12-15分鐘即可。"
            )
        ),
        Recipe(
            id = 2,
            title = "香煎嫩豆腐",
            description = "外酥內嫩，豆香濃郁，高鈣易消化。",
            category = "豆製品",
            seniorTag = "軟質好消化",
            ingredients = listOf(
                RecipeIngredient("板豆腐", "1盒"),
                RecipeIngredient("洗選雞蛋", "1顆"),
                RecipeIngredient("醬油膏", "1大匙")
            ),
            steps = listOf(
                "板豆腐切厚片，沾上打散的蛋液。",
                "平底鍋少油熱鍋，將豆腐兩面煎至金黃。",
                "起鍋前淋上少許醬油膏即可美味上桌。"
            )
        ),
        Recipe(
            id = 3,
            title = "清炒鮮蔬肉片",
            description = "蔬菜爽口、肉片軟嫩，兼具纖維與蛋白質。",
            category = "家常熱炒",
            seniorTag = "營養均衡",
            ingredients = listOf(
                RecipeIngredient("梅花豬肉片", "1盒"),
                RecipeIngredient("青江菜", "2把"),
                RecipeIngredient("蒜頭", "2瓣")
            ),
            steps = listOf(
                "熱鍋下蒜片爆香，放入豬肉片炒至八分熟。",
                "加入洗淨切段的青江菜大火快炒。",
                "加少許鹽巴調味即可起鍋。"
            )
        ),
        Recipe(
            id = 4,
            title = "清蒸蒜香鮭魚排",
            description = "豐富Omega-3與優質油脂，保護心血管與腦部健康。",
            category = "海鮮料理",
            seniorTag = "低鈉清淡",
            ingredients = listOf(
                RecipeIngredient("鮭魚排", "1尾"),
                RecipeIngredient("薑絲", "適量"),
                RecipeIngredient("蔥花", "少許")
            ),
            steps = listOf(
                "鮭魚排洗淨擦乾，鋪上薑絲。",
                "放入電鍋外鍋加1杯水蒸熟。",
                "起鍋撒上蔥花即可享用原汁原味。"
            )
        ),
        Recipe(
            id = 5,
            title = "熱燉蘋果燕麥粥",
            description = "水果自然香甜，膳食纖維豐富促進腸胃蠕動。",
            category = "養生甜品",
            seniorTag = "控糖高纖",
            ingredients = listOf(
                RecipeIngredient("富士蘋果", "1顆"),
                RecipeIngredient("全脂鮮乳", "200ml"),
                RecipeIngredient("燕麥片", "3大匙")
            ),
            steps = listOf(
                "蘋果去皮切小丁小塊。",
                "小鍋放入鮮奶、燕麥片與蘋果丁微火煮沸。",
                "攪拌至燕麥軟爛即可關火享用。"
            )
        )
    )

    fun getRecommendedRecipes(inStockItems: List<FoodItemEntity>): List<RecipeRecommendation> {
        return presetRecipes.map { recipe ->
            val matched = mutableListOf<String>()
            val missing = mutableListOf<String>()
            var usesExpiring = false

            recipe.ingredients.forEach { ing ->
                val matchingItem = inStockItems.find { item ->
                    item.name.contains(ing.name, ignoreCase = true) || ing.name.contains(item.name, ignoreCase = true)
                }
                if (matchingItem != null) {
                    matched.add(ing.name)
                    if (matchingItem.getExpiryStatus() == ExpiryStatus.RED || matchingItem.getExpiryStatus() == ExpiryStatus.YELLOW) {
                        usesExpiring = true
                    }
                } else {
                    missing.add(ing.name)
                }
            }

            // 計算推薦分數 (符合食材越多分數越高，有快過期食材額外加10分)
            val baseScore = matched.size * 20
            val expiryBonus = if (usesExpiring) 10 else 0
            val score = baseScore + expiryBonus

            RecipeRecommendation(
                recipe = recipe,
                matchedIngredients = matched,
                missingIngredients = missing,
                matchScore = score,
                usesExpiringItems = usesExpiring
            )
        }.sortedByDescending { it.matchScore }
    }
}
