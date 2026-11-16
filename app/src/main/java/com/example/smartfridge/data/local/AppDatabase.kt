package com.example.smartfridge.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.smartfridge.data.local.dao.FoodItemDao
import com.example.smartfridge.data.local.dao.QuickTemplateDao
import com.example.smartfridge.data.local.dao.ShoppingItemDao
import com.example.smartfridge.data.local.dao.StorageTipDao
import com.example.smartfridge.data.local.entity.FoodItemEntity
import com.example.smartfridge.data.local.entity.QuickTemplateEntity
import com.example.smartfridge.data.local.entity.ShoppingItemEntity
import com.example.smartfridge.data.local.entity.StorageTipEntity
import com.example.smartfridge.domain.model.ItemActionState
import com.example.smartfridge.domain.model.StorageLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Database(
    entities = [
        FoodItemEntity::class,
        QuickTemplateEntity::class,
        ShoppingItemEntity::class,
        StorageTipEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun foodItemDao(): FoodItemDao
    abstract fun quickTemplateDao(): QuickTemplateDao
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun storageTipDao(): StorageTipDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_fridge_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                populateInitialData(getInstance(context))
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            CoroutineScope(Dispatchers.IO).launch {
                val database = getInstance(context)
                val templates = database.quickTemplateDao().getAllTemplates().first()
                if (templates.isEmpty()) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val now = System.currentTimeMillis()

            // 1. 預填常用補貨模板
            val defaultTemplates = listOf(
                QuickTemplateEntity(name = "全脂鮮乳", defaultShelfLifeDays = 7, defaultUnit = "瓶", category = "乳品", recommendedLocation = StorageLocation.COLD),
                QuickTemplateEntity(name = "板豆腐", defaultShelfLifeDays = 3, defaultUnit = "盒", category = "豆製品", recommendedLocation = StorageLocation.COLD),
                QuickTemplateEntity(name = "洗選雞蛋", defaultShelfLifeDays = 14, defaultUnit = "盒", category = "蛋類", recommendedLocation = StorageLocation.COLD),
                QuickTemplateEntity(name = "吐司", defaultShelfLifeDays = 4, defaultUnit = "包", category = "烘焙", recommendedLocation = StorageLocation.ROOM),
                QuickTemplateEntity(name = "青江菜", defaultShelfLifeDays = 5, defaultUnit = "把", category = "蔬菜", recommendedLocation = StorageLocation.COLD),
                QuickTemplateEntity(name = "梅花豬肉片", defaultShelfLifeDays = 3, defaultUnit = "盒", category = "肉類", recommendedLocation = StorageLocation.COLD),
                QuickTemplateEntity(name = "鮭魚排", defaultShelfLifeDays = 2, defaultUnit = "尾", category = "海鮮", recommendedLocation = StorageLocation.COLD),
                QuickTemplateEntity(name = "富士蘋果", defaultShelfLifeDays = 10, defaultUnit = "顆", category = "水果", recommendedLocation = StorageLocation.COLD),
                QuickTemplateEntity(name = "原味優格", defaultShelfLifeDays = 10, defaultUnit = "杯", category = "乳品", recommendedLocation = StorageLocation.COLD),
                QuickTemplateEntity(name = "火腿切片", defaultShelfLifeDays = 7, defaultUnit = "包", category = "加工食品", recommendedLocation = StorageLocation.COLD)
            )
            database.quickTemplateDao().insertTemplates(defaultTemplates)

            // 2. 預填保存方式小百科
            val defaultTips = listOf(
                StorageTipEntity(foodName = "香蕉", wrongLocation = StorageLocation.COLD, recommendedLocation = StorageLocation.ROOM, reason = "香蕉冷藏會黑化寒害，建議掛於室溫陰涼通風處。"),
                StorageTipEntity(foodName = "馬鈴薯", wrongLocation = StorageLocation.COLD, recommendedLocation = StorageLocation.ROOM, reason = "馬鈴薯冷藏會使澱粉轉化成糖分影響口感，請置於常溫陰暗處。"),
                StorageTipEntity(foodName = "番茄", wrongLocation = StorageLocation.COLD, recommendedLocation = StorageLocation.ROOM, reason = "番茄冷藏會破壞細胞壁導致軟爛失味，未熟透前請常溫保存。"),
                StorageTipEntity(foodName = "洋蔥", wrongLocation = StorageLocation.COLD, recommendedLocation = StorageLocation.ROOM, reason = "洋蔥在濕冷環境易發芽發霉，請放入常溫網袋保持通風。"),
                StorageTipEntity(foodName = "大蒜", wrongLocation = StorageLocation.COLD, recommendedLocation = StorageLocation.ROOM, reason = "大蒜冷藏容易發芽，建議存放在常溫乾燥通風處。"),
                StorageTipEntity(foodName = "蜂蜜", wrongLocation = StorageLocation.COLD, recommendedLocation = StorageLocation.ROOM, reason = "蜂蜜冷藏會結晶硬化，放置常溫密閉即可良好保存。"),
                StorageTipEntity(foodName = "吐司麵包", wrongLocation = StorageLocation.COLD, recommendedLocation = StorageLocation.FROZEN, reason = "冷藏會加速澱粉老化變乾，短期放常溫，長期請密封冷凍。")
            )
            database.storageTipDao().insertTips(defaultTips)

            // 3. 預填範例食材
            val sampleItems = listOf(
                FoodItemEntity(
                    name = "全脂鮮乳",
                    quantity = 1,
                    unit = "瓶",
                    location = StorageLocation.COLD,
                    addedDateMillis = now - TimeUnit.DAYS.toMillis(6),
                    expiryDateMillis = now + TimeUnit.DAYS.toMillis(1), // 紅燈：剩 1 天
                    category = "乳品",
                    status = ItemActionState.IN_FRIDGE
                ),
                FoodItemEntity(
                    name = "板豆腐",
                    quantity = 2,
                    unit = "盒",
                    location = StorageLocation.COLD,
                    addedDateMillis = now - TimeUnit.DAYS.toMillis(1),
                    expiryDateMillis = now + TimeUnit.DAYS.toMillis(4), // 黃燈：剩 4 天
                    category = "豆製品",
                    status = ItemActionState.IN_FRIDGE
                ),
                FoodItemEntity(
                    name = "洗選雞蛋",
                    quantity = 10,
                    unit = "顆",
                    location = StorageLocation.COLD,
                    addedDateMillis = now - TimeUnit.DAYS.toMillis(2),
                    expiryDateMillis = now + TimeUnit.DAYS.toMillis(12), // 綠燈：剩 12 天
                    category = "蛋類",
                    status = ItemActionState.IN_FRIDGE
                )
            )
            sampleItems.forEach { database.foodItemDao().insertFoodItem(it) }
        }
    }
}
